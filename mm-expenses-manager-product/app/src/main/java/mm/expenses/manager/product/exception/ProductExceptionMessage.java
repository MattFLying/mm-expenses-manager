package mm.expenses.manager.product.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum ProductExceptionMessage implements ExceptionType {
    // price
    PRICE_AND_PRICE_RANGE_NOT_ALLOWED("product-price-and-price-range-passed-error", "Only price or price range can be used."),
    PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE("product-price-can-be-less-or-grater-at-once-error", "Price can be less or greater, not both."),
    PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE("product-price-less-or-greater-not-allowed-for-price-range-error", "Price range cannot be set as less or greater than."),
    PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED("product-price-max-and-price-min-must-be-passed-error", "Both price min and price max must be passed. Price min: {}, price max: {}"),

    // product
    PRODUCT_NOT_FOUND("product-not-found-error", "Product of id '%s' not found."),
    PRODUCT_NAME_NOT_VALID("product-name-not-valid-error", "The name of product: '%s' is not valid."),
    PRODUCT_PRICE_CURRENCY_NOT_VALID("product-price-currency-not-valid-error", "The price currency of product is not valid."),
    PRODUCT_PRICE_VALUE_NOT_VALID("product-price-value-not-valid-error", "The price value of product: '%s' is not valid."),
    PRODUCT_DETAILS_NOT_VALID("product-details-not-valid-error", "The details of product: '%s' is not valid."),
    PRODUCT_NO_UPDATE_DATA("product-no-update-data-passed-error", "Data to update product have no be passed. Nothing to update."),

    // rest
    PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED("page-size-and-page-number-must-be-passed-error", "Both page number and page size must be filled"),

    // conversion
    CANNOT_CONVERT_BIG_DECIMAL_TO_DECIMAL128("conversion-error", "Conversion error."),
    CANNOT_CONVERT_DECIMAL128_TO_BIG_DECIMAL("conversion-error", "Conversion error.");

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
