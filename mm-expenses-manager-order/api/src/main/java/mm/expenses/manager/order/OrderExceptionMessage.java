package mm.expenses.manager.order;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public enum OrderExceptionMessage implements ExceptionType {
    ORDER_NAME_EMPTY("order-name-empty", "The name of the order cannot be empty."),
    ORDER_PRODUCTS_EMPTY("order-products-empty", "There is no at least 1 ordered product and order could not be finalized."),
    ORDER_QUANTITY_EMPTY("order-quantity-empty", "Quantity cannot be empty for id: {}"),
    ORDER_QUANTITY_ZERO("order-quantity-zero", "Quantity cannot be zero for id: {}"),
    ORDER_QUANTITY_UNKNOWN_OR_NEGATIVE("order-quantity-unknown-or-negative", "Quantity cannot be unknown or negative for id: {}");

    private final String code;
    private final String message;
    private Object[] parameters = null;

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
