package mm.expenses.manager.product.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPriceService;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductRepository;

import java.time.Instant;

/**
 * Step in chain of new {@link Product} creation.
 * Prepares new product price.
 */
@Slf4j
@RequiredArgsConstructor
final class PrepareProductPrice extends CreateProductChain {

    final static String PRODUCT_PRICE_KEY = "price";

    private final ProductRepository repository;
    private final ProductPriceService priceService;
    private final ProductAsyncHandler asyncHandler;
    private final Instant creationTime;

    @Override
    public Product handleRequest(final CreateProductRequest request) {
        try {
            final var newPrice = priceService.create(request);
            context.addArgument(PRODUCT_PRICE_KEY, newPrice);

            if (!hasNext()) {
                setNextHandler(new SaveCreatedProduct(repository, asyncHandler, creationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown product creation error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        }
    }

}
