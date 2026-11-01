package mm.expenses.manager.account.processor.create;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserMapper;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.account.processor.AccountHandler;
import mm.expenses.manager.account.processor.decorator.UserClassicMapperToResponse;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Handler responsible for user creation.
 */
@Slf4j
@Component
class CreateUserHandler extends AccountHandler {

    CreateUserHandler(final UserRepository repository, final UserMapper mapper) {
        super(repository, mapper);
    }

    @Override
    public Type getType() {
        return Type.CREATE_USER;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new FindRegisteredUser(repository, now),
                new MapRegisteredUser(repository, now),
                new InitializeUserPermissionsUser(repository),
                new SaveCreatedUser(repository)
        );
        return Response.builder()
                .response(chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof AccountHandler.Request createRequest) {
                final var response = handle(createRequest);
                final var decorator = new UserClassicMapperToResponse(mapper);

                if (response instanceof AccountHandler.Response userResponse) {
                    final var user = userResponse.mapResponse(User.class);
                    return Response.builder()
                            .response(user)
                            .decoratedResponse(decorator.decorate(user))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(UserExceptionMessage.USER_CANNOT_BE_CREATED.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown user creation error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_CANNOT_BE_CREATED, exception);
        }
    }

}
