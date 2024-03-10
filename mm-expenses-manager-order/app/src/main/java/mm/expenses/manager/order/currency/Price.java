package mm.expenses.manager.order.currency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.order.async.message.PriceMessage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Objects;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price {

    public static final MathContext DECIMAL_DIGITS = MathContext.DECIMAL32;
    public static final RoundingMode ROUND_PRICE_VALUE_MODE = RoundingMode.HALF_EVEN;
    public static final int ROUND_PRICE_VALUE_DIGITS = 2;

    @JsonProperty("currency")
    private CurrencyCode currency;

    @JsonProperty("amount")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return Objects.nonNull(amount) ? withScale(amount) : withScale(BigDecimal.ZERO);
    }

    public CurrencyCode getCurrency() {
        return Objects.nonNull(currency) ? currency : CurrencyCode.UNDEFINED;
    }

    public Price add(final Price price) {
        if (Objects.nonNull(price)) {
            return new Price(price.getCurrency(), amount.add(price.amount));
        }
        return this;
    }

    @JsonIgnore
    public boolean isPriceFormatValid() {
        return Math.max(amount.stripTrailingZeros().scale(), 0) <= ROUND_PRICE_VALUE_DIGITS;
    }

    @JsonIgnore
    public static Price empty() {
        return new Price(CurrencyCode.UNDEFINED, BigDecimal.ZERO);
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

    public static Price of(final PriceMessage price) {
        return new Price(price.getCurrency(), price.getValue());
    }

    private BigDecimal withScale(final BigDecimal value) {
        return value.setScale(ROUND_PRICE_VALUE_DIGITS, ROUND_PRICE_VALUE_MODE);
    }

}
