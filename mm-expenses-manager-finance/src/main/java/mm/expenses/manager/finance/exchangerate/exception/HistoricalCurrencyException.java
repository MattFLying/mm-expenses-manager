package mm.expenses.manager.finance.exchangerate.exception;

public class HistoricalCurrencyException extends Exception {

    public HistoricalCurrencyException(final String message) {
        super(message);
    }

    public HistoricalCurrencyException(final String message, final Throwable exception) {
        super(message, exception);
    }

}
