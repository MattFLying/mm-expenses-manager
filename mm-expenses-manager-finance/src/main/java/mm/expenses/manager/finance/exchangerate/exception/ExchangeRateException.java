package mm.expenses.manager.finance.exchangerate.exception;

public class ExchangeRateException extends RuntimeException {

    public ExchangeRateException(final String message, final Throwable exception) {
        super(message, exception);
    }

}
