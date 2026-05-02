package mm.expenses.manager.product.processor.conversion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPrice;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;

import java.util.List;
import java.util.Map;

/**
 * Step in chain of currencies conversion for {@link Product}s with missing currencies.
 * Asynchronous update operation for products with updated prices that had previously missing price currencies.
 */
@Slf4j
@RequiredArgsConstructor
final class AsyncProductsPricesUpdated extends ProductsCurrenciesConversionChain<Map<Product, List<ProductPrice>>, Void> {

    private final ProductAsyncHandler asyncHandler;

    @Override
    public Void handleRequest(final Map<Product, List<ProductPrice>> updated) {
        try {
            updated.forEach((product, prices) -> asyncHandler.sendProductPricesMessages(product, prices, AsyncKafkaOperation.UPDATE));

            if (hasNext()) {
                return next.handleRequest(updated);
            }
            return null;
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products prices conversion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_PRICES_CANNOT_BE_CONVERTED, exception);
        }
    }

}
