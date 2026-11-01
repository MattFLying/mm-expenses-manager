package mm.expenses.manager.account.processor.search;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.account.api.user.model.TextOperationRequest;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.pagination.sort.PostgreSQLSortOrder;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.utils.config.PaginationConfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Filter for querying users.
 */
@SuperBuilder
@Getter
@Setter(value = AccessLevel.PRIVATE)
public class UserFilter extends EntityFilter {

    public static final String USERNAME_PROPERTY = "username";
    public static final String USERNAME_OPERATION_PROPERTY = "usernameOperation";

    public static final String EMAIL_PROPERTY = "email";
    public static final String EMAIL_OPERATION_PROPERTY = "emailOperation";

    private String username;
    private TextOperationRequest usernameOperation;

    private String email;
    private TextOperationRequest emailOperation;

    private PostgreSQLSortOrder sortConfig;

    @Override
    protected void buildSpecificQueryParameters(final Map<String, String[]> queryParameters) {
        buildPagination(queryParameters);
        buildDeleted(queryParameters);

        if (Objects.nonNull(sortConfig)) {
            final var sortOrder = sortConfig.getOrder();
            if (Objects.nonNull(sortOrder)) {
                queryParameters.put(PaginationConfig.SORT, new String[]{String.format("%s%s%s", sortOrder.getProperty(), Operation.SEPARATOR, sortOrder.getDirection().name())});
            }
        }

        if (isUsernameOriented()) {
            queryParameters.put(getUsernameOperationProperty(), new String[]{String.valueOf(getUsername())});
        }
        if (isEmailOriented()) {
            queryParameters.put(getEmailOperationProperty(), new String[]{String.valueOf(getEmail())});
        }
    }

    @Override
    protected void buildDeleted(final Map<String, String[]> queryParameters) {
        final var isDeleted = getIsDeleted();
        if (Objects.nonNull(isDeleted)) {
            queryParameters.put(IS_DELETED_PROPERTY, new String[]{String.valueOf(isDeleted)});
        }
    }

    @Override
    protected List<Boolean> getSpecificFiltersPresenceList() {
        final var isUsernamePresent = Objects.nonNull(username);
        final var isUsernameOperationPresent = Objects.nonNull(usernameOperation);
        final var isEmailPresent = Objects.nonNull(email);
        final var isEmailOperationPresent = Objects.nonNull(emailOperation);

        return List.of(isUsernamePresent, isUsernameOperationPresent, isEmailPresent, isEmailOperationPresent);
    }

    /**
     * @return checks if user's username is not null.
     */
    private boolean isUsernameOriented() {
        final var isUsernamePresent = Objects.nonNull(username);
        final var isUsernameOperationPresent = Objects.nonNull(usernameOperation);
        if (isUsernamePresent && !isUsernameOperationPresent) {
            throw new ApiBadRequestException(UserExceptionMessage.USER_USERNAME_MISSING_OPERATOR);
        }
        return isUsernamePresent && isUsernameOperationPresent;
    }

    /**
     * @return checks if user's email is not null.
     */
    private boolean isEmailOriented() {
        final var isEmailPresent = Objects.nonNull(email);
        final var isEmailOperationPresent = Objects.nonNull(emailOperation);
        if (isEmailPresent && !isEmailOperationPresent) {
            throw new ApiBadRequestException(UserExceptionMessage.USER_EMAIL_MISSING_OPERATOR);
        }
        return isEmailPresent && isEmailOperationPresent;
    }

    private String getUsernameOperationProperty() {
        final var operation = Operation.of(usernameOperation);
        return String.format("%s%s", USERNAME_PROPERTY, operation.getValueWithSeparator());
    }

    private String getEmailOperationProperty() {
        final var operation = Operation.of(emailOperation);
        return String.format("%s%s", EMAIL_PROPERTY, operation.getValueWithSeparator());
    }

}
