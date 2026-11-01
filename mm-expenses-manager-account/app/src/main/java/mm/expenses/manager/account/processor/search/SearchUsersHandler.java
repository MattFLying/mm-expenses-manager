package mm.expenses.manager.account.processor.search;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.account.api.user.model.UserPage;
import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserMapper;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.account.processor.AccountHandler;
import mm.expenses.manager.account.processor.UserDetailsExtractor;
import mm.expenses.manager.account.processor.decorator.UserClassicMapperToResponse;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * Handler responsible for search users.
 */
@Slf4j
@Component
class SearchUsersHandler extends AccountHandler {

    private final UserSpecificationHandler specificationHandler;
    private final UserDetailsExtractor detailsExtractor;

    SearchUsersHandler(final UserRepository repository, final UserMapper mapper, final UserSpecificationHandler specificationHandler) {
        super(repository, mapper);
        this.specificationHandler = specificationHandler;
        this.detailsExtractor = new UserDetailsExtractor();
    }

    @Override
    public Type getType() {
        return Type.SEARCH_USERS;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var chain = ChainCommandExecution.build(
                new SearchUsersPrepareSpecificationCriterias(repository, specificationHandler),
                new SearchUsersByCriteria(repository)
        );
        return Response.builder()
                .pagedResponse((Page<User>) chain.handleRequest(request.getRequest()))
                .build();
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof AccountHandler.Request searchRequest) {
                final var response = handle(searchRequest);
                final var decorator = new UserClassicMapperToResponse(mapper);

                if (response instanceof AccountHandler.Response user) {
                    final var pagedUsers = user.getPagedResponse();
                    final var decorated = decorator.decorate(pagedUsers);
                    final var result = mapToSimpleUserPage(decorated);
                    return Response.builder()
                            .pagedResponse(pagedUsers)
                            .decoratedPagedResponse(result)
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(UserExceptionMessage.USERS_CANNOT_BE_FOUND.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown users search error occurred.", exception);
            throw new ApiConflictException(UserExceptionMessage.USERS_CANNOT_BE_FOUND, exception);
        }
    }

    private UserPage mapToSimpleUserPage(final Page<UserResponse> decoratedPage) {
        final var userPage = mapper.mapToPageResponse(decoratedPage);

        if (!userPage.getContent().isEmpty()) {
            userPage.getContent()
                    .forEach(detailsExtractor::extractUserResponseDetails);
        }
        return userPage;
    }

}
