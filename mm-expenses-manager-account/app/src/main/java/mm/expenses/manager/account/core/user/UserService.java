package mm.expenses.manager.account.core.user;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.api.user.model.UserPage;
import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.account.processor.AccountHandler;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.processor.ProcessorType;
import mm.expenses.manager.common.web.security.SecurityContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final Map<ProcessorType, AccountHandler> handlers;

    public UserService(final List<AccountHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        AccountHandler::getType,
                        Function.identity()
                ));
    }

    public User findById(final UUID userId, final Boolean isDeleted) {
        final var requestedData = requestOf(userId, isDeleted);
        final var handler = getHandler(AccountHandler.Type.FIND_USER);
        final var result = handler.handle(requestedData);

        return result.mapResponse(User.class);
    }

    public UserResponse findByIdMapped(final UUID userId, final Boolean isDeleted) {
        final var requestedData = requestOf(userId, isDeleted);
        final var handler = getHandler(AccountHandler.Type.FIND_USER);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(UserResponse.class);
    }

    public UserResponse findByKeycloakIdMapped(final SecurityContext context, final UUID keycloakUserId) {
        final var requestedData = requestOf(keycloakUserId, context.isSystemRequest() || context.isSpiRequest(), null);
        final var handler = getHandler(AccountHandler.Type.FIND_USER);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(UserResponse.class);
    }

    public UserPage search(final EntityFilter queryFilter) {
        final var requestedData = requestOf(queryFilter);
        final var handler = getHandler(AccountHandler.Type.SEARCH_USERS);
        final var result = handler.handleDecorated(requestedData);

        return ((AccountHandler.Response) result).getDecoratedPagedResponse();
    }

    public User create(final CreateUserModel request) {
        final var requestedData = requestOf(request);
        final var handler = getHandler(AccountHandler.Type.CREATE_USER);
        final var result = handler.handle(requestedData);

        return result.mapResponse(User.class);
    }

    public UserResponse createDecorated(final CreateUserModel request) {
        final var requestedData = requestOf(request);
        final var handler = getHandler(AccountHandler.Type.CREATE_USER);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(UserResponse.class);
    }

    public AccountHandler.Request requestOf(final Object request) {
        return AccountHandler.Request.builder().request(request).build();
    }

    private AccountHandler getHandler(final AccountHandler.Type type) {
        try {
            if (Objects.isNull(type)) {
                throw new IllegalArgumentException("Cannot recognize type of user handler");
            }
            return handlers.get(type);
        } catch (final NullPointerException exception) {
            throw new IllegalArgumentException(String.format("Cannot recognize user handler of %s type", type));
        }
    }

    private AccountHandler.Request requestOf(final UUID userId, final Boolean isDeleted) {
        return requestOf(userId, false, isDeleted);
    }

    private AccountHandler.Request requestOf(final UUID userId, final Boolean isSystemCall, final Boolean isDeleted) {
        return AccountHandler.Request.builder().id(userId).isSystemCall(isSystemCall).isDeleted(isDeleted).build();
    }

}
