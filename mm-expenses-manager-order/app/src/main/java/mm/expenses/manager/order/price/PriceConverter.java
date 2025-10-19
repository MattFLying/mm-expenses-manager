package mm.expenses.manager.order.price;

import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.order.client.FinanceApiClient;
import mm.expenses.manager.order.config.CurrencyConfig;
import mm.expenses.manager.order.order.Order;
import mm.expenses.manager.order.order.OrderedProduct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PriceConverter {

    private final FinanceApiClient client;
    private final CurrencyConfig config;
    private final CurrencyMapper mapper;

    public CurrencyCode getDefaultCurrency() {
        return config.getDefaultCurrency();
    }

    public List<OrderedProduct> convertPrices(final List<OrderedProduct> orderedProducts) {
        val defaultCurrency = getDefaultCurrency();
        val productsByCurrency = new HashMap<CurrencyCode, List<OrderedProduct>>();
        orderedProducts.forEach(orderedProduct -> {
            val price = mapper.mapTo(orderedProduct);
            productsByCurrency.computeIfAbsent(
                    price.getCurrency(),
                    k -> new ArrayList<>()
            ).add(orderedProduct);
        });

        if (productsByCurrency.size() == 1 && productsByCurrency.containsKey(defaultCurrency)) {
            return orderedProducts;
        }

        if (productsByCurrency.size() > 1 || !productsByCurrency.containsKey(defaultCurrency)) {
            val currencyConversionRequests = productsByCurrency.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(defaultCurrency))
                    .flatMap(entry -> entry.getValue().stream())
                    .map(orderedProduct -> mapper.map(orderedProduct, defaultCurrency))
                    .flatMap(Collection::stream)
                    .toList();

            val convertedPrices = client.convertMultipleRates(currencyConversionRequests);

            orderedProducts.forEach(orderedProduct -> {
                convertedPrices.stream()
                        .filter(conversionResponse -> isTheConversionResponseSameAsOrderedProduct(orderedProduct, conversionResponse))
                        .findAny()
                        .ifPresent(resultedOrderedProduct -> {
                            val price = updatePriceAfterConversion(resultedOrderedProduct);
                            orderedProduct.setValue(BigDecimalWrapper.of(price.getValue()));
                            orderedProduct.setCurrency(price.getCurrency());

                            orderedProduct.setPriceSummary(updatePriceSummaryAfterConversion(resultedOrderedProduct, orderedProduct.getQuantity()));
                        });
            });
        }
        return orderedProducts;
    }

    public void pricesConversion(final Boolean shouldConvertCurrency, final Order order) {
        if (Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency) {
            // calculate prices if different currencies to default currency
            val defaultCurrency = getDefaultCurrency();
            val isCurrencyConversionNeeded = order.getProducts()
                    .stream()
                    .anyMatch(orderedProduct -> !defaultCurrency.equals(orderedProduct.getCurrency()) || !orderedProduct.getPriceSummary().containsCurrency(defaultCurrency));
            if (isCurrencyConversionNeeded) {
                val convertedProducts = convertPrices(order.getProducts());
                order.setProducts(convertedProducts);
                order.setPriceSummary(Prices.calculatePriceSummary(convertedProducts));
            }
        }
    }

    public Map<UUID, List<OrderedProduct>> convertPricesByOrderId(final Map<UUID, List<OrderedProduct>> productsByOrderId) {
        val defaultCurrency = getDefaultCurrency();
        val currencyConversionRequests = productsByOrderId.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(orderedProduct -> !Objects.equals(orderedProduct.getCurrency(), defaultCurrency))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(OrderedProduct::getId))))
                .stream()
                .map(orderedProduct -> mapper.map(orderedProduct, config.getDefaultCurrency()))
                .flatMap(Collection::stream)
                .toList();

        if (!currencyConversionRequests.isEmpty()) {
            val convertedPricesForOrderedProducts = client.convertMultipleRates(currencyConversionRequests);

            // grouped by ordered product id
            val orderedProductsByIdsFromResponse = convertedPricesForOrderedProducts.stream()
                    .collect(Collectors.toMap(currencyConversionResponse -> UUID.fromString(currencyConversionResponse.getId()), Function.identity()));

            productsByOrderId.values()
                    .forEach(orderedProducts -> {
                        orderedProducts.forEach(orderedProduct -> {
                            if (orderedProductsByIdsFromResponse.containsKey(orderedProduct.getId())) {
                                val currencyConversionResponse = orderedProductsByIdsFromResponse.get(orderedProduct.getId());
                                val price = updatePriceAfterConversion(currencyConversionResponse);

                                orderedProduct.setValue(BigDecimalWrapper.of(price.getValue()));
                                orderedProduct.setCurrency(price.getCurrency());
                                orderedProduct.setPriceSummary(updatePriceSummaryAfterConversion(currencyConversionResponse, orderedProduct.getQuantity()));
                            }
                        });
                    });
        }
        return productsByOrderId;
    }

    public BigDecimal getPricesSummary(final List<OrderedProduct> products, final CurrencyCode currency) {
        val prices = products.stream()
                .map(OrderedProduct::getPriceSummary)
                .filter(pricesSummary -> pricesSummary.containsCurrency(currency))
                .map(pricesSummary -> pricesSummary.getByCurrency(currency))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        return Prices.of(prices)
                .get(0)
                .getValue();
    }

    private Price updatePriceAfterConversion(final CurrencyConversionResponse resultedOrderedProduct) {
        return new Price(
                CurrencyCode.getCurrencyFromString(resultedOrderedProduct.getTo().getCode()),
                BigDecimalWrapper.of(resultedOrderedProduct.getTo().getValue()),
                DateUtils.localDateToInstant(resultedOrderedProduct.getDate())
        );
    }

    private Prices updatePriceSummaryAfterConversion(final CurrencyConversionResponse resultedOrderedProduct, final Double quantity) {
        return new Prices(
                Price.multiply(
                        CurrencyCode.getCurrencyFromString(resultedOrderedProduct.getTo().getCode()),
                        BigDecimalWrapper.of(resultedOrderedProduct.getTo().getValue()),
                        quantity,
                        DateUtils.localDateToInstant(resultedOrderedProduct.getDate())
                )
        );
    }

    private boolean isTheConversionResponseSameAsOrderedProduct(final OrderedProduct orderedProduct, final CurrencyConversionResponse conversionResponse) {
        return StringUtils.equals(conversionResponse.getId(), orderedProduct.getId().toString());
    }

}
