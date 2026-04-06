package mm.expenses.manager.order.price;

import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.order.api.order.model.OrderedProductResponse;
import mm.expenses.manager.order.api.order.model.PriceResponse;
import mm.expenses.manager.order.client.FinanceApiClient;
import mm.expenses.manager.order.config.CurrencyConfig;
import mm.expenses.manager.order.processor.OrderedProduct;
import mm.expenses.manager.order.processor.OrderedProductPrice;
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
        final var defaultCurrency = getDefaultCurrency();
        final var productsByCurrency = new HashMap<CurrencyCode, List<OrderedProductResponse>>();
        orderedProducts.forEach(orderedProduct -> {
            final var price = mapper.mapTo(orderedProduct);
            productsByCurrency.computeIfAbsent(
                    price.getCurrency(),
                    k -> new ArrayList<>()
            ).add(orderedProduct);
        });

        if (productsByCurrency.size() == 1 && productsByCurrency.containsKey(defaultCurrency)) {
            return orderedProducts;
        }

        if (productsByCurrency.size() > 1 || !productsByCurrency.containsKey(defaultCurrency)) {
            final var currencyConversionRequests = productsByCurrency.entrySet()
                    .stream()
                    .filter(entry -> !entry.getKey().equals(defaultCurrency))
                    .flatMap(entry -> entry.getValue().stream())
                    .map(orderedProduct -> mapper.map(orderedProduct, defaultCurrency))
                    .flatMap(Collection::stream)
                    .toList();

            final var convertedPrices = client.convertMultipleRates(currencyConversionRequests);

            orderedProducts.forEach(orderedProduct -> {
                convertedPrices.stream()
                        .filter(conversionResponse -> isTheConversionResponseSameAsOrderedProduct(orderedProduct, conversionResponse))
                        .findAny()
                        .ifPresent(resultedOrderedProduct -> convert(orderedProduct, resultedOrderedProduct));
            });
        }
        return orderedProducts;
    }

    public Map<UUID, List<OrderedProduct>> convertPricesByOrderId(final Map<UUID, List<OrderedProduct>> productsByOrderId) {
        final var defaultCurrency = getDefaultCurrency();
        final var currencyConversionRequests = productsByOrderId.values()
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
            final var convertedPricesForOrderedProducts = client.convertMultipleRates(currencyConversionRequests);

            // grouped by ordered product id
            final var orderedProductsByIdsFromResponse = convertedPricesForOrderedProducts.stream()
                    .collect(Collectors.toMap(currencyConversionResponse -> UUID.fromString(currencyConversionResponse.getId()), Function.identity()));

            productsByOrderId.values()
                    .forEach(orderedProducts -> orderedProducts.forEach(orderedProduct -> convert(orderedProduct, orderedProductsByIdsFromResponse)));
        }
        return productsByOrderId;
    }

    public List<PriceResponse> updateOrderPriceSummaryAfterConversion(final List<OrderedProductResponse> convertedProducts) {
        final var response = new ArrayList<PriceResponse>();
        final var pricesByCurrency = convertedProducts.stream()
                .map(OrderedProductResponse::getPriceSummary)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(PriceResponse::getCurrency));

        pricesByCurrency.forEach((currency, prices) -> {
            final var price = new PriceResponse();
            price.setValue(prices.stream().map(PriceResponse::getValue).reduce(BigDecimal.ZERO, BigDecimal::add));
            price.setCurrency(currency);

            response.add(price);
        });
        return response;
    }

    private void convert(final OrderedProductResponse orderedProduct, final CurrencyConversionResponse resultedOrderedProduct) {
        final var priceConverted = updatePriceAfterConversion(resultedOrderedProduct);
        final var prices = orderedProduct.getPrice();
        final var originalPriceOpt = prices.stream()
                .filter(price -> price.getIsOriginal() && Objects.equals(price.getCurrency(), priceConverted.getCurrency().getCode()))
                .findAny();

        if (originalPriceOpt.isPresent()) {
            final var originalPrice = originalPriceOpt.get();
            originalPrice.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
            originalPrice.setCurrency(priceConverted.getCurrency().getCode());
            orderedProduct.setPriceSummary(updatePriceSummaryAfterConversionInProductResponse(resultedOrderedProduct, orderedProduct.getQuantity()));
        } else {
            final var priceOfRequestedCurrencyOpt = prices.stream()
                    .filter(price -> Objects.equals(price.getCurrency(), priceConverted.getCurrency().getCode()))
                    .findAny();

            priceOfRequestedCurrencyOpt.ifPresentOrElse(
                    priceOfRequestedCurrency -> {
                        priceOfRequestedCurrency.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                        priceOfRequestedCurrency.setCurrency(priceConverted.getCurrency().getCode());
                    },
                    () -> {
                        final var price = new PriceResponse();
                        price.setIsOriginal(false);
                        price.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                        price.setCurrency(priceConverted.getCurrency().getCode());

                        final var pricesResponse = new ArrayList<PriceResponse>();
                        pricesResponse.add(price);

                        orderedProduct.setPrice(pricesResponse);
                        orderedProduct.setPriceSummary(updatePriceSummaryAfterConversionInProductResponse(resultedOrderedProduct, orderedProduct.getQuantity()));
                    }
            );
        }
    }

    private void convert(final OrderedProduct orderedProduct, final Map<UUID, CurrencyConversionResponse> orderedProductsByIdsFromResponse) {
        if (orderedProductsByIdsFromResponse.containsKey(orderedProduct.getId())) {
            final var currencyConversionResponse = orderedProductsByIdsFromResponse.get(orderedProduct.getId());
            final var priceConverted = updatePriceAfterConversion(currencyConversionResponse);
            final var originalPriceOpt = orderedProduct.getPrices()
                    .stream()
                    .filter(price -> price.isPriceOriginal() || Objects.equals(price.getCurrency(), priceConverted.getCurrency()))
                    .findAny();
            if (originalPriceOpt.isPresent()) {
                final var originalPrice = originalPriceOpt.get();
                originalPrice.setValue(BigDecimalWrapper.of(priceConverted.getValue()));
                originalPrice.setCurrency(priceConverted.getCurrency());
                orderedProduct.setPriceSummary(updatePriceSummaryAfterConversion(currencyConversionResponse, orderedProduct.getQuantity()));
            }
        }
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
        final var price = Price.multiply(
                CurrencyCode.getCurrencyFromString(resultedOrderedProduct.getTo().getCode()),
                BigDecimalWrapper.of(resultedOrderedProduct.getTo().getValue()),
                quantity,
                DateUtils.localDateToInstant(resultedOrderedProduct.getDate())
        );

        final var response = new ArrayList<PriceResponse>();
        response.add(mapper.mapPrice(price));

        return response;
    }

    private boolean isTheConversionResponseSameAsOrderedProduct(final OrderedProductResponse orderedProduct, final CurrencyConversionResponse conversionResponse) {
        return StringUtils.equals(conversionResponse.getId(), orderedProduct.getId().toString());
    }

}
