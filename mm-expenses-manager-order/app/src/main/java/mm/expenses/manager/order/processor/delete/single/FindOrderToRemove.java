package mm.expenses.manager.order.processor.delete.single;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Step in chain of {@link Order} deletion.
 * Finds requested order and remove it.
 */
@Slf4j
final class FindOrderToRemove extends DeleteOrderByIdChain {

    final static String ORDER_KEY = "order";

    private final Instant deletionTime;

    FindOrderToRemove(final OrderRepository repository, final Instant deletionTime) {
        super(repository);

        this.deletionTime = deletionTime;
    }

    @Override
    public Order handleRequest(final UUID orderId) {
        try {
            final var order = findOrderById(orderId, false);
            if (Objects.isNull(order)) {
                throw new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(orderId));
            }

            context.addArgument(ORDER_KEY, order);

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
