package mm.expenses.manager.order.processor.search;

import lombok.*;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.pagination.sort.PostgreSQLSortOrder;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.postgresql.specification.criteria.AdditionalCriteriaParameter;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.order.api.order.model.NumberOperationRequest;
import mm.expenses.manager.order.api.order.model.TextOperationRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Filter for querying orders.
 */
@SuperBuilder
@Getter
@Setter(value = AccessLevel.PRIVATE)
public class OrderFilter extends EntityFilter {

    public static final String NAME_PROPERTY = "name";
    public static final String NAME_OPERATION_PROPERTY = "nameOperation";

    public static final String PRODUCTS_PROPERTY = "products";
    public static final String PRODUCTS_COUNT_PROPERTY = "productsCount";
    public static final String PRODUCTS_COUNT_OPERATION_PROPERTY = "productsCountOperation";

    private String name;
    private TextOperationRequest nameOperation;

    private Integer productsCount;
    private NumberOperationRequest productsCountOperation;

    private PostgreSQLSortOrder sortConfig;

    @Override
    protected void buildSpecificQueryParameters(final Map<String, String[]> queryParameters) {
        buildPagination(queryParameters);
        buildDeleted(queryParameters);

        if (Objects.nonNull(sortConfig)) {
            final var sortOrder = sortConfig.getOrder();
            if (Objects.nonNull(sortOrder)) {
                queryParameters.put(PaginationConfig.SORT, new String[]{String.format("%s%s%s", sortOrder.getProperty(), Operation.SEPARATOR, sortOrder.getDirection().name())});
            }
        }

        if (isNameOriented()) {
            queryParameters.put(getNameOperationProperty(), new String[]{String.valueOf(getName())});
        }
    }

    @Override
    protected void prepareAdditionalCriteria(final String parameterName, final String operation, final String[] values) {
        if (Strings.CS.equals(PRODUCTS_COUNT_PROPERTY, parameterName)) {
            productsCountOperation = NumberOperationRequest.fromValue(operation);
            productsCount = Integer.valueOf(values[0]); // ignore if there is more than single value, just take the first one
        }
    }

    @Override
    protected void prepareAdditionalCriteria() {
        if (isProductsCountOriented()) {
            addAdditionalCriteria(
                    AdditionalCriteriaParameter.of(
                            PRODUCTS_COUNT_PROPERTY,
                            String.valueOf(productsCount),
                            Operation.of(getProductsCountOperation()),
                            FieldType.Integer,
                            false
                    )
            );
        }
    }

    @Override
    protected void buildDeleted(final Map<String, String[]> queryParameters) {
        final var isDeleted = getIsDeleted();
        if (Objects.nonNull(isDeleted)) {
            queryParameters.put(IS_DELETED_PROPERTY, new String[]{String.valueOf(isDeleted)});
        }
    }

    @Override
    protected List<Boolean> getSpecificFiltersPresenceList() {
        final var isNamePresent = Objects.nonNull(name);
        final var isNameOperationPresent = Objects.nonNull(nameOperation);

        final var isProductsCountPresent = Objects.nonNull(productsCount);
        final var isProductsCountOperationPresent = Objects.nonNull(productsCountOperation);

        return List.of(isNamePresent, isNameOperationPresent, isProductsCountPresent, isProductsCountOperationPresent);
    }

    /**
     * @return checks if product name is not null.
     */
    private boolean isNameOriented() {
        final var isNamePresent = Objects.nonNull(name);
        final var isNameOperationPresent = Objects.nonNull(nameOperation);
        if (isNamePresent && !isNameOperationPresent) {
            throw new ApiBadRequestException(OrderExceptionMessage.ORDER_NAME_MISSING_OPERATOR);
        }
        return isNamePresent && isNameOperationPresent;
    }

    /**
     * @return checks if products count is not null.
     */
    private boolean isProductsCountOriented() {
        final var isProductsCountPresent = Objects.nonNull(productsCount);
        final var isProductsCountOperationPresent = Objects.nonNull(productsCountOperation);
        if (isProductsCountPresent && !isProductsCountOperationPresent) {
            throw new ApiBadRequestException(OrderExceptionMessage.ORDER_PRODUCTS_COUNT_MISSING_OPERATOR);
        }
        return isProductsCountPresent && isProductsCountOperationPresent;
    }

    private String getNameOperationProperty() {
        final var operation = Operation.of(nameOperation);
        return String.format("%s%s", NAME_PROPERTY, operation.getValueWithSeparator());
    }

}
