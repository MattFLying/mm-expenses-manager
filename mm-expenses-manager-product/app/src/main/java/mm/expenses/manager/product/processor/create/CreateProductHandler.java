package mm.expenses.manager.product.processor.create;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPriceService;
import mm.expenses.manager.product.processor.*;
import mm.expenses.manager.product.processor.decorator.ProductClassicMapperToResponse;
import mm.expenses.manager.product.processor.decorator.ProductClassicMapperToResponseWithDefaultCurrency;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/**
 * Handler responsible for product creation.
 */
@Slf4j
@Component
public class CreateProductHandler extends ProductHandler {

    private final ProductPriceService priceService;
    private final ProductAsyncHandler asyncHandler;

    CreateProductHandler(final ProductRepository repository, final ProductMapper mapper, final PriceConverter priceConverter, final ProductPriceService priceService, final ProductAsyncHandler asyncHandler) {
        super(repository, mapper, priceConverter);

        this.priceService = priceService;
        this.asyncHandler = asyncHandler;
    }

    @Override
    public Type getType() {
        return Type.CREATE;
    }

    @Transactional
    @Override
    public Response handle(final Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new PrepareProductDetails(repository, priceService, asyncHandler, now),
                new PrepareProductPrice(repository, priceService, asyncHandler, now),
                new SaveCreatedProduct(repository, asyncHandler, now),
                new AsyncProductCreation(asyncHandler)
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
            log.error("Unknown product creation error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        }
    }

}
