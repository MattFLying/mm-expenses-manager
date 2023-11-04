package mm.expenses.manager.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public enum ProductsExceptionMessage implements ExceptionType {
    PRODUCT_PRICE_VALUE_LESS_THAN_0("product-price-cannot-be-less-than-0", "The price value cannot be less than 0."),
    PRODUCT_PRICE_CURRENCY_UNDEFINED("product-price-currency-undefined", "The price currency code cannot be empty or is unknown."),
    PRODUCT_NAME_EMPTY("product-name-empty", "The name of the product cannot be empty."),
    PRODUCT_DETAILS_INVALID("product-details-invalid", "Details of the product is not correct.");

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
