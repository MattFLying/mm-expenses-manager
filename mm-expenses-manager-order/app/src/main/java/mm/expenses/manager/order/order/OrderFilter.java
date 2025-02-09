package mm.expenses.manager.order.order;

import lombok.*;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.web.pagination.sort.SortOrder;
import mm.expenses.manager.order.api.order.model.NumberOperationRequest;
import mm.expenses.manager.order.api.order.model.TextOperationRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Filter for querying orders.
 */
@Builder
@Getter(value = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PRIVATE)
class OrderFilter {

    static final String IS_DELETED_PROPERTY = "isDeleted";

    static final String NAME_PROPERTY = "name";
    static final String NAME_OPERATION_PROPERTY = "nameOperation";

    static final String PRODUCTS_PROPERTY = "products";
    static final String PRODUCTS_COUNT_PROPERTY = "productsCount";
    static final String PRODUCTS_COUNT_OPERATION_PROPERTY = "productsCountOperation";

    static final String SHOULD_CONVERT_CURRENCY_PROPERTY = "shouldConvertCurrency";

    private String name;
    private TextOperationRequest nameOperation;

    private Integer productsCount;
    private NumberOperationRequest productsCountOperation;

    private Boolean isDeleted;
    private Boolean shouldConvertCurrency;

    private PageRequest paginationConfig;
    private SortOrder sortConfig;

    /**
     * @return builds query parameters based on passed filters.
     */
    Map<String, String[]> buildQueryParams() {
        val params = new HashMap<String, String[]>();

        if (Objects.nonNull(paginationConfig)) {
            params.put(PaginationConfig.PAGE_NUMBER, new String[]{String.valueOf(paginationConfig.getPageNumber())});
            params.put(PaginationConfig.PAGE_SIZE, new String[]{String.valueOf(paginationConfig.getPageSize())});
        }
        if (Objects.nonNull(sortConfig)) {
            val sortOrder = sortConfig.getOrder();
            if (Objects.nonNull(sortOrder)) {
                params.put(PaginationConfig.SORT, new String[]{String.format("%s%s%s", sortOrder.getProperty(), Operation.SEPARATOR, sortOrder.getDirection().name())});
            }
        }

        if (isNameOriented()) {
            params.put(getNameOperationProperty(), new String[]{String.valueOf(getName())});
        }
        if (isProductsCountOriented()) {
            params.put(getProductsCountOperationProperty(), new String[]{String.valueOf(getProductsCount())});
        }

        if (shouldBeDeleted()) {
            params.put(IS_DELETED_PROPERTY, new String[]{String.valueOf(isDeleted)});
        }
        return params;
    }

    /**
     * @return checks if all prices should be converted to the default currency or not.
     */
    public boolean shouldConvertPricesToDefault() {
        return Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency;
    }

    /**
     * @return checks if product name is not null.
     */
    private boolean isNameOriented() {
        val isNamePresent = Objects.nonNull(name);
        val isNameOperationPresent = Objects.nonNull(nameOperation);
        if (isNamePresent && !isNameOperationPresent) {
            throw new ApiBadRequestException(OrderExceptionMessage.ORDER_NAME_MISSING_OPERATOR);
        }
        return isNamePresent && isNameOperationPresent;
    }

    /**
     * @return checks if products count is not null.
     */
    private boolean isProductsCountOriented() {
        val isProductsCountPresent = Objects.nonNull(productsCount);
        val isProductsCountOperationPresent = Objects.nonNull(productsCountOperation);
        if (isProductsCountPresent && !isProductsCountOperationPresent) {
            throw new ApiBadRequestException(OrderExceptionMessage.ORDER_PRODUCTS_COUNT_MISSING_OPERATOR);
        }
        return isProductsCountPresent && isProductsCountOperationPresent;
    }

    /**
     * @return checks if should filter only be deleted products.
     */
    private boolean shouldBeDeleted() {
        return Objects.nonNull(isDeleted) && isDeleted;
    }

    private String getNameOperationProperty() {
        val operation = Operation.of(nameOperation);
        return String.format("%s%s", NAME_PROPERTY, operation.getValueWithSeparator());
    }

    private String getProductsCountOperationProperty() {
        val operation = Operation.of(productsCountOperation);
        return String.format("%s%s", PRODUCTS_PROPERTY, operation.getValueWithSeparator());
    }

}
