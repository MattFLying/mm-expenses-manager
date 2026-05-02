package mm.expenses.manager.product.processor.create;

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
        return Type.CREATE_PRODUCT;
    }

    @Transactional
    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        final var now = Instant.now();
        final var chain = ChainCommandExecution.build(
                new PrepareProductDetails(repository, priceService, asyncHandler, now),
                new PrepareProductPrice(repository, priceService, asyncHandler, now),
                new SaveCreatedProduct(repository, asyncHandler, now),
                new AsyncProductCreation(asyncHandler)
        );
        return Response.builder()
                .response(chain.handleRequest(request.getRequest()))
                .build();
    }

    @Transactional
    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof ProductHandler.Request createRequest) {
                final var response = handle(createRequest);
                final var decorator = Objects.isNull(createRequest.getExpectedCurrency())
                        ? new ProductClassicMapperToResponse(mapper)
                        : new ProductClassicMapperToResponseWithDefaultCurrency(mapper, createRequest.getExpectedCurrency());

                if (response instanceof ProductHandler.Response product) {
                    final var productResponse = product.mapResponse(Product.class);
                    return Response.builder()
                            .response(productResponse)
                            .decoratedResponse(decorator.decorate(productResponse))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown product creation error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_CREATED, exception);
        }
    }

}
