package mm.expenses.manager.order.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.order.core.OrderedProduct;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.processor.product.CreateOrderedProduct;
import mm.expenses.manager.order.processor.product.FindAllRequestedProducts;
import mm.expenses.manager.order.price.PriceConverter;
import mm.expenses.manager.order.product.Product;
import mm.expenses.manager.order.product.ProductService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Responsible for {@link OrderedProduct} operations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderedProductService {

    private final ProductService productService;
    private final PriceConverter priceConverter;

    public OrderedProduct create(final CreateNewOrderedProductRequest request, final Product product, final Instant date) {
        return new CreateOrderedProduct(request, product, date, getDefaultCurrency()).execute();
    }

    public Map<UUID, Product> findAllRequestedProducts(final Set<UUID> requestedProductIds) {
        return new FindAllRequestedProducts(productService, requestedProductIds).execute();
    }

    public CurrencyCode getDefaultCurrency() {
        return priceConverter.getDefaultCurrency();
    }

}
