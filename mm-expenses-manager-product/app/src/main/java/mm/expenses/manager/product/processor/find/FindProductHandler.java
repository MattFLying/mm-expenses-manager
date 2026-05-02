package mm.expenses.manager.product.processor.find;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.processor.ProductHandler;
import mm.expenses.manager.product.core.ProductMapper;
import mm.expenses.manager.product.core.ProductRepository;
import mm.expenses.manager.product.processor.decorator.ProductClassicMapperToResponse;
import mm.expenses.manager.product.processor.decorator.ProductClassicMapperToResponseWithDefaultCurrency;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Handler responsible for finding single product.
 */
@Slf4j
@Component
class FindProductHandler extends ProductHandler {

    FindProductHandler(final ProductRepository repository, final ProductMapper mapper, final PriceConverter priceConverter) {
        super(repository, mapper, priceConverter);
    }

    @Override
    public Type getType() {
        return Type.FIND_PRODUCT;
    }

    @Override
    public ProcessorHandler.Response handle(final ProcessorHandler.Request request) {
        if (request instanceof ProductHandler.Request findRequest) {
            final var chain = new FindProductById(repository, findRequest.isDeleted());
            return Response.builder()
                    .response(chain.handleRequest(findRequest.getId()))
                    .build();
        }
        throw new ProcessorHandler.ProcessorHandlerException(ProductExceptionMessage.PRODUCT_CANNOT_BE_FOUND.getMessage());
    }

    @Override
    public ProcessorHandler.Response handleDecorated(final ProcessorHandler.Request request) {
        try {
            if (request instanceof ProductHandler.Request findRequest) {
                final var response = handle(findRequest);
                final var decorator = Objects.isNull(findRequest.getExpectedCurrency())
                        ? new ProductClassicMapperToResponse(mapper)
                        : new ProductClassicMapperToResponseWithDefaultCurrency(mapper, findRequest.getExpectedCurrency());

                if (response instanceof ProductHandler.Response product) {
                    final var productResponse = product.mapResponse(Product.class);
                    return Response.builder()
                            .response(productResponse)
                            .decoratedResponse(decorator.decorate(productResponse))
                            .build();
                }
            }
            throw new ProcessorHandler.ProcessorHandlerException(ProductExceptionMessage.PRODUCT_CANNOT_BE_FOUND.getMessage());
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown find single product error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_FOUND, exception);
        }
    }

}
