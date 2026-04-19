package mm.expenses.manager.product.processor.delete.clean;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.postgresql.pagination.PaginationHelper;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.processor.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for permanently removing products that are already marked as deleted.
 * This handler is for scheduled processing of cleaning previously deleted products.
 */
@Slf4j
@Component
class HardDeleteProductsHandler extends ProductHandler {

    private final PaginationHelper pagination;

    HardDeleteProductsHandler(final ProductRepository repository, final ProductMapper mapper, final PriceConverter priceConverter, final PaginationHelper pagination) {
        super(repository, mapper, priceConverter);

        this.pagination = pagination;
    }

    @Override
    public Type getType() {
        return Type.HARD_DELETE;
    }

    @Override
    public Response handle(final Request request) {
        log.info("Clean deleted products in progress.");

        var deletedCount = 0L;
        var pageNumber = 0;
        var pageable = pageRequest(pageNumber);

        var find = new FindDeletedProducts(repository);
        var delete = new HardDeleteProducts(repository);

        var page = find.handleRequest(pageable);
        while (page.hasContent()) {
            final var ids = find.getProductIds();

            deletedCount += delete.handleRequest(ids);

            pageNumber++;
            pageable = pageRequest(pageNumber);
            page = find.handleRequest(pageable);
        }

        log.info("{} products were completely removed.", deletedCount);
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
