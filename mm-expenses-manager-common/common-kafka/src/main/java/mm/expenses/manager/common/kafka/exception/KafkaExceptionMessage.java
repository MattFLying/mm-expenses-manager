package mm.expenses.manager.common.kafka.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum KafkaExceptionMessage implements ExceptionType {
    ASYNC_PRODUCER_MESSAGE_SEND_FAILED("async-producer-message-error", "Sending event to binding: %s on topic: %s failed. Body: %s"),
    ASYNC_PRODUCER_MESSAGE_IS_NULL("async-producer-message-error", "Cannot send asynchronous message because the message is null."),
    ASYNC_PRODUCER_BINDING_OR_TOPIC_IS_NULL("async-producer-binding-error", "Producer binding or topic is null what is not allowed. Binding: %s. Topic: %s.");

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
