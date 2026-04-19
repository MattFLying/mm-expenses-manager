package mm.expenses.manager.product.processor.decorator;

import mm.expenses.manager.common.web.decorator.PagedDecorator;
import mm.expenses.manager.product.api.product.model.ProductResponse;
import mm.expenses.manager.product.processor.Product;

/**
 * Simple {@link Product} decorator for specific cases.
 */
public abstract class ProductMappingStrategy extends PagedDecorator<Product, ProductResponse> {

    /**
     * Default method to do any necessary validation in implemented decorator.
     */
    protected void validate() {

    }

}
