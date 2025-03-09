package mm.expenses.manager.product.product;

import lombok.*;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.web.pagination.sort.SortOrder;
import mm.expenses.manager.product.api.product.model.NumberOperationRequest;
import mm.expenses.manager.product.api.product.model.TextOperationRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Filter for querying products.
 */
@Builder
@Getter(value = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PRIVATE)
class ProductFilter {

    static final String IS_DELETED_PROPERTY = "isDeleted";

    static final String NAME_PROPERTY = "name";
    static final String NAME_OPERATION_PROPERTY = "nameOperation";

    static final String PRICE_VALUE_PROPERTY = "price.value";
    static final String PRICE_VALUE_OPERATION_PROPERTY = "priceValueOperation";

    static final String PRICE_CURRENCY_PROPERTY = "price.currency";
    static final String PRICE_CURRENCY_OPERATION_PROPERTY = "priceCurrencyOperation";

    static final String SHOULD_CONVERT_CURRENCY_PROPERTY = "shouldConvertCurrency";

    static final String GENERAL_QUERY_PROPERTY = "query";

    private String name;
    private TextOperationRequest nameOperation;

    private BigDecimal priceValue;
    private NumberOperationRequest priceValueOperation;

    private String priceCurrency;
    private TextOperationRequest priceCurrencyOperation;

    private Boolean isDeleted;
    private Boolean shouldConvertCurrency;

    private PageRequest paginationConfig;
    private SortOrder sortConfig;

    private String query;

    /**
     * @return builds query parameters based on passed filters.
     */
    Map<String, String[]> buildQueryParams() {
        val queryParameters = new HashMap<String, String[]>();

        if (isExplicitQueryOriented()) {
            val params = query.split("&");
            for (val queryParameter : params) {
                val parameter = queryParameter.split("=");

                val parameterName = parameter[0];
                val parameterValues = parameter[1].split(",");

                queryParameters.put(parameterName, parameterValues);
            }
            return queryParameters;
        }
        buildSpecificQueryParameters(queryParameters);
        return queryParameters;
    }

    /**
     * build query parameters based on specific query parameters not defined in general query path variable.
     */
    private void buildSpecificQueryParameters(final Map<String, String[]> queryParameters) {
        if (Objects.nonNull(paginationConfig)) {
            queryParameters.put(PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(paginationConfig.getPageNumber())});
            queryParameters.put(PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(paginationConfig.getPageSize())});
        }
        if (Objects.nonNull(sortConfig)) {
            val sortOrder = sortConfig.getOrder();
            if (Objects.nonNull(sortOrder)) {
                queryParameters.put(PaginationConfig.SORT, new String[]{String.format("%s%s%s", sortOrder.getProperty(), Operation.SEPARATOR, sortOrder.getDirection().name())});
            }
        }

        if (isNameOriented()) {
            queryParameters.put(getNameOperationProperty(), new String[]{String.valueOf(getName())});
        }
        if (isProductsPriceValueOriented()) {
            queryParameters.put(getProductsPriceValueOperationProperty(), new String[]{String.valueOf(getPriceValue())});
        }
        if (isProductsPriceCurrencyOriented()) {
            queryParameters.put(getProductsPriceCurrencyOperationProperty(), new String[]{String.valueOf(getPriceCurrency())});
        }

        if (shouldBeDeleted()) {
            queryParameters.put(IS_DELETED_PROPERTY, new String[]{String.valueOf(isDeleted)});
        }
    }

    /**
     * @return checks if all prices should be converted to the default currency or not.
     */
    public boolean shouldConvertPricesToDefault() {
        return Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency;
    }

    /**
     * @return checks if products price value is not null.
     */
    public boolean isProductsPriceValueOriented() {
        val isProductsPriceValuePresent = Objects.nonNull(priceValue);
        val isProductsPriceValueOperationPresent = Objects.nonNull(priceValueOperation);
        if (isProductsPriceValuePresent && !isProductsPriceValueOperationPresent) {
            throw new ApiBadRequestException(ProductExceptionMessage.PRICE_VALUE_MISSING_OPERATOR);
        }
        return isProductsPriceValuePresent && isProductsPriceValueOperationPresent;
    }

    /**
     * @return checks if product name is not null.
     */
    private boolean isNameOriented() {
        val isNamePresent = Objects.nonNull(name);
        val isNameOperationPresent = Objects.nonNull(nameOperation);
        if (isNamePresent && !isNameOperationPresent) {
            throw new ApiBadRequestException(ProductExceptionMessage.PRODUCT_NAME_MISSING_OPERATOR);
        }
        return isNamePresent && isNameOperationPresent;
    }

    /**
     * @return checks if products price currency is not null.
     */
    private boolean isProductsPriceCurrencyOriented() {
        val isProductsPriceCurrencyPresent = Objects.nonNull(priceCurrency);
        val isProductsPriceCurrencyOperationPresent = Objects.nonNull(priceCurrencyOperation);
        if (isProductsPriceCurrencyPresent && !isProductsPriceCurrencyOperationPresent) {
            throw new ApiBadRequestException(ProductExceptionMessage.PRICE_CURRENCY_MISSING_OPERATOR);
        }
        return isProductsPriceCurrencyPresent && isProductsPriceCurrencyOperationPresent;
    }

    /**
     * @return checks if general query parameter is in use.
     */
    private boolean isExplicitQueryOriented() {
        val isQueryPresent = StringUtils.isNotBlank(query);
        if (isQueryPresent) {
            val listOfSpecificFilters = getSpecificFiltersPresenceList();
            if (listOfSpecificFilters.stream().anyMatch(filter -> filter)) {
                throw new ApiBadRequestException(ProductExceptionMessage.PRODUCT_FILTERING_BY_EXPLICIT_QUERY_ONLY);
            }
            return true;
        }
        return false;
    }

    /**
     * @return list of presence of specific query parameters available during filtering products.
     */
    private List<Boolean> getSpecificFiltersPresenceList() {
        val isNamePresent = Objects.nonNull(name);
        val isNameOperationPresent = Objects.nonNull(nameOperation);

        val isProductsPriceValuePresent = Objects.nonNull(priceValue);
        val isProductsPriceValueOperationPresent = Objects.nonNull(priceValueOperation);

        val isProductsPriceCurrencyPresent = Objects.nonNull(priceCurrency);
        val isProductsPriceCurrencyOperationPresent = Objects.nonNull(priceCurrencyOperation);

        return List.of(isNamePresent, isNameOperationPresent, isProductsPriceValuePresent, isProductsPriceValueOperationPresent, isProductsPriceCurrencyPresent, isProductsPriceCurrencyOperationPresent);
    }

    /**
     * @return checks if it should filter only be deleted products.
     */
    private boolean shouldBeDeleted() {
        return Objects.nonNull(isDeleted) && isDeleted;
    }

    private String getNameOperationProperty() {
        val operation = Operation.of(nameOperation);
        return String.format("%s%s", NAME_PROPERTY, operation.getValueWithSeparator());
    }

    private String getProductsPriceValueOperationProperty() {
        val operation = Operation.of(priceValueOperation);
        return String.format("%s%s", PRICE_VALUE_PROPERTY, operation.getValueWithSeparator());
    }

    private String getProductsPriceCurrencyOperationProperty() {
        val operation = Operation.of(priceCurrencyOperation);
        return String.format("%s%s", PRICE_CURRENCY_PROPERTY, operation.getValueWithSeparator());
    }

}
