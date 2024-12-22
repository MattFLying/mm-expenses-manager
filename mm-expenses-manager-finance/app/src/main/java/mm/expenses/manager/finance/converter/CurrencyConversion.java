package mm.expenses.manager.finance.converter;

import lombok.Builder;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder(toBuilder = true)
public record CurrencyConversion(String id, LocalDate date, CurrencyRate from, CurrencyRate to) {

    public static CurrencyConversion of(final String id, final LocalDate date, final CurrencyRate from, final CurrencyRate to) {
        return CurrencyConversion.builder()
                .id(id)
                .date(date)
                .from(from)
                .to(to)
                .build();
    }

    @Builder(toBuilder = true)
    public record CurrencyRate(CurrencyCode code, BigDecimal value, LocalDate date) {

        @Override
        public BigDecimal value() {
            return BigDecimalWrapper.of(value);
        }

        public static CurrencyRate of(final CurrencyCode code, final BigDecimal value) {
            return CurrencyRate.builder().code(code).value(BigDecimalWrapper.of(value)).build();
        }

        public static CurrencyRate of(final LocalDate date, final CurrencyCode code, final BigDecimal value) {
            return CurrencyRate.builder().code(code).value(BigDecimalWrapper.of(value)).date(date).build();
        }

    }

}
