package mm.expenses.manager.product.processor.search;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.BaseProductHandler;
import mm.expenses.manager.product.processor.ProductFilterView;
import mm.expenses.manager.product.processor.ProductFilterViewRepository;
import mm.expenses.manager.product.processor.ProductMapper;
import mm.expenses.manager.product.processor.decorator.ProductFilterViewClassicMapperToResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for search products.
 */
@Slf4j
@Component
class SearchProductsHandler extends BaseProductHandler<ProductFilterView> {

    private final ProductFilterViewSpecificationHandler specificationHandler;
    private final ProductFilterViewRepository repository;

    SearchProductsHandler(final ProductFilterViewRepository repository, final ProductMapper mapper, final PriceConverter priceConverter, final ProductFilterViewSpecificationHandler specificationHandler) {
        super(mapper, priceConverter);
        this.specificationHandler = specificationHandler;
        this.repository = repository;
    }

    @Override
    public Type getType() {
        return Type.SEARCH;
    }

    @Override
    public Response handle(final Request request) {
        final var chain = ChainCommandExecution.build(
                new PrepareSpecificationCriterias(repository, specificationHandler, priceConverter),
                new SearchProductsByCriteria(repository, priceConverter),
                new ConvertPricesForSearchProducts(priceConverter),
                new PrepareSearchProductsResult()
        );

        return of((Page<ProductFilterView>) chain.handleRequest(request.request()));
    }

    @Override
    public Response handleDecorated(final Request request) {
        try {
            final var pagedProducts = handle(request);
            final var decorator = new ProductFilterViewClassicMapperToResponse(mapper);
            final var result = decorator.decorate(pagedProducts.getPagedResponse());

            return of((Page<ProductFilterView>) pagedProducts.getPagedResponse(), mapper.mapToPageResponse(result));
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products search error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND, exception);
        }
    }

}
