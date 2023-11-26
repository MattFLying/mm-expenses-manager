package mm.expenses.manager.order.product.exception;

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
public enum ProductExceptionMessage implements ExceptionType {
    PRODUCT_NOT_FOUND("product-not-found-error", "Product of id '%s' not found."),
    NEW_PRODUCT_VALIDATION("product-new-validation", ""),
    UPDATE_PRODUCT_VALIDATION("product-update-validation", ""),
    PRODUCT_PRICE_LESS_AND_GREATER("product-price-less-and-greater", "Price must be less or greater"),
    PRODUCT_PRICE_LESS_OR_GREATER("product-price-less-or-greater", ""),
    PRODUCT_FILTERS_INCORRECT("product-filters-incorrect", "Incorrect filters");

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
