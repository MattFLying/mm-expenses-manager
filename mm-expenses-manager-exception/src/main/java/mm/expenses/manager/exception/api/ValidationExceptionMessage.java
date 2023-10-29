package mm.expenses.manager.exception.api;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum ValidationExceptionMessage implements ExceptionType {
    VALIDATION_EXCEPTION("validation-exception", "So...?");

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
