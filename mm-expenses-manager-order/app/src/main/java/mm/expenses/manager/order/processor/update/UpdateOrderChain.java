package mm.expenses.manager.order.processor.update;

import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.processor.GeneralSecuredOrderChain;

/**
 * Default abstraction to build chains of responsibility for {@link Order} update process.
 */
abstract sealed class UpdateOrderChain extends GeneralSecuredOrderChain<UpdateOrderRequest, Order> permits ValidateRequestedOrderUpdates, FindOrderToUpdate, UpdateBasicOrderData, ModifyOrderedProductsDuringOrderUpdate, OrderSummaryUpdateDuringUpdate, SaveUpdatedOrder {

    UpdateOrderChain(final OrderRepository repository) {
        super(repository);

        this.ownPermission = Permission.updateOwnOrder;
        this.fullPermission = Permission.updateOrder;
    }

}
