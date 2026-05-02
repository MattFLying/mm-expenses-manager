package mm.expenses.manager.product.processor;

import mm.expenses.manager.product.core.ProductMapper;
import mm.expenses.manager.product.core.ProductRepository;
import mm.expenses.manager.product.currency.PriceConverter;

/**
 * Default handler definition to process specific handlers.
 */
public abstract class ProductHandler extends BaseProductHandler {

    protected final ProductRepository repository;

    public ProductHandler(final ProductRepository repository, final ProductMapper mapper, final PriceConverter priceConverter) {
        super(mapper, priceConverter);

        this.repository = repository;
    }

}
