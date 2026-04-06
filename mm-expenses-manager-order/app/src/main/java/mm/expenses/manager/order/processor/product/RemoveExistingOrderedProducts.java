package mm.expenses.manager.order.processor.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.order.processor.Order;

import java.util.*;

/**
 * Remove ordered products strategy.
 */
@RequiredArgsConstructor
public class RemoveExistingOrderedProducts implements OrderedProductStrategy<Order> {

    private final Order order;
    private final List<UUID> request;
    private boolean executed = false;

    @Override
    public Order execute() {
        executed = order.getProducts()
                .removeIf(orderedProduct -> request.contains(orderedProduct.getId()));

        return order;
    }

    @Override
    public boolean executed() {
        return executed;
    }

}
