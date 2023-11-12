package mm.expenses.manager.product.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
class ProductScheduler {

    private final PaginationHelper pagination;

    @Scheduled(cron = "${app.product.clear-deleted-cron}")
    void cleanDeletedProducts() {
        log.info("Clean deleted products in progress.");

        var deletedCount = 0L;

        var pageNumber = 0;
        var pageable = pageRequest(pageNumber);
        var page = ProductContext.findDeleted(pageable);
        while (page.hasContent()) {
            final var ids = page.getContent()
                    .stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            ProductContext.removeProducts(ids);
            deletedCount += ids.size();

            pageNumber++;
            pageable = pageRequest(pageNumber);
            page = ProductContext.findDeleted(pageable);
        }

        log.info("{} products were completely removed.", deletedCount);
    }

    private PageRequest pageRequest(final int pageNumber) {
        return pagination.getPageRequest(pageNumber, 50);
    }

}
