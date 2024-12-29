package mm.expenses.manager.common.utils.price;

/**
 * Simple interface to be implemented to handle prices summary that may be different for different cases.
 */
@FunctionalInterface
public interface PriceSummary {

    /**
     * Returns prices summary as array because it may contain prices for different currencies.
     */
    Prices getPriceSummary();

}
