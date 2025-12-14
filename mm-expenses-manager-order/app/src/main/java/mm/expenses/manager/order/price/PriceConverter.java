package mm.expenses.manager.order.price;

import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.order.api.order.model.OrderResponse;
import mm.expenses.manager.order.api.order.model.OrderedProductResponse;
import mm.expenses.manager.order.api.order.model.PriceResponse;
import mm.expenses.manager.order.client.FinanceApiClient;
import mm.expenses.manager.order.config.CurrencyConfig;
import mm.expenses.manager.order.order.Order;
import mm.expenses.manager.order.order.OrderedProduct;
import mm.expenses.manager.order.order.OrderedProductPrice;
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

    public List<OrderedProductResponse> convertPricesFromResponse(final List<OrderedProductResponse> orderedProducts) {
        val defaultCurrency = getDefaultCurrency();
        val productsByCurrency = new HashMap<CurrencyCode, List<OrderedProductResponse>>();
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
                            val priceConverted = updatePriceAfterConversion(resultedOrderedProduct);
                            val prices = orderedProduct.getPrice();
                            val originalPriceOpt = prices.stream()
                                    .filter(price -> price.getIsOriginal() && Objects.equals(price.getCurrency(), priceConverted.getCurrency().getCode()))
                                    .findAny();
                            if (originalPriceOpt.isPresent()) {
                                val originalPrice = originalPriceOpt.get();
                                originalPrice.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                                originalPrice.setCurrency(priceConverted.getCurrency().getCode());
                                orderedProduct.setPriceSummary(updatePriceSummaryAfterConversionInProductResponse(resultedOrderedProduct, orderedProduct.getQuantity()));
                            } else {
                                val priceOfRequestedCurrencyOpt = prices.stream()
                                        .filter(price -> Objects.equals(price.getCurrency(), priceConverted.getCurrency().getCode()))
                                        .findAny();
                                if (priceOfRequestedCurrencyOpt.isPresent()) {
                                    val priceOfRequestedCurrency = priceOfRequestedCurrencyOpt.get();
                                    priceOfRequestedCurrency.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                                    priceOfRequestedCurrency.setCurrency(priceConverted.getCurrency().getCode());
                                } else {
                                    val price = new PriceResponse();
                                    price.setIsOriginal(false);
                                    price.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                                    price.setCurrency(priceConverted.getCurrency().getCode());

                                    val pricesResponse = new ArrayList<PriceResponse>();
                                    pricesResponse.add(price);

                                    orderedProduct.setPrice(pricesResponse);
                                    orderedProduct.setPriceSummary(updatePriceSummaryAfterConversionInProductResponse(resultedOrderedProduct, orderedProduct.getQuantity()));
                                }
                            }
                        });
            });
        }
        return orderedProducts;
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
                            val priceConverted = updatePriceAfterConversion(resultedOrderedProduct);
                            val prices = orderedProduct.getPrices();
                            val originalPriceOpt = prices.stream()
                                    .filter(price -> price.isPriceOriginal() && Objects.equals(price.getCurrency(), priceConverted.getCurrency()))
                                    .findAny();
                            if (originalPriceOpt.isPresent()) {
                                val originalPrice = originalPriceOpt.get();
                                originalPrice.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                                originalPrice.setCurrency(priceConverted.getCurrency());
                                orderedProduct.setPriceSummary(updatePriceSummaryAfterConversion(resultedOrderedProduct, orderedProduct.getQuantity()));
                            } else {
                                val priceOfRequestedCurrencyOpt = prices.stream()
                                        .filter(price -> Objects.equals(price.getCurrency(), priceConverted.getCurrency()))
                                        .findAny();
                                if (priceOfRequestedCurrencyOpt.isPresent()) {
                                    val priceOfRequestedCurrency = priceOfRequestedCurrencyOpt.get();
                                    priceOfRequestedCurrency.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                                    priceOfRequestedCurrency.setCurrency(priceConverted.getCurrency());
                                } else {
                                    val price = new OrderedProductPrice();
                                    price.setOrderedProduct(orderedProduct);
                                    price.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                                    price.setCurrency(priceConverted.getCurrency());
                                    price.setPriceOriginal(false);
                                    price.setPriceCustom(false);
                                    price.setDeleted(orderedProduct.isDeleted());

                                    orderedProduct.addPrice(price);
                                    orderedProduct.setPriceSummary(updatePriceSummaryAfterConversion(resultedOrderedProduct, orderedProduct.getQuantity()));
                                }
                            }
                        });
            });
        }
        return orderedProducts;
    }

    public void pricesConversion(final Boolean shouldConvertCurrency, final OrderResponse order) {
        if (Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency) {
            // calculate prices if different currencies to default currency
            val defaultCurrency = getDefaultCurrency();
            val isCurrencyConversionNeeded = order.getOrderedProducts()
                    .stream()
                    .anyMatch(orderedProduct -> orderedProduct.getPrice()
                            .stream()
                            .anyMatch(price -> (price.getIsOriginal() && !Objects.equals(price.getCurrency(), defaultCurrency.getCode())) || !Objects.equals(price.getCurrency(), defaultCurrency.getCode()))
                    );
            if (isCurrencyConversionNeeded) {
                val convertedProducts = convertPricesFromResponse(order.getOrderedProducts());
                order.setOrderedProducts(convertedProducts);
                order.setPriceSummary(updateOrderPriceSummaryAfterConversion(convertedProducts));
            }
        }
    }

    public void pricesConversion(final Boolean shouldConvertCurrency, final Order order) {
        if (Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency) {
            // calculate prices if different currencies to default currency
            val defaultCurrency = getDefaultCurrency();
            val isCurrencyConversionNeeded = order.getProducts()
                    .stream()
                    .anyMatch(orderedProduct -> orderedProduct.getPrices()
                            .stream()
                            .anyMatch(price -> (price.isPriceOriginal() && !Objects.equals(price.getCurrency(), defaultCurrency)) || !Objects.equals(price.getCurrency(), defaultCurrency))
                    );
            if (isCurrencyConversionNeeded) {
                val convertedProducts = convertPrices(order.getProducts());
                order.setProducts(convertedProducts);
                order.setPriceSummary(Prices.calculatePriceSummary(convertedProducts, defaultCurrency));
            }
        }
    }

    public Map<UUID, List<OrderedProduct>> convertPricesByOrderId(final Map<UUID, List<OrderedProduct>> productsByOrderId) {
        val defaultCurrency = getDefaultCurrency();
        val currencyConversionRequests = productsByOrderId.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(orderedProduct -> !Objects.equals(
                        orderedProduct.getPrices()
                                .stream()
                                .filter(OrderedProductPrice::isPriceOriginal)
                                .findAny()
                                .map(OrderedProductPrice::getCurrency)
                                .orElse(CurrencyCode.UNDEFINED),
                        defaultCurrency
                ))
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
                                val priceConverted = updatePriceAfterConversion(currencyConversionResponse);
                                val originalPriceOpt = orderedProduct.getPrices()
                                        .stream()
                                        .filter(price -> price.isPriceOriginal() || Objects.equals(price.getCurrency(), priceConverted.getCurrency()))
                                        .findAny();
                                if (originalPriceOpt.isPresent()) {
                                    val originalPrice = originalPriceOpt.get();
                                    originalPrice.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                                    originalPrice.setCurrency(priceConverted.getCurrency());
                                    orderedProduct.setPriceSummary(updatePriceSummaryAfterConversion(currencyConversionResponse, orderedProduct.getQuantity()));
                                }
                            }
                        });
                    });
        }
        return productsByOrderId;
    }

    public BigDecimal getPricesSummary(final List<OrderedProduct> products, final CurrencyCode currency) {
        return products.stream()
                .map(orderedProduct -> orderedProduct.getPriceSummary(currency))
                .findAny()
                .map(Price::getValue)
                .orElse(BigDecimal.ZERO);
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

    private List<PriceResponse> updatePriceSummaryAfterConversionInProductResponse(final CurrencyConversionResponse resultedOrderedProduct, final Double quantity) {
        val price = Price.multiply(
                CurrencyCode.getCurrencyFromString(resultedOrderedProduct.getTo().getCode()),
                BigDecimalWrapper.of(resultedOrderedProduct.getTo().getValue()),
                quantity,
                DateUtils.localDateToInstant(resultedOrderedProduct.getDate())
        );

        val response = new ArrayList<PriceResponse>();
        response.add(mapper.mapPrice(price));

        return response;
    }

    private List<PriceResponse> updateOrderPriceSummaryAfterConversion(final List<OrderedProductResponse> convertedProducts) {
        val response = new ArrayList<PriceResponse>();
        val pricesByCurrency = convertedProducts.stream()
                .map(OrderedProductResponse::getPriceSummary)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(PriceResponse::getCurrency));

        pricesByCurrency.forEach((currency, prices) -> {
            val price = new PriceResponse();
            price.setValue(prices.stream().map(PriceResponse::getValue).reduce(BigDecimal.ZERO, BigDecimal::add));
            price.setCurrency(currency);

            response.add(price);
        });
        return response;
    }

    private boolean isTheConversionResponseSameAsOrderedProduct(final OrderedProduct orderedProduct, final CurrencyConversionResponse conversionResponse) {
        return StringUtils.equals(conversionResponse.getId(), orderedProduct.getId().toString());
    }

    private boolean isTheConversionResponseSameAsOrderedProduct(final OrderedProductResponse orderedProduct, final CurrencyConversionResponse conversionResponse) {
        return StringUtils.equals(conversionResponse.getId(), orderedProduct.getId().toString());
    }

}
