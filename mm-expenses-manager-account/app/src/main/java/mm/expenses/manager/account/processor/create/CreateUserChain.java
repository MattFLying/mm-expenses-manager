package mm.expenses.manager.account.processor.create;

import mm.expenses.manager.account.core.user.CreateUserModel;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;

/**
 * Default abstraction to build chains of responsibility for {@link User} creation process.
 */
abstract sealed class CreateUserChain extends ChainCommandExecution<CreateUserModel, User> permits FindRegisteredUser, MapRegisteredUser, InitializeUserPermissionsUser, SaveCreatedUser {

}
