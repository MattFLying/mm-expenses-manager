package mm.expenses.manager.order.processor.decorator;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.order.api.order.model.OrderResponse;
import mm.expenses.manager.order.api.order.model.PriceResponse;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.price.PriceConverter;
import mm.expenses.manager.order.core.OrderedProductPrice;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Mapper from {@link Order} to {@link OrderResponse} with converted prices for both order and ordered products.
 */
@RequiredArgsConstructor
public class OrderClassicMapperToResponseWithConversionDecorator extends OrderMappingStrategy {

    private final PriceConverter converter;
    private final OrderMappingStrategy decorator;
    private final Boolean shouldConvertCurrency;

    @Override
    public OrderResponse decorate(final Order order) {
        validate();

        final var response = decorator.decorate(order);
        pricesConversion(response);

        return response;
    }

    @Override
    public Page<OrderResponse> decorate(final Page<Order> pagedOrders) {
        validate();
        pricesConversion(pagedOrders);

        return decorator.decorate(pagedOrders);
    }

    @Override
    protected void validate() {
        if (Objects.isNull(decorator)) {
            throw new IllegalArgumentException("Order decorator is null");
        }
    }

    private boolean shouldConvertCurrency() {
        return Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency;
    }

    private boolean isConversionNeeded(final PriceResponse price, final CurrencyCode defaultCurrency) {
        return (price.getIsOriginal() && !Objects.equals(price.getCurrency(), defaultCurrency.getCode())) || !Objects.equals(price.getCurrency(), defaultCurrency.getCode());
    }

    private boolean shouldConvertForMultiplePrices(final OrderedProductPrice price) {
        return (price.isPriceOriginal() && !Objects.equals(price.getCurrency(), converter.getDefaultCurrency())) || !Objects.equals(price.getCurrency(), converter.getDefaultCurrency());
    }

    private void pricesConversion(final OrderResponse order) {
        if (shouldConvertCurrency()) {
            // calculate prices if different currencies to default currency
            final var defaultCurrency = converter.getDefaultCurrency();
            final var isCurrencyConversionNeeded = order.getOrderedProducts()
                    .stream()
                    .anyMatch(orderedProduct -> orderedProduct.getPrice()
                            .stream()
                            .anyMatch(price -> isConversionNeeded(price, defaultCurrency))
                    );
            if (isCurrencyConversionNeeded) {
                final var convertedProducts = converter.convertPricesFromResponse(order.getOrderedProducts());
                order.setOrderedProducts(convertedProducts);
                order.setPriceSummary(converter.updateOrderPriceSummaryAfterConversion(convertedProducts));
            }
        }
    }

    private void pricesConversion(final Page<Order> pagedOrders) {
        if (shouldConvertCurrency()) {
            final var productsByOrderId = pagedOrders.getContent()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Order::getId,
                            Collectors.flatMapping(order -> order.getProducts().stream(), Collectors.toList())
                    ));

            // calculate prices if different currencies
            final var isCurrencyConversionNeeded = productsByOrderId.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .anyMatch(orderedProduct -> orderedProduct.getPrices()
                            .stream()
                            .anyMatch(this::shouldConvertForMultiplePrices));
            if (isCurrencyConversionNeeded) {
                final var convertedOrders = converter.convertPricesByOrderId(productsByOrderId);
                pagedOrders.getContent()
                        .forEach(order -> {
                            final var orderId = order.getId();
                            final var productsByOrder = convertedOrders.getOrDefault(orderId, order.getProducts());

                            order.setProducts(productsByOrder);
                            order.setPriceSummary(Prices.calculatePriceSummary(productsByOrder));
                        });
            }
        }
    }

}
