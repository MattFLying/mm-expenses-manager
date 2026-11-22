package mm.expenses.manager.order.processor.find;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Step in chain of new {@link Order} creation.
 * Finds requested product by its id.
 */
@Slf4j
final class FindOrderById extends FindOrderByIdChain {

    private final Boolean isDeleted;

    FindOrderById(final OrderRepository repository, final Boolean isDeletedFlag) {
        super(repository);

        this.isDeleted =  isDeletedFlag;
    }

    @Override
    public void setNextHandler(final ChainCommandExecution<UUID, Order> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Order handleRequest(final UUID id) {
        try {
            final var isDeletedFlag = Objects.nonNull(isDeleted) ? isDeleted : false;
            final var foundOrder = findOrderById(id, isDeletedFlag);

            if (hasNext()) {
                // This step should not ever happen but if there is any possibility to do anything after save in database
                // then there is a proper place to do it.
                return next.handleRequest(id);
            } else {
                return foundOrder;
            }
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown find single order error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id), exception);
        }
    }

}
