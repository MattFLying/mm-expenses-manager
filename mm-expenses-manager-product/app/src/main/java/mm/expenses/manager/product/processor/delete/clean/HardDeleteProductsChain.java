package mm.expenses.manager.product.processor.delete.clean;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.core.Product;

/**
 * Default abstraction to build chains of responsibility for {@link Product}s hard deletion process.
 */
abstract sealed class HardDeleteProductsChain<Request, Response> extends ChainCommandExecution<Request, Response> permits FindDeletedProducts, HardDeleteProducts {

}
