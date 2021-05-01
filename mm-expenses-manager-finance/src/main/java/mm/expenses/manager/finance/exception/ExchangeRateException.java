package mm.expenses.manager.finance.exception;

import mm.expenses.manager.exception.EmUncheckedException;
import mm.expenses.manager.exception.ExceptionType;

/**
 * Default exception for any cases of exchange rates.
 */
public class ExchangeRateException extends EmUncheckedException {

    public ExchangeRateException(final ExceptionType exceptionMessage, final Throwable exception) {
        super(exceptionMessage, exception);
    }

}
