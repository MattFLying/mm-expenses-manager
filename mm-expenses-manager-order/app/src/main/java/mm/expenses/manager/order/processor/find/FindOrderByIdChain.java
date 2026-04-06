package mm.expenses.manager.order.processor.find;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.processor.Order;

import java.util.UUID;

/**
 * Default abstraction to build chains of responsibility for {@link Order} single find process.
 */
abstract sealed class FindOrderByIdChain extends ChainCommandExecution<UUID, Order> permits FindOrderById{

}
