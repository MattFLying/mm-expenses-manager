package mm.expenses.manager.order.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.*;
import mm.expenses.manager.order.processor.product.*;
import org.apache.commons.collections4.CollectionUtils;

import java.time.Instant;
import java.util.*;

/**
 * Step in chain of {@link Order} update.
 * Modifies ordered products if any detected.
 */
@Slf4j
@RequiredArgsConstructor
final class ModifyOrderedProductsDuringOrderUpdate extends UpdateOrderChain {

    final static String PRODUCTS_UPDATED_KEY = "areProductsUpdated";

    private final OrderRepository repository;
    private final OrderedProductService productService;
    private final Instant modificationTime;
    private final LinkedList<OrderedProductStrategy<?>> updateProductsStrategies = new LinkedList<>();

    @Override
    public Order handleRequest(final UpdateOrderRequest request) {
        try {
            final var orderOpt = context.getArgument(FindOrderToUpdate.ORDER_KEY);
            orderOpt.ifPresentOrElse(
                    order -> {
                        final var existingProductsToUpdate = request.getOrderedProducts();
                        if (CollectionUtils.isNotEmpty(existingProductsToUpdate)) {
                            updateProductsStrategies.add(new UpdateExistingOrderedProducts((Order) order, existingProductsToUpdate, modificationTime));
                        }

                        final var productsToRemove = request.getRemoveProducts();
                        if (CollectionUtils.isNotEmpty(productsToRemove)) {
                            updateProductsStrategies.add(new RemoveExistingOrderedProducts((Order) order, productsToRemove));
                        }

                        final var productsToAdd = request.getNewProducts();
                        if (CollectionUtils.isNotEmpty(productsToAdd)) {
                            updateProductsStrategies.add(new AddExistingOrderedProducts((Order) order, productsToAdd, productService, modificationTime));
                        }

                        updateProductsStrategies.forEach(OrderedProductStrategy::execute);

                        context.addArgument(PRODUCTS_UPDATED_KEY, updateProductsStrategies.stream().anyMatch(OrderedProductStrategy::executed));
                        context.addArgument(FindOrderToUpdate.ORDER_KEY, order);
                    },
                    () -> {
                        log.error("Cannot update order because of one of required parameters is null. orderOpt={}", orderOpt);
                        throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_UPDATED);
                    }
            );

            if (!hasNext()) {
                setNextHandler(new OrderSummaryUpdateDuringUpdate(repository, modificationTime));
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
