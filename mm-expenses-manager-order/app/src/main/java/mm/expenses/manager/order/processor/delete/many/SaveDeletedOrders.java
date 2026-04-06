package mm.expenses.manager.order.processor.delete.many;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiInternalErrorException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderRepository;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Step in chain of new {@link Order}s deletion.
 * Builds the expected {@link Order} objects and save it into database.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveDeletedOrders extends DeleteMultipleOrdersChain<Set<UUID>> {

    private final OrderRepository repository;

    @Override
    public void setNextHandler(final ChainCommandExecution<Set<UUID>, List<Order>> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public List<Order> handleRequest(final Set<UUID> ids) {
        try {
            final var ordersToRemoveOpt = context.getArgument(FindOrdersToRemove.ORDERS_TO_REMOVE_KEY);

            if (ordersToRemoveOpt.isPresent()) {
                final var ordersToRemove = (List<Order>) ordersToRemoveOpt.get();

                final var saved = repository.saveAll(ordersToRemove);

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(ids);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot delete orders because of one of required parameters is null. ordersToRemoveOpt={}", ordersToRemoveOpt);
                throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_DELETED);
            }
        } catch (final IllegalArgumentException exception) {
            log.error("Order entities or one of them are null.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_DELETED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("Order entities have optimistic lock failure.", exception);
            throw new ApiInternalErrorException(OrderExceptionMessage.ORDERS_CANNOT_BE_DELETED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_DELETED, exception);
        } catch (final Exception exception) {
            log.error("Unknown orders deletion error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDERS_CANNOT_BE_DELETED, exception);
        }
    }

}
