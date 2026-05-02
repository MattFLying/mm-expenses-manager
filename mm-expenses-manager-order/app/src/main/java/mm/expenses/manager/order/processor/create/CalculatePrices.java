package mm.expenses.manager.order.processor.create;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.core.*;
import mm.expenses.manager.order.exception.OrderExceptionMessage;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Step in chain of new {@link Order} creation.
 * Calculates {@link OrderPrice}s based on already defined {@link OrderedProduct}s for expected order.
 */
@Slf4j
@RequiredArgsConstructor
final class CalculatePrices extends CreateOrderChain {

    final static String CALCULATED_PRICES_FOR_ORDERED_PRODUCTS_KEY = "calculatedOrderProductsPrices";

    private final OrderRepository repository;
    private final Instant creationTime;

    @Override
    public Order handleRequest(final CreateNewOrderRequest request) {
        try {
            final var orderedProductsOpt = context.getArgument(MapOrderedProducts.ORDERED_PRODUCTS_KEY);
            orderedProductsOpt.ifPresentOrElse(
                    orderedProducts -> {
                        final var orderPrices = new ArrayList<OrderPrice>();

                        calculateOrderPrices((List<OrderedProduct>) orderedProducts, orderPrices, creationTime);
                        updateContext((List<OrderedProduct>) orderedProducts, orderPrices);
                    },
                    () -> {
                        log.error("Cannot create order because of one of required parameters is null. orderedProductsOpt={}", orderedProductsOpt);
                        throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED);
                    }
            );

            if (!hasNext()) {
                setNextHandler(new SaveCreatedOrder(repository, creationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown order creation error occurred.", exception);
            throw new ApiConflictException(OrderExceptionMessage.ORDER_CANNOT_BE_CREATED, exception);
        }
    }

    private void calculateOrderPrices(final List<OrderedProduct> orderedProducts, final List<OrderPrice> orderPrices, final Instant creationDate) {
        orderedProducts.stream()
                .collect(Collectors.groupingBy(
                        orderedProduct -> {
                            final var prices = orderedProduct.getPrices();
                            final var originalPrice = prices.stream()
                                    .filter(OrderedProductPrice::isPriceOriginal)
                                    .findAny();

                            return originalPrice.isPresent()
                                    ? originalPrice.get().getCurrency()
                                    : prices.get(0).getCurrency();
                        }
                )).forEach((currency, products) -> orderPrices.add(calculateNewOrderPrice(products, currency, creationDate)));
    }

    public OrderPrice calculateNewOrderPrice(final List<OrderedProduct> products, final CurrencyCode expectedCurrency, final Instant creationDate) {
        return OrderPrice.builder()
                .value(getPricesSummary(products, expectedCurrency))
                .currency(expectedCurrency)
                .isPriceOriginal(true)
                .isPriceConverted(false)
                .createdAt(creationDate)
                .lastModifiedAt(creationDate)
                .date(DateUtils.instantToLocalDate(creationDate).toString())
                .build();
    }

    public BigDecimal getPricesSummary(final List<OrderedProduct> products, final CurrencyCode currency) {
        return products.stream()
                .map(orderedProduct -> orderedProduct.getPriceSummary(currency))
                .findAny()
                .map(Price::getValue)
                .orElse(BigDecimal.ZERO);
    }

    private void updateContext(final List<OrderedProduct> orderedProducts, final List<OrderPrice> orderPrices) {
        context.addArguments(
                Map.of(MapOrderedProducts.ORDERED_PRODUCTS_KEY, orderedProducts,
                        CALCULATED_PRICES_FOR_ORDERED_PRODUCTS_KEY, orderPrices
                )
        );
    }

}
