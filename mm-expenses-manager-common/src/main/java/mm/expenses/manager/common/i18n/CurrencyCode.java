package mm.expenses.manager.common.i18n;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;

/**
 * Three digits code, iso currency code and countries of usage according to ISO 4217
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public enum CurrencyCode {
    UNDEFINED("Undefined", -1, Set.of()),
    EUR("Euro", 978, Set.of(CountryCode.AD, CountryCode.AT, CountryCode.AX, CountryCode.BE, CountryCode.BL, CountryCode.CY, CountryCode.DE, CountryCode.EE, CountryCode.ES, CountryCode.FI, CountryCode.FR, CountryCode.GF, CountryCode.GP, CountryCode.GR, CountryCode.IE, CountryCode.IT, CountryCode.LT, CountryCode.LU, CountryCode.LV, CountryCode.MC, CountryCode.ME, CountryCode.MF, CountryCode.MQ, CountryCode.MT, CountryCode.NL, CountryCode.PM, CountryCode.PT, CountryCode.RE, CountryCode.SI, CountryCode.SK, CountryCode.SM, CountryCode.TF, CountryCode.VA, CountryCode.YT)),
    JPY("Yen", 392, Set.of(CountryCode.JP)),
    GBP("Pound Sterling", 826, Set.of(CountryCode.GB, CountryCode.GG, CountryCode.IM, CountryCode.JE)),
    AUD("Australian Dollar", 36, Set.of(CountryCode.AU, CountryCode.CC, CountryCode.CX, CountryCode.HM, CountryCode.KI, CountryCode.NF, CountryCode.NR, CountryCode.TV)),
    CAD("Canadian Dollar", 124, Set.of(CountryCode.CA)),
    CHF("Swiss Franc", 756, Set.of(CountryCode.CH, CountryCode.LI)),
    SEK("Swedish Krona", 752, Set.of(CountryCode.SE)),
    NZD("New Zealand Dollar", 554, Set.of(CountryCode.CK, CountryCode.NU, CountryCode.NZ, CountryCode.PN, CountryCode.TK)),
    PLN("Zloty", 985, Set.of(CountryCode.PL));

    private final String name;
    private final int isoCode;
    private final Set<CountryCode> countryCodesList;

    public String getCode() {
        return this.name();
    }

    public static CurrencyCode getCurrencyFromString(final String value, final boolean ignoreCase) {
        try {
            return CurrencyCode.valueOf(ignoreCase ? value : value.toUpperCase());
        } catch (final Exception exception) {
            log.warn("Unknown currency code was passed: {}", value);
            return CurrencyCode.UNDEFINED;
        }
    }

    public static CurrencyCode getCurrencyFromString(final String value) {
        return getCurrencyFromString(value, true);
    }

    public static CurrencyCode of(final CurrencyCode currencyCode) {
        return Objects.nonNull(currencyCode) ? currencyCode : CurrencyCode.UNDEFINED;
    }

}
