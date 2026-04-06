package mm.expenses.manager.order.processor.delete.many;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderRepository;
import mm.expenses.manager.order.processor.delete.single.DeleteSingleOrder;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Step in chain of multiple {@link Order}s deletion.
 * Delete orders one-by-one.
 */
@Slf4j
@RequiredArgsConstructor
final class DeleteMultipleOrders extends DeleteMultipleOrdersChain<Set<UUID>> {

    private final OrderRepository repository;
    private final Instant deletionTime;

    @Override
    public List<Order> handleRequest(final Set<UUID> ids) {
        try {
            final var ordersToRemoveOpt = context.getArgument(FindOrdersToRemove.ORDERS_TO_REMOVE_KEY);

            if (ordersToRemoveOpt.isPresent()) {
                final var ordersToRemove = (List<Order>) ordersToRemoveOpt.get();

                ordersToRemove.forEach(order -> DeleteSingleOrder.deleteSingle(order, deletionTime));

                context.addArgument(FindOrdersToRemove.ORDERS_TO_REMOVE_KEY, ordersToRemove);
            } else {
                log.error("Cannot delete orders because of one of required parameters is null. ordersToRemoveOpt={}", ordersToRemoveOpt);
                throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_DELETED);
            }

            if (!hasNext()) {
                setNextHandler(new SaveDeletedOrders(repository));
            }
            return next.handleRequest(ids);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown orders deletion error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_DELETED, exception);
        }
    }

}
