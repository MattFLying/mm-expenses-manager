package mm.expenses.manager.finance.nbp.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.util.Set;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum TableType {
    UNKNOWN(Set.of()),
    A(Set.of(
            CurrencyCode.THB, CurrencyCode.USD, CurrencyCode.AUD, CurrencyCode.HKD, CurrencyCode.CAD, CurrencyCode.NZD,
            CurrencyCode.SGD, CurrencyCode.EUR, CurrencyCode.HUF, CurrencyCode.CHF, CurrencyCode.GBP, CurrencyCode.UAH,
            CurrencyCode.JPY, CurrencyCode.CZK, CurrencyCode.DKK, CurrencyCode.ISK, CurrencyCode.NOK, CurrencyCode.SEK,
            CurrencyCode.HRK, CurrencyCode.RON, CurrencyCode.BGN, CurrencyCode.TRY, CurrencyCode.ILS, CurrencyCode.CLP,
            CurrencyCode.PHP, CurrencyCode.MXN, CurrencyCode.ZAR, CurrencyCode.BRL, CurrencyCode.MYR, CurrencyCode.RUB,
            CurrencyCode.IDR, CurrencyCode.INR, CurrencyCode.KRW, CurrencyCode.CNY, CurrencyCode.XDR
    )),
    B(Set.of(
            CurrencyCode.AFN, CurrencyCode.MGA, CurrencyCode.PAB, CurrencyCode.ETB, CurrencyCode.VES, CurrencyCode.BOB,
            CurrencyCode.CRC, CurrencyCode.SVC, CurrencyCode.NIO, CurrencyCode.GMD, CurrencyCode.MKD, CurrencyCode.DZD,
            CurrencyCode.BHD, CurrencyCode.IQD, CurrencyCode.JOD, CurrencyCode.KWD, CurrencyCode.LYD, CurrencyCode.RSD,
            CurrencyCode.TND, CurrencyCode.MAD, CurrencyCode.AED, CurrencyCode.STN, CurrencyCode.BSD, CurrencyCode.BBD,
            CurrencyCode.BZD, CurrencyCode.BND, CurrencyCode.FJD, CurrencyCode.GYD, CurrencyCode.JMD, CurrencyCode.LRD,
            CurrencyCode.NAD, CurrencyCode.SRD, CurrencyCode.TTD, CurrencyCode.XCD, CurrencyCode.SBD, CurrencyCode.ZWL,
            CurrencyCode.VND, CurrencyCode.AMD, CurrencyCode.CVE, CurrencyCode.AWG, CurrencyCode.BIF, CurrencyCode.XOF,
            CurrencyCode.XAF, CurrencyCode.XPF, CurrencyCode.DJF, CurrencyCode.GNF, CurrencyCode.KMF, CurrencyCode.CDF,
            CurrencyCode.RWF, CurrencyCode.EGP, CurrencyCode.GIP, CurrencyCode.LBP, CurrencyCode.SSP, CurrencyCode.SDG,
            CurrencyCode.SYP, CurrencyCode.GHS, CurrencyCode.HTG, CurrencyCode.PYG, CurrencyCode.ANG, CurrencyCode.PGK,
            CurrencyCode.LAK, CurrencyCode.MWK, CurrencyCode.ZMW, CurrencyCode.AOA, CurrencyCode.MMK, CurrencyCode.GEL,
            CurrencyCode.MDL, CurrencyCode.ALL, CurrencyCode.HNL, CurrencyCode.SLL, CurrencyCode.SZL, CurrencyCode.LSL,
            CurrencyCode.AZN, CurrencyCode.MZN, CurrencyCode.NGN, CurrencyCode.ERN, CurrencyCode.TWD, CurrencyCode.TMT,
            CurrencyCode.MRU, CurrencyCode.TOP, CurrencyCode.MOP, CurrencyCode.ARS, CurrencyCode.DOP, CurrencyCode.COP,
            CurrencyCode.CUP, CurrencyCode.UYU, CurrencyCode.BWP, CurrencyCode.GTQ, CurrencyCode.IRR, CurrencyCode.YER,
            CurrencyCode.QAR, CurrencyCode.OMR, CurrencyCode.SAR, CurrencyCode.KHR, CurrencyCode.BYN, CurrencyCode.LKR,
            CurrencyCode.MVR, CurrencyCode.MUR, CurrencyCode.NPR, CurrencyCode.PKR, CurrencyCode.SCR, CurrencyCode.PEN,
            CurrencyCode.KGS, CurrencyCode.TJS, CurrencyCode.UZS, CurrencyCode.KES, CurrencyCode.SOS, CurrencyCode.TZS,
            CurrencyCode.UGX, CurrencyCode.BDT, CurrencyCode.WST, CurrencyCode.KZT, CurrencyCode.MNT, CurrencyCode.VUV,
            CurrencyCode.BAM
    )),
    C(Set.of());

    private final Set<CurrencyCode> currencies;

    public static TableType parse(final String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (final Exception exception) {
            log.error("Cannot parse nbp table type: {}", value, exception);
            return UNKNOWN;
        }
    }

    public static TableType findTableForCurrency(final CurrencyCode currencyCode) {
        if (TableType.A.getCurrencies().contains(currencyCode)) {
            return TableType.A;
        }
        if (TableType.B.getCurrencies().contains(currencyCode)) {
            return TableType.B;
        }
        log.error("Cannot find table type for currency: {}", currencyCode);
        return TableType.UNKNOWN;
    }

    public static Set<CurrencyCode> findCurrencies(final TableType table) {
        switch (table) {
            case A:
                return TableType.A.getCurrencies();
            case B:
                return TableType.B.getCurrencies();
            default:
                return Set.of();
        }
    }

}
