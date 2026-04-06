package mm.expenses.manager.order.processor;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.order.api.order.model.OrderPage;
import mm.expenses.manager.order.api.order.model.OrderResponse;
import mm.expenses.manager.order.price.PriceConverter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Default handler definition to process specific handlers.
 */
@RequiredArgsConstructor
public abstract class OrderHandler {

    protected final OrderRepository repository;
    protected final OrderMapper mapper;
    protected final PriceConverter priceConverter;

    /**
     * Handler type
     */
    public abstract Type getType();

    /**
     * Handles specific request to processed with specific response expected.
     */
    public abstract Response handle(final Request request);

    /**
     * Decorates handled response.
     */
    public abstract Response handleDecorated(final Request request);

    protected Response of(final Order order) {
        return Response.builder().response(order).build();
    }

    protected Response of(final Order order, final OrderResponse orderResponse) {
        return Response.builder().response(order).decoratedResponse(orderResponse).build();
    }

    protected Response of(final List<Order> listedResponse) {
        return Response.builder().listedResponse(listedResponse).build();
    }

    protected Response of(final Page<Order> orderPage) {
        return Response.builder().pagedResponse(orderPage).build();
    }

    protected Response of(final Page<Order> page, final OrderPage orderPage) {
        return Response.builder().pagedResponse(page).decoratedPagedResponse(orderPage).build();
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
    public record Request(Object request, UUID id, Boolean isDeleted, Boolean shouldConvertCurrency) {

        public boolean isShouldConvertCurrency() {
            return Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency;
        }

    }

    /**
     * Response to be used in handlers context with the expected return value.
     *
     * @param response               - order response
     * @param decoratedResponse      - decorated order response
     * @param pagedResponse          - paginated orders
     * @param listedResponse         - list of orders
     * @param decoratedPagedResponse - decorated paginated orders
     */
    @Builder
    public record Response(
            Order response,
            OrderResponse decoratedResponse,
            Page<Order> pagedResponse,
            List<Order> listedResponse,
            OrderPage decoratedPagedResponse) {

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
        SEARCH
    }

}
