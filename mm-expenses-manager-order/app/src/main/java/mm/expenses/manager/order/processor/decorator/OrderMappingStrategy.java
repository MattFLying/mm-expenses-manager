package mm.expenses.manager.order.processor.decorator;

import mm.expenses.manager.common.web.decorator.PagedDecorator;
import mm.expenses.manager.order.api.order.model.OrderResponse;
import mm.expenses.manager.order.processor.Order;

/**
 * Simple {@link Order} decorator for specific cases.
 */
public abstract class OrderMappingStrategy extends PagedDecorator<Order, OrderResponse> {

    /**
     * Default method to do any necessary validation in implemented decorator.
     */
    protected void validate() {

    }

}
