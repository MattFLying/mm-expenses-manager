package mm.expenses.manager.common.utils.price;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.val;
import mm.expenses.manager.common.utils.exception.PriceIllegalArgumentException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Common implementation of multiple prices to be reused whereas is needed with expected possible operations.
 * Contains a simple list of {@link Price}s.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Prices extends ArrayList<Price> implements Serializable {

    public Prices() {
        super();
    }

    public Prices(final Price price) {
        super(List.of(price));
    }

    /**
     * Add all prices to the list unless there are duplicated prices by currency then throw {@link PriceIllegalArgumentException}
     */
    public Prices(final List<Price> prices) {
        this();

        val pricesOccurrence = prices.stream().collect(Collectors.groupingBy(Price::getCurrency, Collectors.toList()));
        if (pricesOccurrence.values().stream().anyMatch(price -> price.size() > 1)) {
            throw new PriceIllegalArgumentException();
        }
        addAll(prices);
    }

    @JsonIgnore
    public boolean containsCurrency(final CurrencyCode currency) {
        return stream().anyMatch(price -> price.getCurrency().equals(currency));
    }

    @JsonIgnore
    public boolean containsCurrency(final String currencyCode) {
        return containsCurrency(CurrencyCode.getCurrencyFromString(currencyCode));
    }

    @JsonIgnore
    public static Prices of(final List<Price> prices) {
        val map = new LinkedHashMap<CurrencyCode, Price>();
        prices.forEach(price -> {
            if (!map.containsKey(price.getCurrency())) {
                map.put(price.getCurrency(), price);
            } else {
                map.compute(price.getCurrency(), (currencyCode, originalPrice) -> accumulatePrices(originalPrice, price));
            }
        });
        return new Prices(map.values().stream().toList());
    }

    @JsonIgnore
    public static Prices multiply(final Prices prices, final Double quantity) {
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(prices) && Objects.nonNull(quantity)) {
            return new Prices(prices.stream().map(price -> Price.multiply(price, quantity)).toList());
        }
        return new Prices();
    }

    /**
     * Calculates prices summary for given objects that implements {@link PriceSummary} interface.
     *
     * @param objects - some objects that implements {@link PriceSummary} interface with own interpretation of prices to be summarized here
     * @param <T>     - specific object that implements {@link PriceSummary} interface
     */
    @JsonIgnore
    public static <T extends PriceSummary> Prices calculatePriceSummary(final Collection<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return new Prices();
        }
        return Prices.of(
                objects.stream()
                        .map(PriceSummary::getPriceSummary)
                        .flatMap(Collection::stream)
                        .toList()
        );
    }

    @JsonIgnore
    private static Price accumulatePrices(final Price first, final Price second) {
        return Price.add(first, second);
    }

}
