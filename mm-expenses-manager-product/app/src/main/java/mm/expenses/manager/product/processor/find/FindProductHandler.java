package mm.expenses.manager.product.processor.find;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.ProductHandler;
import mm.expenses.manager.product.processor.ProductMapper;
import mm.expenses.manager.product.processor.ProductRepository;
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
        return Type.FIND;
    }

    @Override
    public Response handle(final Request request) {
        final var chain = new FindProductById(repository, request.isDeleted());
        final var response = chain.handleRequest(request.id());

        return of(response);
    }

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
            log.error("Unknown find single product error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_FOUND, exception);
        }
    }

}
