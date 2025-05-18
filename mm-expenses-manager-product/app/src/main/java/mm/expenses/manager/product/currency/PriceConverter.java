package mm.expenses.manager.product.currency;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.product.client.FinanceApiClient;
import mm.expenses.manager.product.config.CurrencyConfig;
import mm.expenses.manager.product.product.ProductFilterView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Prices converter based on finances service allow to convert specific currencies to expected currency.
 */
@Component
@RequiredArgsConstructor
public class PriceConverter {

    private final FinanceApiClient client;
    private final CurrencyConfig config;
    private final CurrencyMapper mapper;

    /**
     * @return default currency in use.
     */
    public CurrencyCode getDefaultCurrency() {
        return config.getDefaultCurrency();
    }

    /**
     * @return passed products as parameter with converted prices to default currency.
     */
    public List<ProductFilterView> convertPrices(final List<ProductFilterView> products) {
        val defaultCurrency = getDefaultCurrency();
        val currencyConversionRequests = products.stream()
                .filter(product -> !Objects.equals(product.getPriceCurrency(), defaultCurrency))
                .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(ProductFilterView::getProductId))))
                .stream()
                .map(product -> mapper.map(product, config.getDefaultCurrency()))
                .toList();

        val convertedPrices = client.convertMultipleRates(currencyConversionRequests);
        products.forEach(product -> convertedPrices.stream()
                .filter(conversionResponse -> isTheConversionResponseSameAsProduct(product, conversionResponse))
                .findAny()
                .ifPresent(resultedProduct -> {
                    product.setPriceValue(BigDecimalWrapper.of(resultedProduct.getTo().getValue()));
                    product.setPriceCurrency(CurrencyCode.getCurrencyFromString(resultedProduct.getTo().getCode()));
                    product.setPriceCreatedAt(DateUtils.localDateToInstant(resultedProduct.getDate()));
                })
        );
        return products;
    }

    private boolean isTheConversionResponseSameAsProduct(final ProductFilterView product, final CurrencyConversionResponse conversionResponse) {
        return StringUtils.equals(conversionResponse.getId(), product.getProductId().toString()) && Objects.equals(product.getPriceCurrency().getCode(), conversionResponse.getFrom().getCode());
    }

}
