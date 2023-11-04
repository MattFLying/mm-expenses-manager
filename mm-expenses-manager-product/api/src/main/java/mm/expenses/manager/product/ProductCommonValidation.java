package mm.expenses.manager.product;

import jakarta.validation.ConstraintValidatorContext;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

public class ProductCommonValidation {

    public static void handlePriceValue(final boolean isLessThanZero, ConstraintValidatorContext context) {
        if (isLessThanZero) {
            final var exceptionType = ProductsExceptionMessage.PRODUCT_PRICE_VALUE_LESS_THAN_0;
            context.buildConstraintViolationWithTemplate(exceptionType.getMessage()).addPropertyNode(exceptionType.getCode()).addConstraintViolation();
        }
    }

    public static void handlePriceCurrency(final boolean isUndefined, ConstraintValidatorContext context) {
        if (isUndefined) {
            final var exceptionType = ProductsExceptionMessage.PRODUCT_PRICE_CURRENCY_UNDEFINED;
            context.buildConstraintViolationWithTemplate(exceptionType.getMessage()).addPropertyNode(exceptionType.getCode()).addConstraintViolation();
        }
    }

    public static void handleProductName(final boolean isNameEmpty, ConstraintValidatorContext context) {
        if (isNameEmpty) {
            final var exceptionType = ProductsExceptionMessage.PRODUCT_NAME_EMPTY;
            context.buildConstraintViolationWithTemplate(exceptionType.getMessage()).addPropertyNode(exceptionType.getCode()).addConstraintViolation();
        }
    }

    public static void handleProductDetails(final boolean isDetailsValid, ConstraintValidatorContext context) {
        if (isDetailsValid) {
            final var exceptionType = ProductsExceptionMessage.PRODUCT_DETAILS_INVALID;
            context.buildConstraintViolationWithTemplate(exceptionType.getMessage()).addPropertyNode(exceptionType.getCode()).addConstraintViolation();
        }
    }

    public static boolean isPriceValid(final BigDecimal value, final String currency, final ConstraintValidatorContext context) {
        var isPriceValueValid = true;
        var isPriceCurrencyValid = true;

        if (value != null) {
            isPriceValueValid = isPriceValueValid(value);
            handlePriceValue(!isPriceValueValid, context);
        }
        if (currency != null) {
            isPriceCurrencyValid = isPriceCurrencyCodeValid(currency);
            handlePriceCurrency(!isPriceCurrencyValid, context);
        }
        return isPriceValueValid && isPriceCurrencyValid;
    }

    public static boolean isProductNameNotEmpty(final String name) {
        return StringUtils.isNotBlank(name);
    }

    public static boolean isPriceValueValid(final BigDecimal value) {
        return Objects.nonNull(value) && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isPriceCurrencyCodeValid(final String currency) {
        return Objects.nonNull(currency) && CurrencyCode.exists(currency) && !currency.equals(CurrencyCode.UNDEFINED.getCode());
    }

    public static boolean isPriceCurrencyCodeValid(final CurrencyCode currency) {
        return Objects.nonNull(currency) && !currency.equals(CurrencyCode.UNDEFINED);
    }

    public static boolean isNameEmpty(final String name) {
        return StringUtils.isBlank(name);
    }

    public static boolean isNewProductDetailsValid(final Map<String, Object> details) {
        return MapUtils.isNotEmpty(details);
    }

}
