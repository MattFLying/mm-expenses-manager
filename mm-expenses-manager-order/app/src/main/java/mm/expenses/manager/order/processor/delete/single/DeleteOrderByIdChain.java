package mm.expenses.manager.order.processor.delete.single;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.core.Order;

import java.util.UUID;

/**
 * Default abstraction to build chains of responsibility for {@link Order} deletion process.
 */
abstract sealed class DeleteOrderByIdChain extends ChainCommandExecution<UUID, Order> permits FindOrderToRemove, DeleteSingleOrder, SaveDeletedOrder{

}
