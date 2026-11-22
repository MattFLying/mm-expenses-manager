package mm.expenses.manager.order.processor.search;

import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.processor.GeneralSecuredOrderChain;
import org.springframework.data.domain.Page;

/**
 * Default abstraction to build chains of responsibility for {@link Order} deletion process.
 */
abstract sealed class SearchOrdersChain extends GeneralSecuredOrderChain<EntityFilter, Page<Order>> permits SearchOrdersPrepareSpecificationCriterias, SearchOrdersByCriteria {

    SearchOrdersChain(final OrderRepository repository) {
        super(repository);

        this.ownPermission = Permission.getOwnOrdersInfo;
        this.fullPermission = Permission.getOrdersInfo;
    }

}
