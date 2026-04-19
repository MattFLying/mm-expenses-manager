package mm.expenses.manager.product.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import mm.expenses.manager.product.ProductsExceptionMessage;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum ProductExceptionMessage implements ExceptionType {
    PRODUCT_CANNOT_BE_CREATED("product-creation-error", "Product cannot be created"),
    PRODUCT_CANNOT_BE_UPDATED("product-update-error", "Product cannot be updated"),
    PRODUCT_CANNOT_BE_DELETED("product-delete-error", "Product cannot be deleted"),
    PRODUCTS_CANNOT_BE_DELETED("products-delete-error", "Products cannot be deleted"),
    PRODUCTS_CANNOT_BE_PERMANENTLY_DELETED("products-hard-delete-error", "Products cannot be permanently deleted"),
    PRODUCTS_PRICES_CANNOT_BE_CONVERTED("products-prices-conversion-error", "Products prices cannot be converted"),
    PRODUCT_CANNOT_BE_FOUND("product-found-error", "Product cannot be found"),
    PRODUCTS_CANNOT_BE_FOUND("products-found-error", "Products cannot be found"),

    // price
    PRICE_VALUE_MISSING_OPERATOR("product-price-missing-operator-for-products-price-value", "Missing operator for products price filtering by products price value."),
    PRICE_CURRENCY_MISSING_OPERATOR("product-price-missing-operator-for-products-price-currency", "Missing operator for products price filtering by products price currency."),

    // product
    PRODUCT_NOT_FOUND("product-not-found-error", "Product of id '%s' not found."),
    PRODUCTS_NOT_FOUND("products-not-found-error", "Products with ids: '%s' not found."),
    PRODUCT_NAME_NOT_VALID(ProductsExceptionMessage.PRODUCT_NAME_EMPTY),
    PRODUCT_PRICE_ORIGINAL_NOT_FOUND(ProductsExceptionMessage.PRODUCT_PRICE_ORIGINAL_MISSING),
    PRODUCT_PRICE_CURRENCY_NOT_VALID(ProductsExceptionMessage.PRODUCT_PRICE_CURRENCY_UNDEFINED),
    PRODUCT_PRICE_VALUE_NOT_VALID(ProductsExceptionMessage.PRODUCT_PRICE_VALUE_UNDEFINED),
    PRODUCT_NO_UPDATE_DATA("product-no-update-data-passed-error", "Data to update product have no be passed. Nothing to update."),
    PRODUCT_NAME_MISSING_OPERATOR("product-missing-operator-for-name", "Missing operator for product filtering by name.");

    private final String code;
    private final String message;
    private Object[] parameters = null;

    ProductExceptionMessage(ExceptionType exceptionType) {
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
