package mm.expenses.manager.product.processor.find;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.processor.Product;

import java.util.UUID;

/**
 * Default abstraction to build chains of responsibility for {@link Product} single find process.
 */
abstract sealed class FindProductByIdChain extends ChainCommandExecution<UUID, Product> permits FindProductById {

}
