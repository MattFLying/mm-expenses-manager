package mm.expenses.manager.product.processor.create;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.core.Product;

/**
 * Default abstraction to build chains of responsibility for {@link Product} creation process.
 */
abstract sealed class CreateProductChain extends ChainCommandExecution<CreateProductRequest, Product> permits PrepareProductDetails, PrepareProductPrice, SaveCreatedProduct, AsyncProductCreation {

}
