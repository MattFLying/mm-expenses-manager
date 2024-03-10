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
    ORDER_NOT_FOUND("order-not-found-error", "Order of id '%s' not found."),
    ORDERS_NOT_FOUND("orders-not-found-error", "Orders with ids: '%s' not found."),
    ORDER_NO_UPDATE_DATA("order-no-update-data-passed-error", "Data to update order have no be passed. Nothing to update."),
    ORDER_NOT_ALL_PRODUCTS_FOUND("order-products-not-all-found", "Not all products were found to create ordered products: %s"),
    ORDER_PRODUCTS_CANNOT_BE_EMPTY("order-products-cannot-be-empty", "Order must contains at least 1 product."),
    ORDER_PRODUCT_QUANTITY_MUST_BE_GREATER_THAN_ZERO("order-product-quantity-must-be-greater-than-zero", "Ordered product quantity must be greater than 0.0."),
    PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED("page-size-and-page-number-must-be-passed-error", "Both page number and page size must be filled"),
    PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE("order-price-summary-can-be-less-or-grater-at-once-error", "Price summary can be less or greater, not both."),
    PRICE_MUST_BE_LESS_THAN_OR_GREATER_THAN("order-price-summary-must-be-less-or-grater-error", "Price summary must be less or greater."),
    PRODUCTS_COUNT_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE("products-count-price-summary-can-be-less-or-grater-at-once-error", "Products count can be less or greater, not both.");

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
