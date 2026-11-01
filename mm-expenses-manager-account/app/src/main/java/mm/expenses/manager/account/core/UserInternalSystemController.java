package mm.expenses.manager.account.core;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.account.api.user.model.*;
import mm.expenses.manager.account.core.user.UserService;
import mm.expenses.manager.account.core.user.UserSortOrder;
import mm.expenses.manager.account.processor.search.UserFilter;
import mm.expenses.manager.common.postgresql.pagination.PaginationHelper;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.web.security.CurrentUserProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/system")
class UserInternalSystemController {

    private final UserService service;
    private final PaginationHelper pagination;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserPage> findAll(@RequestParam(value = PaginationConfig.PAGE_NUMBER, required = false) final Integer pageNumber,
                                     @RequestParam(value = PaginationConfig.PAGE_SIZE, required = false) final Integer pageSize,
                                     @RequestParam(value = PaginationConfig.SORT, required = false) final SortUsersRequest sort,
                                     @RequestParam(value = UserFilter.IS_DELETED_PROPERTY, required = false) final Boolean isDeleted,
                                     @RequestParam(value = UserFilter.USERNAME_PROPERTY, required = false) final String username,
                                     @RequestParam(value = UserFilter.USERNAME_OPERATION_PROPERTY, required = false) final TextOperationRequest usernameOperation,
                                     @RequestParam(value = UserFilter.EMAIL_PROPERTY, required = false) final String email,
                                     @RequestParam(value = UserFilter.EMAIL_OPERATION_PROPERTY, required = false) final TextOperationRequest emailOperation,
                                     @RequestParam(value = UserFilter.GENERAL_QUERY_PROPERTY, required = false) final String query) {

        final var queryFilter = UserFilter.builder()
                .sortConfig(UserSortOrder.of(sort))
                .username(username)
                .usernameOperation(usernameOperation)
                .email(email)
                .emailOperation(emailOperation)
                .query(query)
                .isDeleted(isDeleted)
                .paginationConfig(pagination.getPageRequest(pageNumber, pageSize))
                .build();

        return ResponseEntity.ok(service.search(queryFilter));
    }

    @GetMapping(value = "/users/keycloak/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<UserResponse> findByKeycloakId(@PathVariable("id") final UUID keycloakId) {
        final var context = currentUserProvider.getSecurityContext();
        return ResponseEntity.ok(service.findByKeycloakIdMapped(context, keycloakId));
    }

}
