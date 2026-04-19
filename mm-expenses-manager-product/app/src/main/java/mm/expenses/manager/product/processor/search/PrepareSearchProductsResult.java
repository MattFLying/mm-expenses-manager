package mm.expenses.manager.product.processor.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.ProductFilterView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Step in chain of search {@link ProductFilterView}s.
 * Execute prepared criterias to search expected products.
 */
@Slf4j
@RequiredArgsConstructor
final class PrepareSearchProductsResult extends SearchProductsChain {

    @Override
    public void setNextHandler(final ChainCommandExecution<EntityFilter, Page<ProductFilterView>> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Page<ProductFilterView> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            final var pagedProductsOpt = context.getArgument(SearchProductsByCriteria.PAGED_PRODUCTS_KEY);
            if (pagedProductsOpt.isPresent()) {
                var pagedProducts = (Page<ProductFilterView>) pagedProductsOpt.get();
                final var isCurrencyConversionRequired = queryFilter.shouldConvertCurrenciesToDefault();
                final var priceValueSortingOrder = ((ProductFilter) queryFilter).getPriceValueSortingOrder();

                if (isCurrencyConversionRequired && priceValueSortingOrder.isPresent()) {
                    final var direction = priceValueSortingOrder.get().getDirection();
                    final var content = pagedProducts.getContent();

                    pagedProducts = new PageImpl<>(sortFilteredProductsByPriceValue(direction, content), pagedProducts.getPageable(), pagedProducts.getTotalElements());
                    // in case if currency conversion is not needed but sorting by price value is expected then sorting is required again to keep the expected currency first
                } else if (priceValueSortingOrder.isPresent()) {
                    final var direction = priceValueSortingOrder.get().getDirection();
                    final var content = pagedProducts.getContent();

                    final var defaultCurrencyOpt = context.getArgument(PrepareSpecificationCriterias.DEFAULT_CURRENCY_KEY);

                    pagedProducts = new PageImpl<>(sortFilteredProductsByPriceCurrencyAndValue(direction, content, (CurrencyCode) defaultCurrencyOpt.get()), pagedProducts.getPageable(), pagedProducts.getTotalElements());
                }
                context.addArgument(SearchProductsByCriteria.PAGED_PRODUCTS_KEY, pagedProducts);


                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(queryFilter);
                } else {
                    return pagedProducts;
                }
            } else {
                log.error("Cannot search products because of one of required parameters is null.");
                throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND);
            }
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products search error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND, exception);
        }
    }

    private List<ProductFilterView> sortFilteredProductsByPriceValue(final Sort.Direction direction, final List<ProductFilterView> content) {
        final var comparator = direction.isAscending()
                ? Comparator.comparing(ProductFilterView::getPriceValue)
                : Comparator.comparing(ProductFilterView::getPriceValue).reversed();

        return content.stream()
                .sorted(comparator)
                .toList();
    }

    private List<ProductFilterView> sortFilteredProductsByPriceCurrencyAndValue(final Sort.Direction direction, final List<ProductFilterView> content, final CurrencyCode expectedCurrencyCode) {
        final var comparator = direction.isAscending()
                ? Comparator.<ProductFilterView, Boolean>comparing(product -> !Objects.equals(expectedCurrencyCode, product.getPriceCurrency())).thenComparing(ProductFilterView::getPriceCurrency).thenComparing(ProductFilterView::getPriceValue)
                : Comparator.<ProductFilterView, Boolean>comparing(product -> Objects.equals(expectedCurrencyCode, product.getPriceCurrency())).thenComparing(ProductFilterView::getPriceCurrency).thenComparing(ProductFilterView::getPriceValue).reversed();

        return content.stream()
                .sorted(comparator)
                .toList();
    }

}
