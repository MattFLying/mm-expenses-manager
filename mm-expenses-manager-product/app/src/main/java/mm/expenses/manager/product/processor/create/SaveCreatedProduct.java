package mm.expenses.manager.product.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPrice;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductRepository;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Step in chain of new {@link Product} creation.
 * Builds the expected {@link Product} object and save it into database.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveCreatedProduct extends CreateProductChain {

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant creationTime;

    @Override
    public Product handleRequest(final CreateProductRequest request) {
        try {
            final var newProduct = Product.builder()
                    .name(request.getName())
                    .createdAt(creationTime)
                    .lastModifiedAt(creationTime)
                    .build();

            context.getArgument(PrepareProductDetails.PRODUCT_DETAILS_KEY)
                    .ifPresent(details -> newProduct.setDetails((Map<String, Object>) details));

            context.getArgument(PrepareProductPrice.PRODUCT_PRICE_KEY)
                    .ifPresentOrElse(
                            price -> {
                                newProduct.setPrice(List.of((ProductPrice) price));
                            },
                            () -> {
                                log.error("Cannot create product because of price is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED);
                            }
                    );

            final var saved = repository.save(newProduct);
            context.addArgument(ChainCommandExecution.Context.SAVED_KEY, saved);

            if (!hasNext()) {
                setNextHandler(new AsyncProductCreation(asyncHandler));
            }
            return next.handleRequest(request);
        } catch (final IllegalArgumentException exception) {
            log.error("Product entity is null.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("Product entity has optimistic lock failure.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        } catch (final Exception exception) {
            log.error("Unknown product creation error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        }
    }

}
