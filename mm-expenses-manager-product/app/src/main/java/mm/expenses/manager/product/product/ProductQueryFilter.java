package mm.expenses.manager.product.product;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Filter for querying products.
 */
public record ProductQueryFilter(String name,
                                 BigDecimal price,
                                 BigDecimal priceMin,
                                 BigDecimal priceMax,
                                 Boolean lessThan,
                                 Boolean greaterThan) {

    private static final String FILTER_DELIMITER = "_";
    static final String NAME_PROPERTY = "name";
    static final String PRICE_PROPERTY = "price";
    static final String PRICE_MIN_PROPERTY = "priceMin";
    static final String PRICE_MAX_PROPERTY = "priceMax";
    static final String PRICE_LESS_THAN_PROPERTY = "lessThan";
    static final String PRICE_GREATER_THAN_PROPERTY = "greaterThan";

    /**
     * @return checks if price min is not null.
     */
    public boolean isPriceMin() {
        return Objects.nonNull(priceMin);
    }

    /**
     * @return checks if price max is not null.
     */
    public boolean isPriceMax() {
        return Objects.nonNull(priceMax);
    }

    /**
     * @return checks if price is not null.
     */
    public boolean isPriceOriented() {
        return Objects.nonNull(price);
    }

    /**
     * @return checks if price min or price max is not null.
     */
    public boolean isAnyOfPriceRangeUsed() {
        return isPriceMin() || isPriceMax();
    }

    /**
     * @return checks if price min and price max is not null.
     */
    public boolean isPriceRangeOriented() {
        return isPriceMin() && isPriceMax();
    }

    /**
     * @return checks if price min and price max and price is not null.
     */
    public boolean isPriceAndPriceRangeOriented() {
        return isPriceOriented() && isAnyOfPriceRangeUsed();
    }

    /**
     * @return checks if price less and greater than is not null.
     */
    public boolean isPriceLessAndGreaterUsed() {
        return (Objects.nonNull(lessThan) && lessThan) && (Objects.nonNull(greaterThan) && greaterThan);
    }

    /**
     * @return checks if price less or greater than is not null.
     */
    public boolean isPriceLessOrGreaterUsed() {
        return (Objects.nonNull(lessThan) && lessThan) || (Objects.nonNull(greaterThan) && greaterThan);
    }

    /**
     * @return finds correct filter type by current settings.
     */
    public Filter findFilter() {
        final var joiner = new StringJoiner(FILTER_DELIMITER);
        try {
            if (shouldBeAll()) {
                return Filter.ALL;
            }

            if (StringUtils.isNotBlank(name)) {
                joiner.add(Filter.NAME.name());
            }
            if (isPriceRangeOriented()) {
                joiner.add(Filter.PRICE_RANGE.name());
            } else {
                if (isLessThan()) {
                    joiner.add(Filter.PRICE_LESS_THAN.name());
                } else if (isGreaterThan()) {
                    joiner.add(Filter.PRICE_GREATER_THAN.name());
                }
            }
            return Filter.valueOf(joiner.toString().toUpperCase());
        } catch (final IllegalArgumentException exception) {
            return Filter.ALL;
        }
    }

    private boolean shouldBeAll() {
        return StringUtils.isBlank(name) && Objects.isNull(priceMin) && Objects.isNull(priceMax) && Objects.isNull(price) && (Objects.isNull(lessThan) || !lessThan) && (Objects.isNull(greaterThan) || !greaterThan);
    }

    private boolean isLessThan() {
        final var isLessThan = Objects.nonNull(lessThan) && lessThan;

        return isPriceOriented() && isLessThan;
    }

    private boolean isGreaterThan() {
        final var isGreaterThan = Objects.nonNull(greaterThan) && greaterThan;

        return isPriceOriented() && isGreaterThan;
    }

    /**
     * Simple filter enum of available filters for products.
     */
    public enum Filter {
        ALL, NAME, PRICE_RANGE, PRICE_LESS_THAN, PRICE_GREATER_THAN, NAME_PRICE_RANGE, NAME_PRICE_LESS_THAN, NAME_PRICE_GREATER_THAN
    }

}
