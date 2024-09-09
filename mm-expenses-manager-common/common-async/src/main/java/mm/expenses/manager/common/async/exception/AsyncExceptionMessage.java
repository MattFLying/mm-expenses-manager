package mm.expenses.manager.common.async.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum AsyncExceptionMessage implements ExceptionType {
    ASYNC_CONSUMER_MESSAGE_IS_NULL("async-consumer-binding-error", "Consumer binding message is null."),
    ASYNC_CONSUMER_BINDING_OR_TOPIC_IS_NULL("async-consumer-binding-error", "Consumer binding or topic is null what is not allowed. Binding: %s. Topic: %s.");

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
