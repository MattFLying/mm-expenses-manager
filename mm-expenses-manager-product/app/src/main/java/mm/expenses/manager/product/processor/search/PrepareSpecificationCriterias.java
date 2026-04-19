package mm.expenses.manager.product.processor.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.specification.criteria.AdditionalCriteriaParameter;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.ProductFilterView;
import mm.expenses.manager.product.processor.ProductFilterViewRepository;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Step in chain of search {@link ProductFilterView}s.
 * Prepares specification criterias to find expected products.
 */
@Slf4j
@RequiredArgsConstructor
final class PrepareSpecificationCriterias extends SearchProductsChain {

    final static String SPECIFICATION_RESULT_KEY = "specificationResult";
    final static String DEFAULT_CURRENCY_KEY = "defaultCurrency";

    private final ProductFilterViewRepository repository;
    private final ProductFilterViewSpecificationHandler specificationHandler;
    private final PriceConverter priceConverter;

    @Override
    public Page<ProductFilterView> handleRequest(final EntityFilter queryFilter) {
        try {
            Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

            final var defaultCurrency = priceConverter.getDefaultCurrency();
            context.addArgument(DEFAULT_CURRENCY_KEY, defaultCurrency);

            final var filterParameters = queryFilter.buildQueryParams();
            final var hasCurrencyRequested = ((ProductFilter) queryFilter).isProductsPriceCurrencyOriented();

            final var additionalCriteriaParameters = prepareAdditionalCriteriaParameters(hasCurrencyRequested, defaultCurrency);
            final var specificationResult = specificationHandler.handle(
                    filterParameters,
                    additionalCriteriaParameters.toArray(new AdditionalCriteriaParameter[0])
            );
            context.addArgument(SPECIFICATION_RESULT_KEY, specificationResult);

            if (!hasNext()) {
                setNextHandler(new SearchProductsByCriteria(repository, priceConverter));
            }
            return next.handleRequest(queryFilter);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products search error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_CANNOT_BE_FOUND, exception);
        }
    }

    private List<AdditionalCriteriaParameter> prepareAdditionalCriteriaParameters(final boolean hasCurrencyRequested, final CurrencyCode defaultCurrency) {
        final var additionalCriteriaParameters = new ArrayList<AdditionalCriteriaParameter>();

        // if there are no other expectations return the original price
        // at the end the expected product's price is the specific currency or the original price currency
        additionalCriteriaParameters.add(AdditionalCriteriaParameter.of(ProductFilterView.IS_PRICE_ORIGINAL_FIELD_NAME, true));
        if (!hasCurrencyRequested) {
            // but if specific currency is not requested then return the default currency
            additionalCriteriaParameters.add(AdditionalCriteriaParameter.of(ProductFilterView.PRICE_CURRENCY_FIELD_NAME, defaultCurrency));
        }
        return additionalCriteriaParameters;
    }

}
