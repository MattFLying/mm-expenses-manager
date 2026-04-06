package mm.expenses.manager.order.processor.product;

/**
 * Strategy to execute for specific case with ordered products for the given order.
 */
public interface OrderedProductStrategy<T> {

    /**
     * Execute strategy and return expected object type.
     */
    T execute();

    /**
     * Returns true if strategy has been executed.
     */
    boolean executed();

}
