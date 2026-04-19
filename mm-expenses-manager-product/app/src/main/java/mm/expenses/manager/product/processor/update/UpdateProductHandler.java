package mm.expenses.manager.product.processor.update;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.*;
import mm.expenses.manager.product.processor.decorator.ProductClassicMapperToResponse;
import mm.expenses.manager.product.processor.decorator.ProductClassicMapperToResponseWithDefaultCurrency;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/**
 * Handler responsible for product update.
 */
@Slf4j
@Component
public class UpdateProductHandler extends ProductHandler {

    private final ProductAsyncHandler asyncHandler;

    UpdateProductHandler(final ProductRepository repository, final ProductMapper mapper, final PriceConverter priceConverter, final ProductAsyncHandler asyncHandler) {
        super(repository, mapper, priceConverter);

        this.asyncHandler = asyncHandler;
    }

    @Override
    public Type getType() {
        return Type.UPDATE;
    }

    @Transactional
    @Override
    public Response handle(final Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new ValidateRequestedProductUpdates(repository, asyncHandler, now, request.id()),
                new FindProductToUpdate(repository, asyncHandler, now),
                new UpdateProductData(repository, asyncHandler, now),
                new SaveUpdatedProduct(repository, asyncHandler, now),
                new AsyncProductUpdate(asyncHandler)
        );
        return of((Product) chain.handleRequest(request.request()));
    }

    @Transactional
    @Override
    public Response handleDecorated(final Request request) {
        try {
            final var response = handle(request);
            final var decorator = Objects.isNull(request.expectedCurrency())
                    ? new ProductClassicMapperToResponse(mapper)
                    : new ProductClassicMapperToResponseWithDefaultCurrency(mapper, request.expectedCurrency());

            final var product = response.getResponse();
            return of(product, decorator.decorate(product));
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown product update error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        }
    }

}
