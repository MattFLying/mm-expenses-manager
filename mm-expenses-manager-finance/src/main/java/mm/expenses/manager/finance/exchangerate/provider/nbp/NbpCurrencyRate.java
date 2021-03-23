package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
class NbpCurrencyRate extends CurrencyRate {

    NbpCurrencyRate(final CurrencyCode currency, final LocalDate date, final Double rate, final TableType tableType, final String tableNumber) {
        super(currency, date, rate, Map.of(
                Details.TABLE_TYPE.getProperty(), tableType,
                Details.TABLE_NUMBER.getProperty(), tableNumber
        ));
    }

    static NbpCurrencyRate of(final CurrencyCode currency, final TableType tableType, final NbpClient.RateDto rateDto) {
        return new NbpCurrencyRate(currency, rateDto.getEffectiveDate(), rateDto.getMid(), tableType, rateDto.getNo());
    }

    static NbpCurrencyRate sameDataDifferentDate(final NbpCurrencyRate rate, final LocalDate date) {
        return new NbpCurrencyRate(rate.getCurrency(), date, rate.getRate(), rate.getTableType(), rate.getTableNumber());
    }

    TableType getTableType() {
        return TableType.parse(getDetails(Details.TABLE_TYPE.getProperty()).orElse(TableType.UNKNOWN));
    }

    String getTableNumber() {
        return String.valueOf(getDetails(Details.TABLE_NUMBER.getProperty()).orElse(StringUtils.EMPTY));
    }

    @Getter
    @RequiredArgsConstructor
    public enum Details {
        TABLE_TYPE("table-type"),
        TABLE_NUMBER("table-number");

        private final String property;
    }

}
