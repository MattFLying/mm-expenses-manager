package mm.expenses.manager.account.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.account.api.user.model.UserPage;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserMapper;
import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.common.utils.processor.ProcessorType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;

/**
 * Default handler definition to process specific handlers.
 */
@RequiredArgsConstructor
public abstract class AccountHandler extends ProcessorHandler {

    protected final UserRepository repository;
    protected final UserMapper mapper;

    @SuperBuilder
    public static class Request extends ProcessorHandler.Request {

        private Boolean isDeleted;

        private Boolean isSystemCall;

        public boolean isDeleted() {
            return Objects.nonNull(isDeleted) && isDeleted;
        }

        public boolean isSystemCall() {
            return Objects.nonNull(isSystemCall) && isSystemCall;
        }

    }

    @Getter
    @SuperBuilder
    public static class Response extends ProcessorHandler.Response {

        private Page<User> pagedResponse;

        private List<User> listedResponse;

        private UserPage decoratedPagedResponse;

    }

    /**
     * Possible handler types to be implemented and to be used.
     */
    public enum Type implements ProcessorType {
        CREATE_USER,
        FIND_USER,
        SEARCH_USERS
    }

}
