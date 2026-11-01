package mm.expenses.manager.account.core.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.account.api.user.model.SortUsersRequest;
import mm.expenses.manager.common.postgresql.pagination.sort.PostgreSQLSortOrder;
import mm.expenses.manager.common.postgresql.pagination.sort.PostgreSQLSortProperty;
import mm.expenses.manager.common.utils.pagination.sort.SortProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Defines sorting for {@link User}.
 */
@Getter
@RequiredArgsConstructor
public enum UserSortOrder implements PostgreSQLSortOrder {
    NAME_ASC(List.of(new PostgreSQLSortProperty("username", Direction.ASC))),
    NAME_DESC(List.of(new PostgreSQLSortProperty("username", Direction.DESC))),

    EMAIL_ASC(List.of(new PostgreSQLSortProperty("email", Direction.ASC))),
    EMAIL_DESC(List.of(new PostgreSQLSortProperty("email", Direction.DESC))),

    CREATED_AT_ASC(List.of(new PostgreSQLSortProperty("createdAt", Direction.ASC))),
    CREATED_AT_DESC(List.of(new PostgreSQLSortProperty("createdAt", Direction.DESC))),

    LAST_MODIFIED_AT_ASC(List.of(new PostgreSQLSortProperty("lastModifiedAt", Direction.ASC))),
    LAST_MODIFIED_AT_DESC(List.of(new PostgreSQLSortProperty("lastModifiedAt", Direction.DESC))),

    DEFAULT_SORT(List.of(new PostgreSQLSortProperty("username", Direction.ASC)));

    private final Collection<SortProperty<Sort.Order>> properties;

    /**
     * @return proper sorting for {@link User} based on passed requested sorting.
     */
    public static PostgreSQLSortOrder of(final SortUsersRequest request) {
        return Objects.isNull(request) ? UserSortOrder.DEFAULT_SORT : switch (request) {
            case USERNAME_ASC -> UserSortOrder.NAME_ASC;
            case USERNAME_DESC -> UserSortOrder.NAME_DESC;
            case EMAIL_ASC -> UserSortOrder.EMAIL_ASC;
            case EMAIL_DESC -> UserSortOrder.EMAIL_DESC;
            case CREATED_AT_ASC -> UserSortOrder.CREATED_AT_ASC;
            case CREATED_AT_DESC -> UserSortOrder.CREATED_AT_DESC;
            case LAST_MODIFIED_AT_ASC -> UserSortOrder.LAST_MODIFIED_AT_ASC;
            case LAST_MODIFIED_AT_DESC -> UserSortOrder.LAST_MODIFIED_AT_DESC;

            // in case if any other request's value is not handled.
            default -> UserSortOrder.DEFAULT_SORT;
        };
    }

}
