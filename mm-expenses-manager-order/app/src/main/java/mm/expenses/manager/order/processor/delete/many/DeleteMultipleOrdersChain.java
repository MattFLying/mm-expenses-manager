package mm.expenses.manager.order.processor.delete.many;

import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.processor.GeneralSecuredOrderChain;

import java.util.List;

/**
 * Default abstraction to build chains of responsibility for {@link Order}s deletion process.
 */
abstract sealed class DeleteMultipleOrdersChain<Request> extends GeneralSecuredOrderChain<Request, List<Order>> permits FindOrdersToRemove, DeleteMultipleOrders, SaveDeletedOrders {

    DeleteMultipleOrdersChain(final OrderRepository repository) {
        super(repository);

        this.ownPermission = Permission.deleteOwnOrder;
        this.fullPermission = Permission.deleteOrder;
    }

}
