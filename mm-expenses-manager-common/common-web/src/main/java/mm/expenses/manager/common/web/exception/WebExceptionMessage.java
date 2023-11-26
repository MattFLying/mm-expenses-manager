package mm.expenses.manager.common.web.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages related with web.
 */
@RequiredArgsConstructor
public enum WebExceptionMessage implements ExceptionType {
    METHOD_NOT_ALLOWED("http-method-not-allowed", "Method '%s' is not allowed."),
    REQUIRED_REQUEST_PROCESSOR_AND_CONTEXT("request-processor-context-required", "Request processor and context are required but %s"),
    HTTP_METHOD_NULL("http-method-is-null", "Http method cannot be null.");

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
