package mm.expenses.manager.order.processor.create;

import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.core.Order;

/**
 * Default abstraction to build chains of responsibility for {@link Order} creation process.
 */
abstract sealed class CreateOrderChain extends ChainCommandExecution<CreateNewOrderRequest, Order> permits FindOrderedProducts, MapOrderedProducts, CalculatePrices, SaveCreatedOrder {

}
