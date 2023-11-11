package mm.expenses.manager.finance.exception;

import mm.expenses.manager.common.exceptions.base.EmCheckedException;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

/**
 * Exception thrown when something went wrong with update historical exchange rates process.
 */
public class HistoricalCurrencyException extends EmCheckedException {

    public HistoricalCurrencyException(final ExceptionType exceptionMessage) {
        super(exceptionMessage);
    }

    public HistoricalCurrencyException(final ExceptionType exceptionMessage, final Throwable exception) {
        super(exceptionMessage, exception);
    }

}
