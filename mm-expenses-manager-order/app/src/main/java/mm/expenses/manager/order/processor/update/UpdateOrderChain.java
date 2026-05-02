package mm.expenses.manager.order.processor.update;

import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.core.Order;

/**
 * Default abstraction to build chains of responsibility for {@link Order} update process.
 */
abstract sealed class UpdateOrderChain extends ChainCommandExecution<UpdateOrderRequest, Order> permits ValidateRequestedOrderUpdates, FindOrderToUpdate, UpdateBasicOrderData, ModifyOrderedProductsDuringOrderUpdate, OrderSummaryUpdateDuringUpdate, SaveUpdatedOrder {

}
