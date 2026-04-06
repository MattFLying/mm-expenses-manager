package mm.expenses.manager.order.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiInternalErrorException;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderRepository;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * Step in chain of {@link Order} update.
 * Save updated order into database.
 */
@Slf4j
@RequiredArgsConstructor
final class SaveUpdatedOrder extends UpdateOrderChain {

    private final OrderRepository repository;

    @Override
    public void setNextHandler(final ChainCommandExecution<UpdateOrderRequest, Order> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Order handleRequest(final UpdateOrderRequest request) {
        try {
            final var orderOpt = context.getArgument(FindOrderToUpdate.ORDER_KEY);

            if (orderOpt.isPresent()) {
                final var order = (Order) orderOpt.get();
                final var saved = repository.save(order);
                context.addArgument(Context.SAVED_KEY, saved);
                context.addArgument(FindOrderToUpdate.ORDER_KEY, saved);

                if (hasNext()) {
                    // This step should not ever happen but if there is any possibility to do anything after save in database
                    // then there is a proper place to do it.
                    return next.handleRequest(request);
                } else {
                    return saved;
                }
            } else {
                log.error("Cannot update order because of one of required parameters is null. orderOpt={}", orderOpt);
                throw new IllegalStateException("Cannot update order.");
            }
        } catch (final IllegalArgumentException exception) {
            log.error("Order entity is null.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        } catch (final OptimisticLockingFailureException exception) {
            log.error("Order entity has optimistic lock failure.", exception);
            throw new ApiInternalErrorException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        } catch (final ApiException exception) {
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        } catch (final Exception exception) {
            log.error("Unknown order update error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        }
    }

}
