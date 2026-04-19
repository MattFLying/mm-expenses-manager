package mm.expenses.manager.product.processor.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.specification.HandledSpecificationResult;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.ProductFilterView;
import mm.expenses.manager.product.processor.ProductFilterViewRepository;
import org.springframework.data.domain.Page;

import java.util.Objects;

/**
 * Step in chain of search {@link ProductFilterView}s.
 * Execute prepared criterias to search expected products.
 */
@Slf4j
@RequiredArgsConstructor
final class SearchProductsByCriteria extends SearchProductsChain {

    final static String PAGED_PRODUCTS_KEY = "pagedProducts";

    private final ProductFilterViewRepository repository;
    private final PriceConverter priceConverter;

    @Override
    public Page<ProductFilterView> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            context.getArgument(PrepareSpecificationCriterias.SPECIFICATION_RESULT_KEY)
                    .ifPresentOrElse(
                            specificationResult -> {
                                final var pagedProducts = repository.findAll(
                                        ((HandledSpecificationResult<ProductFilterView>) specificationResult).specification(),
                                        ((HandledSpecificationResult<ProductFilterView>) specificationResult).pageable()
                                );
                                context.addArgument(PAGED_PRODUCTS_KEY, pagedProducts);
                            },
                            () -> {
                                log.error("Cannot search products because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new ConvertPricesForSearchProducts(priceConverter));
            }
            return next.handleRequest(queryFilter);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products search error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND, exception);
        }
    }

}
