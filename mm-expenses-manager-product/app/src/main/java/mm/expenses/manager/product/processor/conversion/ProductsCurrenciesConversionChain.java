package mm.expenses.manager.product.processor.conversion;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.core.Product;

/**
 * Default abstraction to build chains of responsibility for {@link Product}s deletion process.
 */
abstract sealed class ProductsCurrenciesConversionChain<Request, Response> extends ChainCommandExecution<Request, Response> permits FindProductsWithMissingPriceCurrencies, UpdateConvertedPrices, AsyncProductsPricesUpdated {

}
