package mm.expenses.manager.order.processor.delete.single;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Step in chain of {@link Order} deletion.
 * Deletes order and its associated models with specific flags.
 */
@Slf4j
@RequiredArgsConstructor
public final class DeleteSingleOrder extends DeleteOrderByIdChain {

    private final OrderRepository repository;
    private final Instant deletionTime;

    @Override
    public Order handleRequest(final UUID orderId) {
        try {
            final var orderOpt = context.getArgument(FindOrderToRemove.ORDER_KEY);
            if (orderOpt.isPresent()) {
                final var order = (Order) orderOpt.get();
                deleteSingle(order, deletionTime);

                context.addArgument(FindOrderToRemove.ORDER_KEY, order);

                if (hasNext()) {
                    return next.handleRequest(orderId);
                }
            } else {
                log.error("Cannot update order because of one of required parameters is null. orderOpt={}", orderOpt);
                throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED);
            }

            if (!hasNext()) {
                setNextHandler(new SaveDeletedOrder(repository));
            }
            return next.handleRequest(orderId);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order deletion error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED, exception);
        }
    }

    public static void deleteSingle(final Order order, final Instant deletionTime) {
        order.setDeleted(true);
        order.setLastModifiedAt(deletionTime);

        order.getProducts().forEach(product -> {
            product.setDeleted(true);
            product.setLastModifiedAt(deletionTime);
            product.getPrices().forEach(price -> {
                price.setDeleted(true);
                price.setLastModifiedAt(deletionTime);
            });
        });
        order.getPrices().forEach(price -> {
            price.setDeleted(true);
            price.setLastModifiedAt(deletionTime);
        });
    }

}
