package mm.expenses.manager.product.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Custom product validator for any requests.
 */
public class ProductValidator implements ConstraintValidator<ValidateProduct, Object> {

    @Override
    public boolean isValid(Object request, ConstraintValidatorContext context) {
        if (request instanceof CreateProductRequest newRequest) {
            return isNewProductRequestValid(newRequest, context);
        }
        if (request instanceof UpdateProductRequest updateRequest) {
            return isUpdateProductRequestValid(updateRequest, context);
        }
        return false;
    }

    public static boolean isNewProductRequestValid(final CreateProductRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }
        final var isNameValid = isProductNameNotEmpty(request.getName());
        final var isDetailsValid = isNewProductDetailsValid(request.getDetails());

        final var priceRequest = request.getPrice();
        final var isPriceValueValid = isPriceValueValid(priceRequest.getValue());
        final var isPriceCurrencyValid = isPriceCurrencyCodeValid(priceRequest.getCurrency());

        context.disableDefaultConstraintViolation();
        if (!isNameValid) {
            context.buildConstraintViolationWithTemplate("The name of the product cannot be empty.").addConstraintViolation();
        }
        if (!isDetailsValid) {
            context.buildConstraintViolationWithTemplate("Details of the product is not correct.").addConstraintViolation();
        }
        if (!isPriceValueValid) {
            context.buildConstraintViolationWithTemplate("The price value cannot be less than 0.").addConstraintViolation();
        }
        if (!isPriceCurrencyValid) {
            context.buildConstraintViolationWithTemplate("The price currency code cannot be empty or is unknown.").addConstraintViolation();
        }

        return isNameValid && isDetailsValid && isPriceValueValid && isPriceCurrencyValid;
    }

    public static boolean isUpdateProductRequestValid(final UpdateProductRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }
        context.disableDefaultConstraintViolation();

        var isNameValid = true;
        var isPriceValid = true;

        if (request.getName() != null) {
            isNameValid = isProductNameNotEmpty(request.getName());
            if (!isNameValid) {
                context.buildConstraintViolationWithTemplate("The name of the product cannot be empty.").addConstraintViolation();
            }
        }

        if (request.getPrice() != null) {
            isPriceValid = isPriceValid(request.getPrice().getValue(), request.getPrice().getCurrency(), context);
        }

        return Stream.of(isNameValid, isPriceValid).anyMatch(x -> x);
    }

    public static boolean isPriceValid(final BigDecimal value, final String currency, final ConstraintValidatorContext context) {
        var isPriceValueValid = true;
        var isPriceCurrencyValid = true;

        if (value != null) {
            isPriceValueValid = isPriceValueValid(value);
            if (!isPriceValueValid) {
                context.buildConstraintViolationWithTemplate("The price value cannot be less than 0.").addConstraintViolation();
            }
        }

        if (currency != null) {
            isPriceCurrencyValid = isPriceCurrencyCodeValid(currency);
            if (!isPriceCurrencyValid) {
                context.buildConstraintViolationWithTemplate("The price currency code cannot be empty or is unknown.").addConstraintViolation();
            }
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
