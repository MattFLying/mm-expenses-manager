package mm.expenses.manager.order.processor.delete.many;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.processor.Order;

import java.util.List;

/**
 * Default abstraction to build chains of responsibility for {@link Order}s deletion process.
 */
abstract sealed class DeleteMultipleOrdersChain<Request> extends ChainCommandExecution<Request, List<Order>> permits FindOrdersToRemove, DeleteMultipleOrders, SaveDeletedOrders{

}
