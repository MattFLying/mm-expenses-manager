package mm.expenses.manager.order.currency;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price implements Serializable {

    @JsonProperty("currency")
    private CurrencyCode currency;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("date")
    private Instant date;

    public Price(CurrencyCode currency, BigDecimal amount) {
        this(currency, amount, null);
    }

    public BigDecimal getAmount() {
        return BigDecimalWrapper.of(amount);
    }

    public CurrencyCode getCurrency() {
        return Objects.nonNull(currency) ? currency : CurrencyCode.UNDEFINED;
    }

    @JsonIgnore
    public boolean isPriceFormatValid() {
        return Math.max(getAmount().stripTrailingZeros().scale(), 0) <= BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS;
    }

    @JsonIgnore
    public static Price empty() {
        return new Price(CurrencyCode.UNDEFINED, BigDecimalWrapper.zero());
    }

    public static Price multiply(final CurrencyCode currency, final BigDecimal value, final Double quantity, final Instant date) {
        if (Objects.nonNull(value) && Objects.nonNull(quantity)) {
            return new Price(currency, BigDecimalWrapper.of(BigDecimalWrapper.of(value).multiply(BigDecimalWrapper.of(quantity))), date);
        }
        return Price.empty();
    }

    public static Price multiply(final Price price, final Double quantity) {
        if (Objects.nonNull(price) && Objects.nonNull(quantity)) {
            return multiply(price.getCurrency(), BigDecimalWrapper.of(price.getAmount()), quantity, price.getDate());
        }
        return Price.empty();
    }

    public static Price add(final Price first, final Price second) {
        if (Objects.nonNull(first) && Objects.nonNull(second)) {
            val latestDate = Stream.of(first.getDate(), second.getDate())
                    .filter(Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(DateUtils.nowAsInstant());
            return new Price(first.getCurrency(), BigDecimalWrapper.of(BigDecimalWrapper.of(first.getAmount()).add(BigDecimalWrapper.of(second.getAmount()))), latestDate);
        }
        return Price.empty();
    }

}
