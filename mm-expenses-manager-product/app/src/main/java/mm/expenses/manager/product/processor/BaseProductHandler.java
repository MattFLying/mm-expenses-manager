package mm.expenses.manager.product.processor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.api.product.model.ProductPage;
import mm.expenses.manager.product.api.product.model.ProductResponse;
import mm.expenses.manager.product.currency.PriceConverter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Default handler definition to process specific handlers.
 */
@RequiredArgsConstructor
public abstract class BaseProductHandler<T> {

    protected final ProductMapper mapper;
    protected final PriceConverter priceConverter;

    /**
     * Handler type
     */
    public abstract Type getType();

    /**
     * Handles specific request to processed with specific response expected.
     */
    public abstract BaseProductHandler<T>.Response handle(final Request request);

    /**
     * Decorates handled response.
     */
    public abstract BaseProductHandler<T>.Response handleDecorated(final Request request);

    protected BaseProductHandler<T>.Response of(final T product) {
        return new Response(product);
    }

    protected BaseProductHandler<T>.Response of(final T product, final ProductResponse productResponse) {
        return new Response(product, productResponse);
    }

    protected BaseProductHandler<T>.Response of(final List<T> listedResponse) {
        return new Response(listedResponse);
    }

    protected BaseProductHandler<T>.Response of(final Page<T> productPage) {
        return new Response(productPage);
    }

    protected BaseProductHandler<T>.Response of(final Page<T> page, final ProductPage productPage) {
        return new Response(page, productPage);
    }

    /**
     * Request to be processed within specific handlers' implementation.
     *
     * @param request               - request object to be processed
     * @param id                    - specific id
     * @param isDeleted             - if should handle deleted objects
     * @param shouldConvertCurrency - defines if curreny should be also converted
     */
    @Builder
    public record Request(Object request, UUID id, Boolean isDeleted, Boolean shouldConvertCurrency,
                          CurrencyCode expectedCurrency) {

        public boolean isShouldConvertCurrency() {
            return Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency;
        }

    }

    /**
     * Response to be used in handlers context with the expected return value.
     **/
    @Getter
    @AllArgsConstructor
    public class Response {

        private T response;
        private ProductResponse decoratedResponse;
        private Page<T> pagedResponse;
        private List<T> listedResponse;
        private ProductPage decoratedPagedResponse;

        public Response(final T response) {
            this(response, null, null, null, null);
        }

        public Response(final T response, final ProductResponse decoratedResponse) {
            this(response, decoratedResponse, null, null, null);
        }

        public Response(final List<T> listedResponse) {
            this(null, null, null, listedResponse, null);
        }

        public Response(final Page<T> pagedResponse) {
            this(null, null, pagedResponse, null, null);
        }

        public Response(final Page<T> pagedResponse, final ProductPage decoratedPagedResponse) {
            this(null, null, pagedResponse, null, decoratedPagedResponse);
        }

    }

    /**
     * Possible handler types to be implemented and to be used.
     */
    public enum Type {
        CREATE,
        UPDATE,
        DELETE,
        DELETE_MANY,
        FIND,
        SEARCH,

        HARD_DELETE,
        UPDATE_PRICE_CURRENCIES
    }

}
