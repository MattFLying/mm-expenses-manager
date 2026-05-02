package mm.expenses.manager.order.core;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.processor.ProcessorType;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.OrderPage;
import mm.expenses.manager.order.api.order.model.OrderResponse;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.processor.OrderHandler;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    private final Map<ProcessorType, OrderHandler> handlers;

    public OrderService(final List<OrderHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        OrderHandler::getType,
                        Function.identity()
                ));
    }

    @Transactional
    public Order create(final CreateNewOrderRequest request) {
        final var requestedData = requestOf(request);
        final var handler = getHandler(OrderHandler.Type.CREATE_ORDER);
        final var result = handler.handle(requestedData);

        return result.mapResponse(Order.class);
    }

    @Transactional
    public OrderResponse create(final CreateNewOrderRequest request, final Boolean shouldConvertCurrency) {
        final var requestedData = requestOf(request, shouldConvertCurrency);
        final var handler = getHandler(OrderHandler.Type.CREATE_ORDER);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(OrderResponse.class);
    }

    @Transactional
    public Order update(final UUID orderId, final UpdateOrderRequest request) {
        final var requestedData = requestOf(orderId, request);
        final var handler = getHandler(OrderHandler.Type.UPDATE_ORDER);
        final var result = handler.handle(requestedData);

        return result.mapResponse(Order.class);
    }

    @Transactional
    public OrderResponse update(final UUID orderId, final UpdateOrderRequest request, final Boolean shouldConvertCurrency) {
        final var requestedData = requestOf(orderId, request, shouldConvertCurrency);
        final var handler = getHandler(OrderHandler.Type.UPDATE_ORDER);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(OrderResponse.class);
    }

    public Order delete(final UUID orderId) {
        final var requestedData = requestOf(orderId);
        final var handler = getHandler(OrderHandler.Type.DELETE_SINGLE_ORDER);
        final var result = handler.handle(requestedData);

        return result.mapResponse(Order.class);
    }

    public void delete(final Set<UUID> orderIds) {
        final var requestedData = requestOf(orderIds);
        final var handler = getHandler(OrderHandler.Type.DELETE_MANY_ORDERS);
        handler.handle(requestedData);
    }

    public Order findById(final UUID orderId, final Boolean isDeleted) {
        final var requestedData = requestOf(orderId, isDeleted);
        final var handler = getHandler(OrderHandler.Type.FIND_ORDER);
        final var result = handler.handle(requestedData);

        return result.mapResponse(Order.class);
    }

    public OrderResponse findById(final UUID orderId, final Boolean isDeleted, final Boolean shouldConvertCurrency) {
        final var requestedData = requestOf(orderId, isDeleted, shouldConvertCurrency);
        final var handler = getHandler(OrderHandler.Type.FIND_ORDER);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(OrderResponse.class);
    }

    public Page<Order> search(final EntityFilter queryFilter) {
        final var requestedData = requestOf(queryFilter);
        final var handler = getHandler(OrderHandler.Type.SEARCH_ORDERS);
        final var result = handler.handle(requestedData);

        return ((OrderHandler.Response) result).getPagedResponse();
    }

    public OrderPage search(final EntityFilter queryFilter, final Boolean shouldConvertCurrency) {
        final var requestedData = requestOf(queryFilter, shouldConvertCurrency);
        final var handler = getHandler(OrderHandler.Type.SEARCH_ORDERS);
        final var result = handler.handleDecorated(requestedData);

        return ((OrderHandler.Response) result).getDecoratedPagedResponse();
    }

    private OrderHandler getHandler(final OrderHandler.Type type) {
        try {
            if (Objects.isNull(type)) {
                throw new IllegalArgumentException("Cannot recognize type of order handler");
            }
            return handlers.get(type);
        } catch (final NullPointerException exception) {
            throw new IllegalArgumentException(String.format("Cannot recognize order handler of %s type", type));
        }
    }

    private OrderHandler.Request requestOf(final Object request) {
        return OrderHandler.Request.builder().request(request).build();
    }

    private OrderHandler.Request requestOf(final Object request, final Boolean shouldConvertCurrency) {
        return OrderHandler.Request.builder().request(request).shouldConvertCurrency(shouldConvertCurrency).build();
    }

    private OrderHandler.Request requestOf(final UUID orderId, final Object request) {
        return OrderHandler.Request.builder().id(orderId).request(request).build();
    }

    private OrderHandler.Request requestOf(final UUID orderId, final Object request, final Boolean shouldConvertCurrency) {
        return OrderHandler.Request.builder().id(orderId).request(request).shouldConvertCurrency(shouldConvertCurrency).build();
    }

    private OrderHandler.Request requestOf(final UUID orderId, final Boolean isDeleted) {
        return OrderHandler.Request.builder().id(orderId).isDeleted(isDeleted).build();
    }

    private OrderHandler.Request requestOf(final UUID orderId, final Boolean isDeleted, final Boolean shouldConvertCurrency) {
        return OrderHandler.Request.builder().id(orderId).isDeleted(isDeleted).shouldConvertCurrency(shouldConvertCurrency).build();
    }

}
