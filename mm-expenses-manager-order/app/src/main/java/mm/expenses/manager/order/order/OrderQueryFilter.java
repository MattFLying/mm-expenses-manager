package mm.expenses.manager.order.order;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Filter for querying orders.
 */
public record OrderQueryFilter(String name,
                               BigDecimal priceSummary,
                               Integer productsCount,
                               Boolean priceSummaryLessThan,
                               Boolean priceSummaryGreaterThan,
                               Boolean productsCountLessThan,
                               Boolean productsCountGreaterThan) {

    private static final String FILTER_DELIMITER = "_";
    static final String IS_DELETED_PROPERTY = "isDeleted";
    static final String NAME_PROPERTY = "name";
    static final String PRODUCTS_COUNT_PROPERTY = "productsCount";
    static final String PRICE_SUMMARY_PROPERTY = "priceSummary";
    static final String PRICE_SUMMARY_LESS_THAN_PROPERTY = "priceSummaryLessThan";
    static final String PRICE_SUMMARY_GREATER_THAN_PROPERTY = "priceSummaryGreaterThan";
    static final String PRODUCTS_COUNT_LESS_THAN_PROPERTY = "productsCountLessThan";
    static final String PRODUCTS_COUNT_GREATER_THAN_PROPERTY = "productsCountGreaterThan";

    /**
     * @return checks if priceSummary is not null.
     */
    public boolean isPriceSummaryOriented() {
        return Objects.nonNull(priceSummary);
    }

    /**
     * @return checks if productsCount is not null.
     */
    public boolean isProductsCountOriented() {
        return Objects.nonNull(productsCount);
    }

    /**
     * @return checks if priceSummary with less or greater flag is used.
     */
    public boolean isPriceSummaryLessOrGreaterOriented() {
        return isPriceSummaryOriented() && ((Objects.nonNull(priceSummaryLessThan) && priceSummaryLessThan) || (Objects.nonNull(priceSummaryGreaterThan) && priceSummaryGreaterThan));
    }

    /**
     * @return checks if priceSummary less and greater than is not null.
     */
    public boolean isPriceSummaryLessAndGreaterUsed() {
        return (Objects.nonNull(priceSummaryLessThan) && priceSummaryLessThan) && (Objects.nonNull(priceSummaryGreaterThan) && priceSummaryGreaterThan);
    }

    /**
     * @return checks if productsCount less and greater than is not null.
     */
    public boolean isProductsCountLessAndGreaterUsed() {
        return (Objects.nonNull(productsCountLessThan) && productsCountLessThan) && (Objects.nonNull(productsCountGreaterThan) && productsCountGreaterThan);
    }

    /**
     * @return checks if productsCount less or greater than is not null.
     */
    public boolean isProductsCountLessOrGreaterUsed() {
        return (Objects.nonNull(productsCountLessThan) && productsCountLessThan) || (Objects.nonNull(productsCountGreaterThan) && productsCountGreaterThan);
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

            if (Objects.nonNull(productsCount) && productsCount >= 0 && !isProductsCountLessOrGreaterUsed()) {
                joiner.add(Filter.PRODUCTS_COUNT.name());
            } else if (isProductsCountLessThan()) {
                joiner.add(Filter.PRODUCTS_COUNT_LESS_THAN.name());
            } else if (isProductsCountGreaterThan()) {
                joiner.add(Filter.PRODUCTS_COUNT_GREATER_THAN.name());
            }

            if (isPriceSummaryLessThan()) {
                joiner.add(Filter.PRICE_LESS_THAN.name());
            } else if (isPriceSummaryGreaterThan()) {
                joiner.add(Filter.PRICE_GREATER_THAN.name());
            }
            return Filter.valueOf(joiner.toString().toUpperCase());
        } catch (final IllegalArgumentException exception) {
            return Filter.ALL;
        }
    }

    private boolean shouldBeAll() {
        return StringUtils.isBlank(name) &&
                Objects.isNull(priceSummary) && (Objects.isNull(priceSummaryLessThan) || !priceSummaryLessThan) && (Objects.isNull(priceSummaryGreaterThan) || !priceSummaryGreaterThan) &&
                Objects.isNull(productsCount) && (Objects.isNull(productsCountLessThan) || !productsCountLessThan) && (Objects.isNull(productsCountGreaterThan) || !productsCountGreaterThan);
    }

    private boolean isPriceSummaryLessThan() {
        final var isLessThan = Objects.nonNull(priceSummaryLessThan) && priceSummaryLessThan;

        return isPriceSummaryOriented() && isLessThan;
    }

    private boolean isPriceSummaryGreaterThan() {
        final var isGreaterThan = Objects.nonNull(priceSummaryGreaterThan) && priceSummaryGreaterThan;

        return isPriceSummaryOriented() && isGreaterThan;
    }

    private boolean isProductsCountLessThan() {
        final var isLessThan = Objects.nonNull(productsCountLessThan) && productsCountLessThan;

        return isProductsCountOriented() && isLessThan;
    }

    private boolean isProductsCountGreaterThan() {
        final var isGreaterThan = Objects.nonNull(productsCountGreaterThan) && productsCountGreaterThan;

        return isProductsCountOriented() && isGreaterThan;
    }

    /**
     * Simple filter enum of available filters for orders.
     */
    public enum Filter {
        ALL,

        NAME, NAME_PRODUCTS_COUNT, NAME_PRICE_LESS_THAN, NAME_PRICE_GREATER_THAN, NAME_PRODUCTS_COUNT_PRICE_LESS_THAN, NAME_PRODUCTS_COUNT_PRICE_GREATER_THAN,

        PRODUCTS_COUNT, PRODUCTS_COUNT_PRICE_LESS_THAN, PRODUCTS_COUNT_PRICE_GREATER_THAN, PRODUCTS_COUNT_LESS_THAN, PRODUCTS_COUNT_GREATER_THAN,
        PRODUCTS_COUNT_LESS_THAN_PRICE_LESS_THAN, PRODUCTS_COUNT_GREATER_THAN_PRICE_LESS_THAN, PRODUCTS_COUNT_LESS_THAN_PRICE_GREATER_THAN, PRODUCTS_COUNT_GREATER_THAN_PRICE_GREATER_THAN,

        PRICE_LESS_THAN, PRICE_GREATER_THAN;
    }

}
