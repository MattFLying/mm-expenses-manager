package mm.expenses.manager.finance.currency;

import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CurrencyCommonValidation {

    private final CurrencyProvider currencyProvider;

    public boolean isCurrent(final CurrencyCode currency) {
        var isCurrent = false;

        final var current = currencyProvider.getCurrentCurrency();
        if (Objects.nonNull(current)) {
            isCurrent = current.equals(currency);
        }
        return isCurrent;
    }

    public static CurrencyCode getCurrencyFromString(final String currency) {
        return CurrencyCode.getCurrencyFromString(currency, false);
    }

    public static boolean isUndefined(final CurrencyCode currency) {
        return currency.equals(CurrencyCode.UNDEFINED);
    }

    public static void handleUndefined(final boolean isUndefined, ConstraintValidatorContext context) {
        if (isUndefined) {
            final var exceptionType = CurrencyExceptionMessage.CURRENCY_NOT_ALLOWED;
            context.buildConstraintViolationWithTemplate(exceptionType.getMessage()).addPropertyNode(exceptionType.getCode()).addConstraintViolation();
        }
    }

    public static void handleCurrentCurrency(final boolean isCurrent, ConstraintValidatorContext context) {
        if (isCurrent) {
            final var exceptionType = CurrencyExceptionMessage.DEFAULT_CURRENCY_NOT_ALLOWED;
            context.buildConstraintViolationWithTemplate(exceptionType.getMessage()).addPropertyNode(exceptionType.getCode()).addConstraintViolation();
        }
    }

}
