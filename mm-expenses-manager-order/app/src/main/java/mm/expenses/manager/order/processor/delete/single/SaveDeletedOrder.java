package mm.expenses.manager.order.processor.delete.single;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiInternalErrorException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.UUID;

/**
 * Step in chain of {@link Order} deletion.
 * Saves deleted order.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveDeletedOrder extends DeleteOrderByIdChain {

    private final OrderRepository repository;

    @Override
    public void setNextHandler(final ChainCommandExecution<UUID, Order> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Order handleRequest(final UUID orderId) {
        try {
            final var orderOpt = context.getArgument(FindOrderToRemove.ORDER_KEY);
            if (orderOpt.isPresent()) {
                final var order = (Order) orderOpt.get();
                final var saved = repository.save(order);
                context.addArgument(FindOrderToRemove.ORDER_KEY, saved);

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(orderId);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot update order because of one of required parameters is null. orderOpt={}", orderOpt);
                throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED);
            }
        } catch (final IllegalArgumentException exception) {
            log.error("Order entity is null.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("Order entity has optimistic lock failure.", exception);
            throw new ApiInternalErrorException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED, exception);
        } catch (final Exception exception) {
            log.error("Unknown order deletion error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_DELETED, exception);
        }
    }

}
