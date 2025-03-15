package mm.expenses.manager.order.order;

import lombok.*;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.web.pagination.sort.SortOrder;
import mm.expenses.manager.order.api.order.model.NumberOperationRequest;
import mm.expenses.manager.order.api.order.model.TextOperationRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Filter for querying orders.
 */
@SuperBuilder
@Getter(value = AccessLevel.PRIVATE)
@Setter(value = AccessLevel.PRIVATE)
class OrderFilter extends EntityFilter {

    static final String NAME_PROPERTY = "name";
    static final String NAME_OPERATION_PROPERTY = "nameOperation";

    static final String PRODUCTS_PROPERTY = "products";
    static final String PRODUCTS_COUNT_PROPERTY = "productsCount";
    static final String PRODUCTS_COUNT_OPERATION_PROPERTY = "productsCountOperation";

    private String name;
    private TextOperationRequest nameOperation;

    private Integer productsCount;
    private NumberOperationRequest productsCountOperation;

    private SortOrder sortConfig;

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
        if (isProductsCountOriented()) {
            queryParameters.put(getProductsCountOperationProperty(), new String[]{String.valueOf(getProductsCount())});
        }
    }

    @Override
    protected List<Boolean> getSpecificFiltersPresenceList() {
        val isNamePresent = Objects.nonNull(name);
        val isNameOperationPresent = Objects.nonNull(nameOperation);

        val isProductsCountPresent = Objects.nonNull(productsCount);
        val isProductsCountOperationPresent = Objects.nonNull(productsCountOperation);

        return List.of(isNamePresent, isNameOperationPresent, isProductsCountPresent, isProductsCountOperationPresent);
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

    private String getNameOperationProperty() {
        val operation = Operation.of(nameOperation);
        return String.format("%s%s", NAME_PROPERTY, operation.getValueWithSeparator());
    }

    private String getProductsCountOperationProperty() {
        val operation = Operation.of(productsCountOperation);
        return String.format("%s%s", PRODUCTS_PROPERTY, operation.getValueWithSeparator());
    }

}
