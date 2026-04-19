package mm.expenses.manager.product.processor.delete.single;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.processor.Product;

import java.util.UUID;

/**
 * Default abstraction to build chains of responsibility for multiple {@link Product}s deletion process.
 */
abstract sealed class DeleteProductByIdChain extends ChainCommandExecution<UUID, Product> permits FindProductToRemove, DeleteSingleProduct, SaveDeletedProduct, AsyncProductDelete {

}
