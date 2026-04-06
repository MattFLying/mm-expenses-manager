package mm.expenses.manager.order.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.Order;
import mm.expenses.manager.order.processor.OrderRepository;
import mm.expenses.manager.order.processor.OrderedProductService;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Step in chain of {@link Order} update.
 * Validates if any change is requested to be updated in given order.
 */
@Slf4j
@RequiredArgsConstructor
final class ValidateRequestedOrderUpdates extends UpdateOrderChain {

    final static String ORDER_ID_KEY = "orderId";

    private final OrderRepository repository;
    private final OrderedProductService productService;
    private final Instant modificationTime;
    private final UUID orderId;

    @Override
    public Order handleRequest(final UpdateOrderRequest request) {
        try {
            if (!isAnyUpdateOrder(request)) {
                throw new ApiConflictException(OrderExceptionMessage.ORDER_NO_UPDATE_DATA);
            }

            context.addArgument(ORDER_ID_KEY, orderId);

            if (!hasNext()) {
                setNextHandler(new FindOrderToUpdate(repository, productService, modificationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order update error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED, exception);
        }
    }

    private boolean isAnyUpdateOrder(final UpdateOrderRequest request) {
        final var isNameUpdated = Objects.nonNull(request.getName());
        final var areNewProductsCreated = CollectionUtils.isNotEmpty(request.getNewProducts());
        final var areProductsUpdated = CollectionUtils.isNotEmpty(request.getOrderedProducts());
        final var areProductsToRemove = CollectionUtils.isNotEmpty(request.getRemoveProducts());

        return isNameUpdated || areNewProductsCreated || areProductsUpdated || areProductsToRemove;
    }

}
