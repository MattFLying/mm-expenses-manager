package mm.expenses.manager.finance.converter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Data
@EqualsAndHashCode
@RequiredArgsConstructor
@Builder(toBuilder = true)
public class CurrencyConversion {

    private final String id;
    private final LocalDate date;

    private final CurrencyRate from;
    private final CurrencyRate to;

    public static CurrencyConversion of(final String id, final LocalDate date, final CurrencyRate from, final CurrencyRate to) {
        return CurrencyConversion.builder()
                .id(id)
                .date(date)
                .from(from)
                .to(to)
                .build();
    }

    @Data
    @EqualsAndHashCode
    @RequiredArgsConstructor
    @Builder(toBuilder = true)
    public static class CurrencyRate {

        public static final RoundingMode ROUND_CURRENCY_VALUE_MODE = RoundingMode.HALF_EVEN;
        public static final int ROUND_CURRENCY_VALUE_DIGITS = 2;

        private final CurrencyCode code;
        private final BigDecimal value;

        private final LocalDate date;

        public BigDecimal getValue() {
            return Objects.nonNull(value)
                    ? value.setScale(ROUND_CURRENCY_VALUE_DIGITS, ROUND_CURRENCY_VALUE_MODE)
                    : BigDecimal.ZERO;
        }

        public static CurrencyRate of(final CurrencyCode code, final BigDecimal value) {
            return CurrencyRate.builder().code(code).value(value).build();
        }

        public static CurrencyRate of(final LocalDate date, final CurrencyCode code, final BigDecimal value) {
            return CurrencyRate.builder().code(code).value(value).date(date).build();
        }

    }

}
