package mm.expenses.manager.order.processor.delete.single;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Step in chain of {@link Order} deletion.
 * Finds requested order and remove it.
 */
@Slf4j
@RequiredArgsConstructor
final class FindOrderToRemove extends DeleteOrderByIdChain {

    final static String ORDER_KEY = "order";

    private final OrderRepository repository;
    private final Instant deletionTime;

    @Override
    public Order handleRequest(final UUID orderId) {
        try {
            final var orderOpt = repository.findByIdAndIsDeleted(orderId, false);

            if (orderOpt.isPresent()) {
                final var order = orderOpt.get();
                context.addArgument(ORDER_KEY, order);
            } else {
                throw new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(orderId));
            }

            if (!hasNext()) {
                setNextHandler(new DeleteSingleOrder(repository, deletionTime));
            }
            return next.handleRequest(orderId);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order deletion error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED, exception);
        }
    }

}
