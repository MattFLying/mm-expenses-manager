package mm.expenses.manager.order.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiInternalErrorException;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.*;
import org.springframework.dao.OptimisticLockingFailureException;

import java.time.Instant;
import java.util.List;

/**
 * Step in chain of new {@link Order} creation.
 * Builds the expected {@link Order} object and save it into database.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveCreatedOrder extends CreateOrderChain {

    private final OrderRepository repository;
    private final Instant creationTime;

    @Override
    public void setNextHandler(final ChainCommandExecution<CreateNewOrderRequest, Order> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Order handleRequest(final CreateNewOrderRequest request) {
        try {
            final var orderedProductsOpt = context.getArgument(MapOrderedProducts.ORDERED_PRODUCTS_KEY);
            final var orderPricesOpt = context.getArgument(CalculatePrices.CALCULATED_PRICES_FOR_ORDERED_PRODUCTS_KEY);

            if (orderedProductsOpt.isPresent() && orderPricesOpt.isPresent()) {
                final var orderedProducts = (List<OrderedProduct>) orderedProductsOpt.get();
                final var orderPrices = (List<OrderPrice>) orderPricesOpt.get();

                final var newOrder = Order.builder()
                        .name(request.getName())
                        .products(orderedProducts)
                        .prices(orderPrices)
                        .createdAt(creationTime)
                        .lastModifiedAt(creationTime)
                        .build();

                final var saved = repository.save(newOrder);
                context.addArgument(Context.SAVED_KEY, saved);

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(request);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot create order because of one of required parameters is null. orderedProductsOpt={}, orderPricesOpt={}", orderedProductsOpt, orderPricesOpt);
                throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED);
            }
        } catch (final IllegalArgumentException exception) {
            log.error("Order entity is null.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("Order entity has optimistic lock failure.", exception);
            throw new ApiInternalErrorException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED, exception);
        } catch (final Exception exception) {
            log.error("Unknown order creation error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED, exception);
        }
    }

}
