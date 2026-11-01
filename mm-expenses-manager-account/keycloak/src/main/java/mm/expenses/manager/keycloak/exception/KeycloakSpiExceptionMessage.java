package mm.expenses.manager.keycloak.exception;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * Provides all available exceptions codes and messages.
 */
@RequiredArgsConstructor
public enum KeycloakSpiExceptionMessage implements ExceptionType {
    USER_DOES_NOT_EXISTS_IN_KEYCLOAK("user-does-not-exists-in-keycloak-error", "User does not exists in Keycloak or ID is missing"),
    USER_CANNOT_BE_DOWNLOADED_INTERNALLY("user-cannot-be-downloaded-internally-error", "Cannot fetch user to process permissions"),

    KAFKA_TOPIC_MISSING("kafka-topic-missing-error", "Kafka topic is missing"),
    KAFKA_MESSAGE_MISSING("kafka-message-missing-error", "Kafka message is missing"),
    KAFKA_MESSAGE_JSON_MAPPING("kafka-message-json-mapping-error", "Kafka message cannot be mapped into JSON value"),

    HTTP_REQUEST_MISSING("http-request-missing-error", "HTTP request is missing"),
    KEYCLOAK_EVENT_JSON_MAPPING("keycloak-event-json-mapping-error", "Keycloak event cannot be mapped into JSON value"),
    REQUEST_SENDER_UNAVAILABLE("request-sender-unavailable", "Request sender is unavailable"),

    UNKNOWN_ERROR("unknown-error", "Unknown error occurred");

    private final String code;
    private final String message;
    private Object[] parameters = null;

    KeycloakSpiExceptionMessage(final ExceptionType exceptionType) {
        this.code = exceptionType.getCode();
        this.message = exceptionType.getMessage();
    }

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
