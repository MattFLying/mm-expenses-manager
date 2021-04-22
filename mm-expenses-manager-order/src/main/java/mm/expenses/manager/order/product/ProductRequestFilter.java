package mm.expenses.manager.order.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.order.product.exception.ProductExceptionCode;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Objects;

@RequiredArgsConstructor
class ProductRequestFilter {

    private final String name;
    private final BigDecimal price;
    private final BigDecimal priceMin;
    private final BigDecimal priceMax;
    private final boolean lessThan;
    private final boolean greaterThan;

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
                //throw new ApiBadRequestException(ProductExceptionCode.PRODUCT_PRICE_LESS_AND_GREATER.getCode(), "Price must be less or greater");
                throw new ApiBadRequestException(null);
            }
            return ProductFilter.BY_PRICE_LESS_THAN;
        }
        if (isOnlyPriceGreater()) {
            if (isPriceNotRestrictedToBeLessOrGreater()) {
                //throw new ApiBadRequestException(ProductExceptionCode.PRODUCT_PRICE_LESS_AND_GREATER.getCode(), "Price must be less or greater");
                throw new ApiBadRequestException(null);
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
