package mm.expenses.manager.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import mm.expenses.manager.finance.currency.CurrencyCommonValidation;

import static mm.expenses.manager.finance.currency.CurrencyCommonValidation.handleUndefined;

/**
 * Custom currency validator for any requests related with currency conversion.
 */
class CurrencyCalculationValidator implements ConstraintValidator<ValidateCurrencyCalculation, String> {

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        final var currencyCode = CurrencyCommonValidation.getCurrencyFromString(currency);

        final var isUndefined = CurrencyCommonValidation.isUndefined(currencyCode);

        context.disableDefaultConstraintViolation();
        handleUndefined(isUndefined, context);

        return !isUndefined;
    }

}
