package mm.expenses.manager.product.processor.delete.many;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.processor.Product;

import java.util.List;

/**
 * Default abstraction to build chains of responsibility for {@link Product}s deletion process.
 */
abstract sealed class DeleteMultipleProductsChain<Request> extends ChainCommandExecution<Request, List<Product>> permits FindProductsToRemove, DeleteMultipleProducts, SaveDeletedProducts, AsyncProductsDelete {

}
