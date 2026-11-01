package mm.expenses.manager.account.processor.decorator;

import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.common.web.decorator.PagedDecorator;

/**
 * Simple {@link User} decorator for specific cases.
 */
public abstract class UserMappingStrategy extends PagedDecorator<User, UserResponse> {

    /**
     * Default method to do any necessary validation in implemented decorator.
     */
    protected void validate() {

    }

}
