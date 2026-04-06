package mm.expenses.manager.order.processor.delete.many;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderRepository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Step in chain of multiple {@link Order}s deletion.
 * Finds requested orders and remove them.
 */
@Slf4j
@RequiredArgsConstructor
final class FindOrdersToRemove extends DeleteMultipleOrdersChain<Set<UUID>> {

    final static String ORDERS_TO_REMOVE_KEY = "toRemove";

    private final OrderRepository repository;
    private final Instant deletionTime;

    @Override
    public List<Order> handleRequest(final Set<UUID> ids) {
        try {
            final var toRemove = repository.findAllByIdInAndIsDeleted(ids, false);
            if (toRemove.size() != ids.size()) {
                final var notFoundIds = toRemove.stream()
                        .map(Order::getId)
                        .filter(orderId -> !ids.contains(orderId))
                        .collect(Collectors.toSet());
                throw new ApiNotFoundException(OrderExceptionMessage.ORDERS_NOT_FOUND.withParameters(notFoundIds));
            }

            context.addArgument(ORDERS_TO_REMOVE_KEY, toRemove);

            if (!hasNext()) {
                setNextHandler(new DeleteMultipleOrders(repository, deletionTime));
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
