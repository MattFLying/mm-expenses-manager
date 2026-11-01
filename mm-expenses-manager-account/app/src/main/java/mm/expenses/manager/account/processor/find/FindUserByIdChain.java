package mm.expenses.manager.account.processor.find;

import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;

import java.util.UUID;

/**
 * Default abstraction to build chains of responsibility for {@link User} single find process.
 */
abstract sealed class FindUserByIdChain extends ChainCommandExecution<UUID, User> permits FindUserById {

}
