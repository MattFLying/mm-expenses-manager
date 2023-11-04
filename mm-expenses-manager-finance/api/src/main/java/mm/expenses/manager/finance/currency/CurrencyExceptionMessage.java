package mm.expenses.manager.finance.currency;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public enum CurrencyExceptionMessage implements ExceptionType {
    CURRENCY_NOT_ALLOWED("currency-not-available-error", "Currency is not available."),
    DEFAULT_CURRENCY_NOT_ALLOWED("currency-not-allowed-error", "Currency is default currency, cannot be used.");

    private final String code;
    private final String message;
    private Object[] parameters = null;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return String.format(message, parameters);
    }

    @Override
    public ExceptionType withParameters(final Object... params) {
        if (Objects.nonNull(parameters) && ArrayUtils.isNotEmpty(parameters)) {
            final var tempList = new ArrayList<>(Arrays.asList(parameters));
            tempList.addAll(new ArrayList<>(Arrays.asList(params)));
            parameters = tempList.toArray();
        } else {
            parameters = params;
        }
        return this;
    }

}
