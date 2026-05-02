package mm.expenses.manager.order.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.processor.OrderedProductService;

import java.time.Instant;
import java.util.UUID;

/**
 * Step in chain of {@link Order} update.
 * Finds requested order and move to the next step if found.
 */
@Slf4j
@RequiredArgsConstructor
final class FindOrderToUpdate extends UpdateOrderChain {

    final static String ORDER_KEY = "order";

    private final OrderRepository repository;
    private final OrderedProductService productService;
    private final Instant modificationTime;

    @Override
    public Order handleRequest(final UpdateOrderRequest request) {
        try {
            final var orderIdOpt = context.getArgument(ValidateRequestedOrderUpdates.ORDER_ID_KEY);

            orderIdOpt.ifPresentOrElse(
                    orderId -> {
                        final var existedOrder = repository.findByIdAndIsDeleted((UUID) orderId, false)
                                .orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(orderId)));

                        context.addArgument(ORDER_KEY, existedOrder);
                    },
                    () -> {
                        log.error("Cannot update order because of one of required parameters is null. orderIdOpt={}", orderIdOpt);
                        throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED);
                    }
            );

            if (!hasNext()) {
                setNextHandler(new UpdateBasicOrderData(repository, productService, modificationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order update error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        }
    }

}
