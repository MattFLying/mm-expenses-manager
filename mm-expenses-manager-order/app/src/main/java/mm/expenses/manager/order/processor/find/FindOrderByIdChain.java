package mm.expenses.manager.order.processor.find;

import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.processor.GeneralSecuredOrderChain;

import java.util.UUID;

/**
 * Default abstraction to build chains of responsibility for {@link Order} single find process.
 */
abstract sealed class FindOrderByIdChain extends GeneralSecuredOrderChain<UUID, Order> permits FindOrderById {

    FindOrderByIdChain(final OrderRepository repository) {
        super(repository);

        this.ownPermission = Permission.getOwnOrderInfo;
        this.fullPermission = Permission.getOrderInfo;
    }

}
