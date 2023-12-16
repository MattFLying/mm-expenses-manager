package mm.expenses.manager.common.beans.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum BeansExceptionMessage implements ExceptionType {
    // conversion
    CANNOT_CONVERT_BIG_DECIMAL_TO_DECIMAL128("conversion-error", "Conversion error."),
    CANNOT_CONVERT_DECIMAL128_TO_BIG_DECIMAL("conversion-error", "Conversion error.");

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
        if (Objects.nonNull(parameters) && parameters.length > 0) {
            final var tempList = new ArrayList<>(Arrays.asList(parameters));
            tempList.addAll(new ArrayList<>(Arrays.asList(params)));
            parameters = tempList.toArray();
        } else {
            parameters = params;
        }
        return this;
    }

}
