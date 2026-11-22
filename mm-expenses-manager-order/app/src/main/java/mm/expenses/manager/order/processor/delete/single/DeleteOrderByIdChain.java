package mm.expenses.manager.order.processor.delete.single;

import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.processor.GeneralSecuredOrderChain;

import java.util.UUID;

/**
 * Default abstraction to build chains of responsibility for {@link Order} deletion process.
 */
abstract sealed class DeleteOrderByIdChain extends GeneralSecuredOrderChain<UUID, Order> permits FindOrderToRemove, DeleteSingleOrder, SaveDeletedOrder{

    DeleteOrderByIdChain(final OrderRepository repository) {
        super(repository);

        this.ownPermission = Permission.deleteOwnOrder;
        this.fullPermission = Permission.deleteOrder;
    }

}
