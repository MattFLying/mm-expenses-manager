package mm.expenses.manager.common.utils.price;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Common implementation of price to be reused whereas is needed with expected possible operations.
 */
@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Price implements Serializable {

    @JsonProperty("currency")
    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private CurrencyCode currency;

    @JsonProperty("value")
    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private BigDecimal value;

    @JsonProperty("date")
    private Instant date;

    public Price() {
        this(null, null, null);
    }

    public Price(final CurrencyCode currency, final BigDecimal value) {
        this(currency, value, null);
    }

    public Price(final Double value, final CurrencyCode currency) {
        this(currency, BigDecimal.valueOf(value));
    }

    public BigDecimal getValue() {
        return BigDecimalWrapper.of(value);
    }

    public CurrencyCode getCurrency() {
        return Objects.nonNull(currency) ? currency : CurrencyCode.UNDEFINED;
    }

    @JsonIgnore
    public boolean isPriceFormatValid() {
        return Math.max(getValue().stripTrailingZeros().scale(), 0) <= BigDecimalWrapper.ROUND_CURRENCY_VALUE_DIGITS;
    }

    @JsonIgnore
    public static Price empty() {
        return new Price(CurrencyCode.UNDEFINED, BigDecimalWrapper.zero());
    }

    @JsonIgnore
    public static Price empty(final CurrencyCode currency) {
        return new Price(currency, BigDecimalWrapper.zero());
    }

    @JsonIgnore
    public static Price multiply(final CurrencyCode currency, final BigDecimal value, final Double quantity, final Instant date) {
        if (Objects.nonNull(value) && Objects.nonNull(quantity)) {
            return new Price(currency, BigDecimalWrapper.of(BigDecimalWrapper.of(value).multiply(BigDecimalWrapper.of(quantity))), date);
        }
        return Price.empty();
    }

    @JsonIgnore
    public static Price multiply(final Price price, final Double quantity) {
        if (Objects.nonNull(price) && Objects.nonNull(quantity)) {
            return multiply(price.getCurrency(), BigDecimalWrapper.of(price.getValue()), quantity, price.getDate());
        }
        return Price.empty();
    }

    @JsonIgnore
    public static Price add(final Price first, final Price second) {
        if (Objects.nonNull(first) && Objects.nonNull(second)) {
            val latestDate = Stream.of(first.getDate(), second.getDate())
                    .filter(Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(DateUtils.nowAsInstant());
            return new Price(first.getCurrency(), BigDecimalWrapper.of(BigDecimalWrapper.of(first.getValue()).add(BigDecimalWrapper.of(second.getValue()))), latestDate);
        }
        return Price.empty();
    }

    @JsonIgnore
    public boolean hasCurrency(final CurrencyCode currency) {
        return Objects.equals(getCurrency(), currency);
    }

    @JsonIgnore
    public boolean hasCurrency(final String currencyCode) {
        return hasCurrency(CurrencyCode.getCurrencyFromString(currencyCode));
    }

}
