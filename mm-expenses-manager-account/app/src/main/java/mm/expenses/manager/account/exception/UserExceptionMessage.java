package mm.expenses.manager.account.exception;

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
public enum UserExceptionMessage implements ExceptionType {
    USER_CANNOT_BE_CREATED("user-creation-error", "User cannot be created"),
    USER_ID_CANNOT_BE_EMPTY("user-id-cannot-be-empty", "User id cannot be empty"),
    USER_ALREADY_EXISTS("user-already-exists", "User already exists"),
    USER_DETAILS_EXTRACTION("user-details-extraction-error", "User details extraction error"),
    USERS_CANNOT_BE_FOUND("users-found-error", "Users cannot be found"),
    USER_NOT_FOUND("user-not-found-error", "User of id '%s' not found."),
    USER_USERNAME_MISSING_OPERATOR("user-missing-operator-for-username", "Missing operator for users filtering by username."),
    USER_EMAIL_MISSING_OPERATOR("user-missing-operator-for-email", "Missing operator for users filtering by email."),
    USER_EMAIL_INVALID("user-email-invalid", "User email is invalid.");

    private final String code;
    private final String message;
    private Object[] parameters = null;

    UserExceptionMessage(ExceptionType exceptionType) {
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
