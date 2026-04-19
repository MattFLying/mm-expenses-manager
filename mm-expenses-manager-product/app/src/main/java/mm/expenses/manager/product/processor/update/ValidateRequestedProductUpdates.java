package mm.expenses.manager.product.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.api.product.model.UpdatePriceRequest;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPriceService;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;
import mm.expenses.manager.product.processor.ProductRepository;
import org.apache.commons.collections4.MapUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Step in chain of {@link Product} update.
 * Validates if any change is requested to be updated in given product.
 */
@Slf4j
@RequiredArgsConstructor
final class ValidateRequestedProductUpdates extends UpdateProductChain {

    final static String PRODUCT_ID_KEY = "productId";

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant modificationTime;
    private final UUID productId;

    @Override
    public Product handleRequest(final UpdateProductRequest request) {
        try {
            if (!isAnyUpdateProduct(request)) {
                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_NO_UPDATE_DATA);
            }

            context.addArgument(PRODUCT_ID_KEY, productId);

            if (!hasNext()) {
                setNextHandler(new FindProductToUpdate(repository, asyncHandler, modificationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown product update error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        }
    }

    private boolean isAnyUpdateProduct(final UpdateProductRequest request) {
        final var isNameUpdated = Objects.nonNull(request.getName());
        final var isPriceUpdated = Objects.nonNull(request.getPrice()) && isAnyUpdatePrice(request.getPrice());
        final var isDetailsUpdated = MapUtils.isNotEmpty(request.getDetails());

        return isNameUpdated || isPriceUpdated || isDetailsUpdated;
    }

    private boolean isAnyUpdatePrice(final UpdatePriceRequest request) {
        final var isCurrencyUpdated = Objects.nonNull(request.getCurrency());
        final var isValueUpdated = Objects.nonNull(request.getValue());

        return isCurrencyUpdated || isValueUpdated;
    }

}
