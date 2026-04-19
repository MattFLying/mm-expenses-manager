package mm.expenses.manager.product.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPriceService;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;
import mm.expenses.manager.product.processor.ProductRepository;

import java.time.Instant;

/**
 * Step in chain of new {@link Product} creation.
 * Prepares new product details.
 */
@Slf4j
@RequiredArgsConstructor
final class PrepareProductDetails extends CreateProductChain {

    final static String PRODUCT_DETAILS_KEY = "details";

    private final ProductRepository repository;
    private final ProductPriceService priceService;
    private final ProductAsyncHandler asyncHandler;
    private final Instant creationTime;

    @Override
    public Product handleRequest(final CreateProductRequest request) {
        try {
            final var details = request.getDetails();
            context.addArgument(PRODUCT_DETAILS_KEY, details);

            if (!hasNext()) {
                setNextHandler(new PrepareProductPrice(repository, priceService, asyncHandler, creationTime));
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
