package mm.expenses.manager.product.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.postgresql.pagination.PaginationHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class ProductScheduler {

    private final PaginationHelper pagination;
    private final ProductService service;
    private final ProductAsyncHandler asyncHandler;

    @Scheduled(cron = "${app.product.clear-deleted-cron}")
    void cleanDeletedProducts() {
        log.info("Clean deleted products in progress.");

        var deletedCount = 0L;

        var pageNumber = 0;
        var pageable = pageRequest(pageNumber);
        var page = service.findDeleted(pageable);
        while (page.hasContent()) {
            final var ids = page.getContent()
                    .stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            service.delete(ids);
            deletedCount += ids.size();

            pageNumber++;
            pageable = pageRequest(pageNumber);
            page = service.findDeleted(pageable);
        }

        log.info("{} products were completely removed.", deletedCount);
    }

    @Transactional
    @Scheduled(cron = "${app.product.update-currencies-cron}")
    void updateCurrenciesForProductsPrices() {
        log.info("Currencies conversion for products in progress.");

        var pageNumber = 0;
        var pageable = pageRequest(pageNumber);
        var page = service.findProductsWithMissingPriceCurrencies(pageable);

        var updatedCount = 0L;

        while (page.hasContent()) {
            final var content = page.getContent();
            final var updated = service.updateConvertedPrices(content);

            updated.forEach((product, prices) -> asyncHandler.sendProductPricesMessages(product, prices, AsyncKafkaOperation.UPDATE));
            updatedCount += updated.values().stream().mapToLong(Collection::size).sum();

            pageNumber++;
            pageable = pageRequest(pageNumber);
            page = service.findProductsWithMissingPriceCurrencies(pageable);
        }

        log.info("{} product prices updated.", updatedCount);
    }

    private PageRequest pageRequest(final int pageNumber) {
        return pagination.getPageRequest(pageNumber, 50);
    }

}
