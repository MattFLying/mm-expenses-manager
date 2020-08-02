package mm.expenses.manager.order.currency;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Data
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class Price {

    private static final int PRICE_DECIMAL_DIGIT = 2;

    private final Currency currency;
    private final BigDecimal amount;

    public BigDecimal getAmount() {
        return Objects.nonNull(amount) ? amount.setScale(PRICE_DECIMAL_DIGIT, RoundingMode.CEILING) : BigDecimal.ZERO.setScale(PRICE_DECIMAL_DIGIT, RoundingMode.CEILING);
    }

    public Price add(final Price price) {
        if (Objects.nonNull(price)) {
            return new Price(price.getCurrency(), amount.add(price.amount));
        }
        return this;
    }

    public boolean isPriceFormatValid() {
        return Math.max(amount.stripTrailingZeros().scale(), 0) <= PRICE_DECIMAL_DIGIT;
    }

    public static Price empty() {
        return new Price(Currency.NONE, BigDecimal.ZERO);
    }

    public static Price multiply(final Price price, final Double quantity) {
        if (Objects.nonNull(price) && Objects.nonNull(quantity)) {
            return new Price(price.getCurrency(), price.amount.multiply(BigDecimal.valueOf(quantity)));
        }
        return Price.empty();
    }

    public static Price add(final Price first, final Price second) {
        return new Price(first.getCurrency(), first.getAmount().add(second.getAmount()));
    }

}
