package mm.expenses.manager.product.processor.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.core.ProductFilterView;
import org.springframework.data.domain.Page;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Step in chain of search {@link ProductFilterView}s.
 * Execute prepared criterias to search expected products.
 */
@Slf4j
@RequiredArgsConstructor
final class ConvertPricesForSearchProducts extends SearchProductsChain {

    private final PriceConverter priceConverter;

    @Override
    public Page<ProductFilterView> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            context.getArgument(SearchProductsByCriteria.PAGED_PRODUCTS_KEY)
                    .ifPresentOrElse(
                            pagedProducts -> {
                                final var isCurrencyConversionRequired = queryFilter.shouldConvertCurrenciesToDefault();
                                if (isCurrencyConversionRequired) {
                                    final var defaultCurrencyOpt = context.getArgument(PrepareSpecificationCriterias.DEFAULT_CURRENCY_KEY);
                                    defaultCurrencyOpt.ifPresent(defaultCurrency -> {
                                        // calculate prices if there are different currencies than default
                                        convertCurrencies((Page<ProductFilterView>) pagedProducts, (CurrencyCode) defaultCurrency);
                                        context.addArgument(SearchProductsByCriteria.PAGED_PRODUCTS_KEY, pagedProducts);
                                    });
                                }
                            },
                            () -> {
                                log.error("Cannot search products because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new PrepareSearchProductsResult());
            }
            return next.handleRequest(queryFilter);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products search error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND, exception);
        }
    }

    private void convertCurrencies(final Page<ProductFilterView> pagedProducts, final CurrencyCode defaultCurrency) {
        final var isCurrencyConversionNeeded = pagedProducts.getContent()
                .stream()
                .anyMatch(product -> !Objects.equals(product.getPriceCurrency(), defaultCurrency));

        if (isCurrencyConversionNeeded) {
            final var convertedProducts = priceConverter.convertPrices(pagedProducts.getContent())
                    .stream()
                    .collect(Collectors.toMap(ProductFilterView::getProductId, Function.identity()));

            pagedProducts.getContent()
                    .forEach(product -> {
                        final var convertedProduct = convertedProducts.get(product.getProductId());
                        if (Objects.isNull(convertedProduct)) {
                            return;
                        }
                        product.setPriceValue(convertedProduct.getPriceValue());
                        product.setPriceCurrency(convertedProduct.getPriceCurrency());
                    });
        }
    }

}
