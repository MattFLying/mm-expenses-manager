package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.util.Set;

@Slf4j
@Getter
@RequiredArgsConstructor
enum TableType {
    UNKNOWN(Set.of()),
    A(Set.of(
            CurrencyCode.AUD, CurrencyCode.CAD, CurrencyCode.NZD, CurrencyCode.EUR, CurrencyCode.CHF,
            CurrencyCode.GBP, CurrencyCode.JPY, CurrencyCode.SEK
    ));

    private final Set<CurrencyCode> currencies;

    static TableType parse(final Object value) {
        try {
            if (value instanceof String) {
                return valueOf(((String) value).toUpperCase());
            }
            return parse(value.toString());
        } catch (final Exception exception) {
            log.error("Cannot parse nbp table type: {}", value, exception);
            return UNKNOWN;
        }
    }

    static TableType findTableForCurrency(final CurrencyCode currencyCode) {
        try {
            if (TableType.A.getCurrencies().contains(currencyCode)) {
                return TableType.A;
            }
            log.error("Cannot find table type for currency: {}", currencyCode);
            return TableType.UNKNOWN;
        } catch (final Exception exception) {
            log.error("Cannot find table type for currency: {}", currencyCode, exception);
            return UNKNOWN;
        }
    }

}
