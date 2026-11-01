package mm.expenses.manager.account.processor.search;

import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import org.springframework.data.domain.Page;

/**
 * Default abstraction to build chains of responsibility for {@link User} search process.
 */
abstract sealed class SearchUsersChain extends ChainCommandExecution<EntityFilter, Page<User>> permits SearchUsersPrepareSpecificationCriterias, SearchUsersByCriteria {

}
