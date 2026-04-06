package mm.expenses.manager.order.processor.search;

import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.processor.Order;
import org.springframework.data.domain.Page;

/**
 * Default abstraction to build chains of responsibility for {@link Order} deletion process.
 */
abstract sealed class SearchOrdersChain extends ChainCommandExecution<EntityFilter, Page<Order>> permits SearchOrdersPrepareSpecificationCriterias, SearchOrdersByCriteria{

}
