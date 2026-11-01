package mm.expenses.manager.account.async;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.account.async.message.UserRegistrationMessage;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for specific operations on users processed when Kafka events from Keycloak received.
 */
@Service
@RequiredArgsConstructor
class AsyncUserService {

    private final UserService userService;

    @Transactional
    public User createFromEvent(final UserRegistrationMessage request) {
        return userService.create(request);
    }

}
