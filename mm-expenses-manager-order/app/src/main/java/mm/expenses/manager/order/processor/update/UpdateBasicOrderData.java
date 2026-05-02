package mm.expenses.manager.order.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.order.OrderCommonValidation;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.processor.OrderedProductService;

import java.time.Instant;
import java.util.Objects;

/**
 * Step in chain of {@link Order} update.
 * Modifies basic information in order if any detected.
 */
@Slf4j
@RequiredArgsConstructor
final class UpdateBasicOrderData extends UpdateOrderChain {

    final static String IS_UPDATED_KEY = "isUpdated";

    private final OrderRepository repository;
    private final OrderedProductService productService;
    private final Instant modificationTime;

    @Override
    public Order handleRequest(final UpdateOrderRequest request) {
        try {
            final var orderOpt = context.getArgument(FindOrderToUpdate.ORDER_KEY);

            orderOpt.ifPresentOrElse(
                    order -> {
                        context.addArgument(IS_UPDATED_KEY, updateOrderName((Order) order, request));
                    },
                    () -> {
                        log.error("Cannot update order because of one of required parameters is null. orderOpt={}", orderOpt);
                        throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED);
                    }
            );

            if (!hasNext()) {
                setNextHandler(new ModifyOrderedProductsDuringOrderUpdate(repository, productService, modificationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order update error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        }
    }

    private boolean updateOrderName(final Order existedOrder, final UpdateOrderRequest request) {
        if (Objects.nonNull(request.getName())) {
            if (!OrderCommonValidation.isOrderNameNotEmpty(request.getName())) {
                throw new ApiValidationException(OrderExceptionMessage.ORDER_NAME_EMPTY);
            }
            existedOrder.setName(request.getName().trim());
            context.addArgument(FindOrderToUpdate.ORDER_KEY, existedOrder);
            return true;
        }
        return false;
    }

}
