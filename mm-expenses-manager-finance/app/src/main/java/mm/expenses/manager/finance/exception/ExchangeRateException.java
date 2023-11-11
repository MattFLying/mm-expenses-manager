package mm.expenses.manager.finance.exception;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

/**
 * Default exception for any cases of exchange rates.
 */
public class ExchangeRateException extends EmUncheckedException {

    public ExchangeRateException(final ExceptionType exceptionMessage, final Throwable exception) {
        super(exceptionMessage, exception);
    }

}
