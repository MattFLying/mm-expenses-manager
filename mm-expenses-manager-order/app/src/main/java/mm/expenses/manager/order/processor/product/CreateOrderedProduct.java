package mm.expenses.manager.order.processor.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.exception.OrderExceptionMessage;
import mm.expenses.manager.order.processor.OrderedProduct;
import mm.expenses.manager.order.processor.OrderedProductPrice;
import mm.expenses.manager.order.product.Product;
import mm.expenses.manager.order.product.ProductPrice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Ordered products strategy responsible for ordered product creation.
 */
@RequiredArgsConstructor
public class CreateOrderedProduct implements OrderedProductStrategy<OrderedProduct> {

    private final CreateNewOrderedProductRequest request;
    private final Product product;
    private final Instant date;
    private final CurrencyCode defaultCurrency;
    private boolean executed = false;

    @Override
    public OrderedProduct execute() {
        final var orderedProduct = new OrderedProduct();
        orderedProduct.setProduct(product);
        orderedProduct.setQuantity(request.getQuantity());

        final var price = new OrderedProductPrice();
        price.setOrderedProduct(orderedProduct);
        price.setPriceOriginal(true);
        price.setPriceConverted(false);

        configureDate(orderedProduct, price);
        configureProductPrice(price);

        final var prices = new ArrayList<OrderedProductPrice>();
        prices.add(price);

        orderedProduct.setPrices(prices);
        executed = true;

        return orderedProduct;
    }

    @Override
    public boolean executed() {
        return executed;
    }

    private void configureDate(final OrderedProduct orderedProduct, final OrderedProductPrice price) {
        if (Objects.nonNull(date)) {
            orderedProduct.setCreatedAt(date);
            orderedProduct.setLastModifiedAt(date);

            price.setCreatedAt(date);
            price.setLastModifiedAt(date);
            price.setDate(DateUtils.instantToLocalDate(date).toString());
        }
    }

    private void configureProductPrice(final OrderedProductPrice price) {
        final var requestedPrice = request.getPrice();
        if (Objects.nonNull(requestedPrice)) {
            if (Objects.isNull(requestedPrice.getValue())) {
                throw new ApiValidationException(OrderExceptionMessage.ORDER_PRODUCTS_CUSTOM_PRICE_VALUE_MISSING.withParameters(request.getProductId()));
            }

            final var currency = Objects.isNull(requestedPrice.getCurrency())
                    ? defaultCurrency
                    : CurrencyCode.getCurrencyFromString(requestedPrice.getCurrency());

            price.setValue(requestedPrice.getValue());
            price.setCurrency(currency);
            price.setPriceCustom(true);
        } else {
            final var prices = product.getPrices();
            final var productPrice = prices.stream()
                    .filter(ProductPrice::isOriginal)
                    .findAny()
                    .orElseGet(() -> {
                        final var defaultCurrencyPrice = prices.get(defaultCurrency);

                        // in case if original price for this product does not exist, find the price of current
                        // default currency, otherwise returns the first available price
                        return Objects.nonNull(defaultCurrencyPrice) ? defaultCurrencyPrice : prices.get(0);
                    });

            price.setValue(productPrice.getValue());
            price.setCurrency(productPrice.getCurrency());
            price.setPriceCustom(false);
        }
    }

}
