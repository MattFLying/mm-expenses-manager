package mm.expenses.manager.order.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum OrderExceptionMessage implements ExceptionType {
    ORDER_CANNOT_BE_CREATED("order-creation-error", "Order cannot be created"),
    ORDER_CANNOT_BE_UPDATED("order-update-error", "Order cannot be updated"),
    ORDER_CANNOT_BE_DELETED("order-delete-error", "Order cannot be deleted"),
    ORDERS_CANNOT_BE_DELETED("orders-delete-error", "Orders cannot be deleted"),
    ORDERS_CANNOT_BE_FOUND("orders-found-error", "Orders cannot be found"),
    ORDER_NOT_FOUND("order-not-found-error", "Order of id '%s' not found."),
    ORDERS_NOT_FOUND("orders-not-found-error", "Orders with ids: '%s' not found."),
    ORDER_NO_UPDATE_DATA("order-no-update-data-passed-error", "Data to update order have no be passed. Nothing to update."),
    ORDER_NOT_ALL_PRODUCTS_FOUND("order-products-not-all-found", "Not all products were found to create ordered products: %s"),
    ORDER_PRODUCTS_CANNOT_BE_EMPTY("order-products-cannot-be-empty", "Order must contains at least 1 product."),
    ORDER_NAME_EMPTY("product-name-empty", "The name of the order cannot be empty."),
    ORDER_NAME_MISSING_OPERATOR("order-missing-operator-for-name", "Missing operator for orders filtering by name."),
    ORDER_PRODUCTS_COUNT_MISSING_OPERATOR("order-missing-operator-for-products-count", "Missing operator for orders filtering by products count."),
    ORDER_PRODUCTS_CUSTOM_PRICE_VALUE_MISSING("order-missing-custom-price-for-products", "Missing price value for ordered product %s."),
    ORDER_PRODUCT_QUANTITY_MUST_BE_GREATER_THAN_ZERO("order-product-quantity-must-be-greater-than-zero", "Ordered product quantity must be greater than 0.0."),
    ORDERED_PRODUCTS_NOT_FOUND("order-products-not-found-error", "Ordered products of ids: '%s' not found.");

    private final String code;
    private final String message;
    private Object[] parameters = null;

    OrderExceptionMessage(ExceptionType exceptionType) {
        this.code = exceptionType.getCode();
        this.message = exceptionType.getMessage();
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return String.format(message, parameters);
    }

    @Override
    public ExceptionType withParameters(final Object... params) {
        if (Objects.nonNull(parameters) && ArrayUtils.isNotEmpty(parameters)) {
            final var tempList = new ArrayList<>(Arrays.asList(parameters));
            tempList.addAll(new ArrayList<>(Arrays.asList(params)));
            parameters = tempList.toArray();
        } else {
            parameters = params;
        }
        return this;
    }

}
