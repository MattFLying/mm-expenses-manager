package mm.expenses.manager.product.processor.conversion;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.postgresql.pagination.PaginationHelper;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.processor.ProductAsyncHandler;
import mm.expenses.manager.product.processor.ProductHandler;
import mm.expenses.manager.product.processor.ProductMapper;
import mm.expenses.manager.product.processor.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;

/**
 * Handler responsible for products prices conversion if there are some missing currencies for any stored products.
 */
@Slf4j
@Component
class ProductsCurrenciesConversionHandler extends ProductHandler {

    private final ProductAsyncHandler asyncHandler;
    private final PaginationHelper pagination;

    ProductsCurrenciesConversionHandler(final ProductRepository repository, final ProductMapper mapper, final PriceConverter priceConverter, final ProductAsyncHandler asyncHandler, final PaginationHelper pagination) {
        super(repository, mapper, priceConverter);

        this.asyncHandler = asyncHandler;
        this.pagination = pagination;
    }

    @Override
    public Type getType() {
        return Type.UPDATE_PRICE_CURRENCIES;
    }

    @Override
    public Response handle(final Request request) {
        log.info("Currencies conversion for products in progress.");

        final var now = Instant.now();
        var updatedCount = 0L;
        var pageNumber = 0;
        var pageable = pageRequest(pageNumber);

        var find = new FindProductsWithMissingPriceCurrencies(repository);
        var update = new UpdateConvertedPrices(repository, priceConverter, now);
        var async = new AsyncProductsPricesUpdated(asyncHandler);

        var page = find.handleRequest(pageable);
        while (page.hasContent()) {
            final var products = find.getProducts();
            final var updated = update.handleRequest(products);
            updatedCount += updated.values().stream().mapToLong(Collection::size).sum();

            async.handleRequest(updated);

            pageNumber++;
            pageable = pageRequest(pageNumber);
            page = find.handleRequest(pageable);
        }

        log.info("{} product prices updated.", updatedCount);
        return null;
    }

    @Override
    public Response handleDecorated(final Request request) {
        return null;
    }

    private PageRequest pageRequest(final int pageNumber) {
        return pagination.getPageRequest(pageNumber, 50);
    }

}
