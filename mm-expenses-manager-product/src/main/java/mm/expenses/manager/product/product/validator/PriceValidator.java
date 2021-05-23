package mm.expenses.manager.product.product.validator;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.product.product.dto.request.PriceRequest;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Custom price validator for any requests.
 */
class PriceValidator implements ConstraintValidator<ValidatePrice, PriceRequest> {

    @Override
    public boolean isValid(final PriceRequest request, final ConstraintValidatorContext context) {
        final var isPriceValueValid = isPriceValueValid(request.getValue());
        final var isPriceCurrencyValid = isPriceCurrencyCodeValid(request.getCurrency());

        context.disableDefaultConstraintViolation();

        if (!isPriceValueValid) {
            context.buildConstraintViolationWithTemplate("The price value cannot be less than 0.").addConstraintViolation();
        }
        if (!isPriceCurrencyValid) {
            context.buildConstraintViolationWithTemplate("The price currency code cannot be empty or is unknown.").addConstraintViolation();
        }

        return isPriceValueValid && isPriceCurrencyValid;
    }

    private boolean isPriceValueValid(final BigDecimal value) {
        return Objects.nonNull(value) && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private boolean isPriceCurrencyCodeValid(final String currency) {
        return Objects.nonNull(currency) && CurrencyCode.exists(currency) && !currency.equals(CurrencyCode.UNDEFINED.getCode());
    }

}
