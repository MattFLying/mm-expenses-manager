package mm.expenses.manager.order.processor.decorator;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.order.api.order.model.OrderResponse;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderMapper;
import org.springframework.data.domain.Page;

/**
 * Classic mapper from {@link Order} to {@link OrderResponse}.
 */
@RequiredArgsConstructor
public class OrderClassicMapperToResponse extends OrderMappingStrategy {

    private final OrderMapper mapper;

    @Override
    public OrderResponse decorate(final Order order) {
        return mapper.mapToResponse(order);
    }

    @Override
    public Page<OrderResponse> decorate(final Page<Order> pagedOrders) {
        return mapper.mapToPageOrderResponse(pagedOrders);
    }

}
