package mm.expenses.manager.order.product.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductExceptionCode {
    NOT_FOUND("product-not-found"),
    NEW_PRODUCT_VALIDATION("product-new-validation"),
    UPDATE_PRODUCT_VALIDATION("product-update-validation"),
    PRODUCT_PRICE_LESS_AND_GREATER("product-price-less-and-greater"),
    PRODUCT_PRICE_LESS_OR_GREATER("product-price-less-or-greater"),
    PRODUCT_FILTERS_INCORRECT("product-filters-incorrect");

    private final String code;

}
