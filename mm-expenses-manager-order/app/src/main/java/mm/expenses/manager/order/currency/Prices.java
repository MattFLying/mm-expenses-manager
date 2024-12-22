package mm.expenses.manager.order.currency;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.order.order.OrderedProduct;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prices extends ArrayList<Price> implements Serializable {

    public Prices() {
        super();
    }

    public Prices(Price price) {
        super(List.of(price));
    }

    public Prices(List<Price> prices) {
        super(prices);
    }

    public boolean containsCurrency(final CurrencyCode currency) {
        return stream().anyMatch(price -> price.getCurrency().equals(currency));
    }

    public boolean containsCurrency(final String currencyCode) {
        return containsCurrency(CurrencyCode.getCurrencyFromString(currencyCode));
    }

    public static Prices of(final List<Price> prices) {
        val map = new HashMap<CurrencyCode, Price>();
        prices.forEach(price -> {
            if (!map.containsKey(price.getCurrency())) {
                map.put(price.getCurrency(), price);
            } else {
                map.compute(price.getCurrency(), (currencyCode, originalPrice) -> accumulatePrices(originalPrice, price));
            }
        });
        return new Prices(map.values().stream().toList());
    }

    public static Prices multiply(final Prices prices, final Double quantity) {
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(prices) && Objects.nonNull(quantity)) {
            return new Prices(prices.stream().map(price -> Price.multiply(price, quantity)).toList());
        }
        return new Prices();
    }

    public static Prices calculatePriceSummary(final Collection<OrderedProduct> products) {
        if (CollectionUtils.isEmpty(products)) {
            return new Prices();
        }
        return Prices.of(
                products.stream()
                        .map(OrderedProduct::getPriceSummary)
                        .flatMap(Collection::stream)
                        .toList()
        );
    }

    private static Price accumulatePrices(final Price first, final Price second) {
        return Price.add(first, second);
    }

}
