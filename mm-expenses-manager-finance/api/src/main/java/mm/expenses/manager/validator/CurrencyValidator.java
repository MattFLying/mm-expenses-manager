package mm.expenses.manager.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import mm.expenses.manager.finance.currency.CurrencyCommonValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static mm.expenses.manager.finance.currency.CurrencyCommonValidation.handleCurrentCurrency;
import static mm.expenses.manager.finance.currency.CurrencyCommonValidation.handleUndefined;

/**
 * Custom currency validator for any requests.
 */
@Component
class CurrencyValidator implements ConstraintValidator<ValidateCurrency, String> {

    @Autowired
    private CurrencyCommonValidation currencyCommonValidation;

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        final var currencyCode = CurrencyCommonValidation.getCurrencyFromString(currency);

        final var isUndefined = CurrencyCommonValidation.isUndefined(currencyCode);
        final var isCurrent = currencyCommonValidation.isCurrent(currencyCode);

        context.disableDefaultConstraintViolation();
        handleUndefined(isUndefined, context);
        handleCurrentCurrency(isCurrent, context);

        return !isUndefined && !isCurrent;
    }

}
