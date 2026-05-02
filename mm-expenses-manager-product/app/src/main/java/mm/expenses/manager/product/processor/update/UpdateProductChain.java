package mm.expenses.manager.product.processor.update;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.core.Product;

/**
 * Default abstraction to build chains of responsibility for {@link Product} update process.
 */
abstract sealed class UpdateProductChain extends ChainCommandExecution<UpdateProductRequest, Product> permits ValidateRequestedProductUpdates, FindProductToUpdate, UpdateProductData, SaveUpdatedProduct, AsyncProductUpdate {

}
