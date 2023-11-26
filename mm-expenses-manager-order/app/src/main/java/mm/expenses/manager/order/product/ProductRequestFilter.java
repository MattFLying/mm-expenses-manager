package mm.expenses.manager.order.product;

import mm.expenses.manager.common.web.exception.ApiBadRequestException;
import mm.expenses.manager.order.product.exception.ProductExceptionMessage;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;

record ProductRequestFilter(String name,
                            BigDecimal price,
                            BigDecimal priceMin,
                            BigDecimal priceMax,
                            Boolean lessThan,
                            Boolean greaterThan) {

    static final String NAME_PROPERTY = "name";
    static final String PRICE_PROPERTY = "price";
    static final String PRICE_MIN_PROPERTY = "priceMin";
    static final String PRICE_MAX_PROPERTY = "priceMax";
    static final String PRICE_LESS_THAN_PROPERTY = "lessThan";
    static final String PRICE_GREATER_THAN_PROPERTY = "greaterThan";

    boolean isPriceNotRestrictedToBeLessOrGreater() {
        return Objects.nonNull(price) && !lessThan && !greaterThan;
    }

    ProductFilter filter() {
        if (isOnlyName()) {
            return ProductFilter.BY_NAME;
        }
        if (isOnlyPriceRange()) {
            return ProductFilter.BY_PRICE_RANGE;
        }
        if (shouldBeAll()) {
            return ProductFilter.ALL;
        }
        if (isOnlyPriceLess()) {
            if (isPriceNotRestrictedToBeLessOrGreater()) {
                throw new ApiBadRequestException(ProductExceptionMessage.PRODUCT_PRICE_LESS_AND_GREATER);
            }
            return ProductFilter.BY_PRICE_LESS_THAN;
        }
        if (isOnlyPriceGreater()) {
            if (isPriceNotRestrictedToBeLessOrGreater()) {
                throw new ApiBadRequestException(ProductExceptionMessage.PRODUCT_PRICE_LESS_AND_GREATER);
            }
            return ProductFilter.BY_PRICE_GREATER_THAN;
        }
        return ProductFilter.NONE;
    }

    private boolean shouldBeAll() {
        return StringUtils.isBlank(name) && Objects.isNull(priceMin) && Objects.isNull(priceMax) && Objects.isNull(price) && !lessThan && !greaterThan;
    }

    private boolean isOnlyPriceGreater() {
        return StringUtils.isBlank(name) && Objects.isNull(priceMin) && Objects.isNull(priceMax) && Objects.nonNull(price) && !lessThan && greaterThan;
    }

    private boolean isOnlyPriceLess() {
        return StringUtils.isBlank(name) && Objects.isNull(priceMin) && Objects.isNull(priceMax) && Objects.nonNull(price) && lessThan && !greaterThan;
    }

    private boolean isOnlyPriceRange() {
        return StringUtils.isBlank(name) && Objects.nonNull(priceMin) && Objects.nonNull(priceMax) && Objects.isNull(price) && !lessThan && !greaterThan;
    }

    private boolean isOnlyName() {
        return StringUtils.isNotBlank(name) && Objects.isNull(priceMin) && Objects.isNull(priceMax) && Objects.isNull(price) && !lessThan && !greaterThan;
    }

    enum ProductFilter {
        NONE, ALL, BY_NAME, BY_PRICE_RANGE, BY_PRICE_LESS_THAN, BY_PRICE_GREATER_THAN
    }

}
