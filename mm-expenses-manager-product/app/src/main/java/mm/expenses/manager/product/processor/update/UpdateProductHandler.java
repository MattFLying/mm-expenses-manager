package mm.expenses.manager.product.processor.update;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductAsyncHandler;
import mm.expenses.manager.product.core.ProductMapper;
import mm.expenses.manager.product.core.ProductRepository;
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
        return Type.UPDATE_PRODUCT;
    }

    @Transactional
    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new ValidateRequestedProductUpdates(repository, asyncHandler, now, request.getId()),
                new FindProductToUpdate(repository, asyncHandler, now),
                new UpdateProductData(repository, asyncHandler, now),
                new SaveUpdatedProduct(repository, asyncHandler, now),
                new AsyncProductUpdate(asyncHandler)
        );
        return Response.builder()
                .response(chain.handleRequest(request.getRequest()))
                .build();
    }

    @Transactional
    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof ProductHandler.Request updateRequest) {
                final var response = handle(updateRequest);
                final var decorator = Objects.isNull(updateRequest.getExpectedCurrency())
                        ? new ProductClassicMapperToResponse(mapper)
                        : new ProductClassicMapperToResponseWithDefaultCurrency(mapper, updateRequest.getExpectedCurrency());

                if (response instanceof ProductHandler.Response product) {
                    final var productResponse = product.mapResponse(Product.class);
                    return Response.builder()
                            .response(productResponse)
                            .decoratedResponse(decorator.decorate(productResponse))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown product update error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        }
    }

}
