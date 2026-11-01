package mm.expenses.manager.account.processor.find;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserMapper;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.account.processor.AccountHandler;
import mm.expenses.manager.account.processor.decorator.UserClassicMapperToResponse;
import mm.expenses.manager.account.processor.UserDetailsExtractor;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for finding single user by its id.
 */
@Slf4j
@Component
class FindUserHandler extends AccountHandler {

    private final UserDetailsExtractor detailsExtractor;

    FindUserHandler(final UserRepository repository, final UserMapper mapper) {
        super(repository, mapper);
        this.detailsExtractor = new UserDetailsExtractor();
    }

    @Override
    public Type getType() {
        return Type.FIND_USER;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        if (request instanceof AccountHandler.Request findRequest) {
            final var chain = new FindUserById(repository, findRequest.isDeleted());
            return Response.builder()
                    .response(chain.handleRequest(findRequest.getId()))
                    .build();
        }
        throw new ProcessorHandler.ProcessorHandlerException(UserExceptionMessage.USER_NOT_FOUND.withParameters(request.getId()).getMessage());
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof AccountHandler.Request findRequest) {
                final var response = handle(findRequest);
                final var decorator = new UserClassicMapperToResponse(mapper, findRequest.isSystemCall());

                if (response instanceof AccountHandler.Response user) {
                    final var userResponse = user.mapResponse(User.class);
                    final var decorated = decorator.decorate(userResponse);
                    final var result = mapToSimpleUserResponse(decorated, findRequest.isSystemCall());
                    return Response.builder()
                            .response(userResponse)
                            .decoratedResponse(result)
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(UserExceptionMessage.USER_NOT_FOUND.withParameters(request.getId()).getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown find single user error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USER_NOT_FOUND.withParameters(request.getId()), exception);
        }
    }

    private UserResponse mapToSimpleUserResponse(final UserResponse userResponse, final boolean isSystemCall) {
        if (!isSystemCall) {
            if (MapUtils.isNotEmpty(userResponse.getDetails())) {
                detailsExtractor.extractUserResponseDetails(userResponse);
            }
        }
        return userResponse;
    }

}
