package mm.expenses.manager.product.processor.search;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.product.core.ProductFilterView;
import mm.expenses.manager.product.core.ProductFilterViewRepository;
import mm.expenses.manager.product.core.ProductMapper;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.*;
import mm.expenses.manager.product.processor.decorator.ProductFilterViewClassicMapperToResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for search products.
 */
@Slf4j
@Component
class SearchProductsHandler extends BaseProductHandler {

    private final ProductFilterViewSpecificationHandler specificationHandler;
    private final ProductFilterViewRepository repository;

    SearchProductsHandler(final ProductFilterViewRepository repository, final ProductMapper mapper, final PriceConverter priceConverter, final ProductFilterViewSpecificationHandler specificationHandler) {
        super(mapper, priceConverter);
        this.specificationHandler = specificationHandler;
        this.repository = repository;
    }

    @Override
    public Type getType() {
        return Type.SEARCH_PRODUCTS;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var chain = ChainCommandExecution.build(
                new PrepareSpecificationCriterias(repository, specificationHandler, priceConverter),
                new SearchProductsByCriteria(repository, priceConverter),
                new ConvertPricesForSearchProducts(priceConverter),
                new PrepareSearchProductsResult()
        );
        return Response.builder()
                .pagedFilteredResponse((Page<ProductFilterView>) chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof ProductHandler.Request searchRequest) {
                final var pagedProducts = handle(searchRequest);
                final var decorator = new ProductFilterViewClassicMapperToResponse(mapper);

                if (pagedProducts instanceof ProductHandler.Response paged) {
                    final var result = decorator.decorate(paged.getPagedFilteredResponse());
                    return Response.builder()
                            .pagedProductResponse(paged.getPagedProductResponse())
                            .decoratedPagedResponse(mapper.mapToPageResponse(result))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products search error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND, exception);
        }
    }

}
