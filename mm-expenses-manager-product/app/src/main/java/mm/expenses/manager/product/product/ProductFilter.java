package mm.expenses.manager.product.product;

import lombok.*;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.pagination.sort.PostgreSQLSortOrder;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.product.api.product.model.CurrencyOperationRequest;
import mm.expenses.manager.product.api.product.model.NumberOperationRequest;
import mm.expenses.manager.product.api.product.model.TextOperationRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Filter for querying products.
 */
@SuperBuilder
@Getter(value = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PRIVATE)
class ProductFilter extends EntityFilter {

    static final String NAME_PROPERTY = "name";
    static final String NAME_OPERATION_PROPERTY = "nameOperation";

    static final String PRICE_VALUE_PROPERTY = "price.value";
    static final String PRICE_VALUE_OPERATION_PROPERTY = "priceValueOperation";

    static final String PRICE_CURRENCY_PROPERTY = "price.currency";
    static final String PRICE_CURRENCY_OPERATION_PROPERTY = "priceCurrencyOperation";

    public static final String IS_PRODUCT_DELETED_PROPERTY = "isProductDeleted";

    private String name;
    private TextOperationRequest nameOperation;

    private BigDecimal priceValue;
    private NumberOperationRequest priceValueOperation;

    private String priceCurrency;
    private CurrencyOperationRequest priceCurrencyOperation;

    private PostgreSQLSortOrder sortConfig;

    @Override
    protected void buildSpecificQueryParameters(final Map<String, String[]> queryParameters) {
        buildPagination(queryParameters);
        buildDeleted(queryParameters);

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
    }

    @Override
    protected void buildDeleted(final Map<String, String[]> queryParameters) {
        if (shouldBeDeleted()) {
            queryParameters.put(IS_PRODUCT_DELETED_PROPERTY, new String[]{String.valueOf(getIsDeleted())});
        }
    }

    @Override
    protected List<Boolean> getSpecificFiltersPresenceList() {
        val isNamePresent = Objects.nonNull(name);
        val isNameOperationPresent = Objects.nonNull(nameOperation);

        val isProductsPriceValuePresent = Objects.nonNull(priceValue);
        val isProductsPriceValueOperationPresent = Objects.nonNull(priceValueOperation);

        val isProductsPriceCurrencyPresent = Objects.nonNull(priceCurrency);
        val isProductsPriceCurrencyOperationPresent = Objects.nonNull(priceCurrencyOperation);

        return List.of(isNamePresent, isNameOperationPresent, isProductsPriceValuePresent, isProductsPriceValueOperationPresent, isProductsPriceCurrencyPresent, isProductsPriceCurrencyOperationPresent);
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
     * @return checks if products price currency is not null.
     */
    public boolean isProductsPriceCurrencyOriented() {
        val isProductsPriceCurrencyPresent = Objects.nonNull(priceCurrency);
        val isProductsPriceCurrencyOperationPresent = Objects.nonNull(priceCurrencyOperation);
        if (isProductsPriceCurrencyPresent && !isProductsPriceCurrencyOperationPresent) {
            throw new ApiBadRequestException(ProductExceptionMessage.PRICE_CURRENCY_MISSING_OPERATOR);
        }
        return isProductsPriceCurrencyPresent && isProductsPriceCurrencyOperationPresent;
    }

    /**
     * @return sorting order of product's price value if available
     */
    public Optional<Sort.Order> getPriceValueSortingOrder() {
        val isSortingEnabled = Objects.nonNull(sortConfig);
        if (isSortingEnabled) {
            val sortOrder = sortConfig.getOrder();
            if (Objects.nonNull(sortOrder) && Objects.equals(sortOrder.getProperty(), ProductFilterView.PRICE_VALUE_FIELD_NAME)) {
                return Optional.of(sortOrder);
            }
        }
        return Optional.empty();
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
