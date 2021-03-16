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
    AED("UAE Dirham", 784, Set.of(CountryCode.AE)),
    AFN("Afghani", 971, Set.of(CountryCode.AF)),
    ALL("Lek", 8, Set.of(CountryCode.AL)),
    AMD("Armenian Dram", 51, Set.of(CountryCode.AM)),
    ANG("Netherlands Antillean Guilder", 532, Set.of(CountryCode.CW, CountryCode.SX)),
    AOA("Kwanza", 973, Set.of(CountryCode.AO)),
    ARS("Argentine Peso", 32, Set.of(CountryCode.AR)),
    AUD("Australian Dollar", 36, Set.of(CountryCode.AU, CountryCode.CC, CountryCode.CX, CountryCode.HM, CountryCode.KI, CountryCode.NF, CountryCode.NR, CountryCode.TV)),
    AWG("Aruban Florin", 533, Set.of(CountryCode.AW)),
    AZN("Azerbaijanian Manat", 944, Set.of(CountryCode.AZ)),
    BAM("Convertible Mark", 977, Set.of(CountryCode.BA)),
    BBD("Barbados Dollar", 52, Set.of(CountryCode.BB)),
    BDT("Taka", 50, Set.of(CountryCode.BD)),
    BGN("Bulgarian Lev", 975, Set.of(CountryCode.BG)),
    BHD("Bahraini Dinar", 48, Set.of(CountryCode.BH)),
    BIF("Burundi Franc", 108, Set.of(CountryCode.BI)),
    BMD("Bermudian Dollar", 60, Set.of(CountryCode.BM)),
    BND("Brunei Dollar", 96, Set.of(CountryCode.BN)),
    BOB("Boliviano", 68, Set.of(CountryCode.BO)),
    BOV("Mvdol", 984, Set.of(CountryCode.BO)),
    BRL("Brazilian Real", 986, Set.of(CountryCode.BR)),
    BSD("Bahamian Dollar", 44, Set.of(CountryCode.BS)),
    BTN("Ngultrum", 64, Set.of(CountryCode.BT)),
    BWP("Pula", 72, Set.of(CountryCode.BW)),
    BYN("Belarusian Ruble", 933, Set.of(CountryCode.BY)),
    BZD("Belize Dollar", 84, Set.of(CountryCode.BZ)),
    CAD("Canadian Dollar", 124, Set.of(CountryCode.CA)),
    CDF("Congolese Franc", 976, Set.of(CountryCode.CD)),
    CHE("WIR Euro", 947, Set.of(CountryCode.CH)),
    CHF("Swiss Franc", 756, Set.of(CountryCode.CH, CountryCode.LI)),
    CHW("WIR Franc", 948, Set.of(CountryCode.CH)),
    CLF("Unidad de Fomento", 990, Set.of(CountryCode.CL)),
    CLP("Chilean Peso", 152, Set.of(CountryCode.CL)),
    CNY("Yuan Renminbi", 156, Set.of(CountryCode.CN)),
    COP("Colombian Peso", 170, Set.of(CountryCode.CO)),
    COU("Unidad de Valor Real", 970, Set.of(CountryCode.CO)),
    CRC("Costa Rican Colon", 188, Set.of(CountryCode.CR)),
    CUC("Peso Convertible", 931, Set.of(CountryCode.CU)),
    CUP("Cuban Peso", 192, Set.of(CountryCode.CU)),
    CVE("Cape Verde Escudo", 132, Set.of(CountryCode.CV)),
    CZK("Czech Koruna", 203, Set.of(CountryCode.CZ)),
    DJF("Djibouti Franc", 262, Set.of(CountryCode.DJ)),
    DKK("Danish Krone", 208, Set.of(CountryCode.DK, CountryCode.FO, CountryCode.GL)),
    DOP("Dominican Peso", 214, Set.of(CountryCode.DO)),
    DZD("Algerian Dinar", 12, Set.of(CountryCode.DZ)),
    EGP("Egyptian Pound", 818, Set.of(CountryCode.EG)),
    ERN("Nakfa", 232, Set.of(CountryCode.ER)),
    ETB("Ethiopian Birr", 230, Set.of(CountryCode.ET)),
    EUR("Euro", 978, Set.of(CountryCode.AD, CountryCode.AT, CountryCode.AX, CountryCode.BE, CountryCode.BL, CountryCode.CY, CountryCode.DE, CountryCode.EE, CountryCode.ES, CountryCode.FI, CountryCode.FR, CountryCode.GF, CountryCode.GP, CountryCode.GR, CountryCode.IE, CountryCode.IT, CountryCode.LT, CountryCode.LU, CountryCode.LV, CountryCode.MC, CountryCode.ME, CountryCode.MF, CountryCode.MQ, CountryCode.MT, CountryCode.NL, CountryCode.PM, CountryCode.PT, CountryCode.RE, CountryCode.SI, CountryCode.SK, CountryCode.SM, CountryCode.TF, CountryCode.VA, CountryCode.YT)),
    FJD("Fiji Dollar", 242, Set.of(CountryCode.FJ)),
    FKP("Falkland Islands Pound", 238, Set.of(CountryCode.FK)),
    GBP("Pound Sterling", 826, Set.of(CountryCode.GB, CountryCode.GG, CountryCode.IM, CountryCode.JE)),
    GEL("Lari", 981, Set.of(CountryCode.GE)),
    GHS("Ghana Cedi", 936, Set.of(CountryCode.GH)),
    GIP("Gibraltar Pound", 292, Set.of(CountryCode.GI)),
    GMD("Dalasi", 270, Set.of(CountryCode.GM)),
    GNF("Guinea Franc", 324, Set.of(CountryCode.GN)),
    GTQ("Quetzal", 320, Set.of(CountryCode.GT)),
    GYD("Guyana Dollar", 328, Set.of(CountryCode.GY)),
    HKD("Hong Kong Dollar", 344, Set.of(CountryCode.HK)),
    HNL("Lempira", 340, Set.of(CountryCode.HN)),
    HRK("Croatian Kuna", 191, Set.of(CountryCode.HR)),
    HTG("Gourde", 332, Set.of(CountryCode.HT)),
    HUF("Forint", 348, Set.of(CountryCode.HU)),
    IDR("Rupiah", 360, Set.of(CountryCode.ID)),
    ILS("New Israeli Sheqel", 376, Set.of(CountryCode.IL)),
    INR("Indian Rupee", 356, Set.of(CountryCode.BT, CountryCode.IN)),
    IQD("Iraqi Dinar", 368, Set.of(CountryCode.IQ)),
    IRR("Iranian Rial", 364, Set.of(CountryCode.IR)),
    ISK("Iceland Krona", 352, Set.of(CountryCode.IS)),
    JMD("Jamaican Dollar", 388, Set.of(CountryCode.JM)),
    JOD("Jordanian Dinar", 400, Set.of(CountryCode.JO)),
    JPY("Yen", 392, Set.of(CountryCode.JP)),
    KES("Kenyan Shilling", 404, Set.of(CountryCode.KE)),
    KGS("Som", 417, Set.of(CountryCode.KG)),
    KHR("Riel", 116, Set.of(CountryCode.KH)),
    KMF("Comoro Franc", 174, Set.of(CountryCode.KM)),
    KPW("North Korean Won", 408, Set.of(CountryCode.KP)),
    KRW("Won", 410, Set.of(CountryCode.KR)),
    KWD("Kuwaiti Dinar", 414, Set.of(CountryCode.KW)),
    KYD("Cayman Islands Dollar", 136, Set.of(CountryCode.KY)),
    KZT("Tenge", 398, Set.of(CountryCode.KZ)),
    LAK("Kip", 418, Set.of(CountryCode.LA)),
    LBP("Lebanese Pound", 422, Set.of(CountryCode.LB)),
    LKR("Sri Lanka Rupee", 144, Set.of(CountryCode.LK)),
    LRD("Liberian Dollar", 430, Set.of(CountryCode.LR)),
    LSL("Loti", 426, Set.of(CountryCode.LS)),
    LYD("Libyan Dinar", 434, Set.of(CountryCode.LY)),
    MAD("Moroccan Dirham", 504, Set.of(CountryCode.EH, CountryCode.MA)),
    MDL("Moldovan Leu", 498, Set.of(CountryCode.MD)),
    MGA("Malagasy Ariary", 969, Set.of(CountryCode.MG)),
    MKD("Denar", 807, Set.of(CountryCode.MK)),
    MMK("Kyat", 104, Set.of(CountryCode.MM)),
    MNT("Tugrik", 496, Set.of(CountryCode.MN)),
    MOP("Pataca", 446, Set.of(CountryCode.MO)),
    MRU("Ouguiya", 929, Set.of(CountryCode.MR)),
    MUR("Mauritius Rupee", 480, Set.of(CountryCode.MU)),
    MVR("Rufiyaa", 462, Set.of(CountryCode.MV)),
    MWK("Kwacha", 454, Set.of(CountryCode.MW)),
    MXN("Mexican Peso", 484, Set.of(CountryCode.MX)),
    MXV("Mexican Unidad de Inversion (UDI)", 979, Set.of(CountryCode.MX)),
    MYR("Malaysian Ringgit", 458, Set.of(CountryCode.MY)),
    MZN("Mozambique Metical", 943, Set.of(CountryCode.MZ)),
    NAD("Namibia Dollar", 516, Set.of(CountryCode.NA)),
    NGN("Naira", 566, Set.of(CountryCode.NG)),
    NIO("Cordoba Oro", 558, Set.of(CountryCode.NI)),
    NOK("Norwegian Krone", 578, Set.of(CountryCode.BV, CountryCode.NO, CountryCode.SJ)),
    NPR("Nepalese Rupee", 524, Set.of(CountryCode.NP)),
    NZD("New Zealand Dollar", 554, Set.of(CountryCode.CK, CountryCode.NU, CountryCode.NZ, CountryCode.PN, CountryCode.TK)),
    OMR("Rial Omani", 512, Set.of(CountryCode.OM)),
    PAB("Balboa", 590, Set.of(CountryCode.PA)),
    PEN("Nuevo Sol", 604, Set.of(CountryCode.PE)),
    PGK("Kina", 598, Set.of(CountryCode.PG)),
    PHP("Philippine Peso", 608, Set.of(CountryCode.PH)),
    PKR("Pakistan Rupee", 586, Set.of(CountryCode.PK)),
    PLN("Zloty", 985, Set.of(CountryCode.PL)),
    PYG("Guarani", 600, Set.of(CountryCode.PY)),
    QAR("Qatari Rial", 634, Set.of(CountryCode.QA)),
    RON("New Romanian Leu", 946, Set.of(CountryCode.RO)),
    RSD("Serbian Dinar", 941, Set.of(CountryCode.RS)),
    RUB("Russian Ruble", 643, Set.of(CountryCode.RU)),
    RWF("Rwanda Franc", 646, Set.of(CountryCode.RW)),
    SAR("Saudi Riyal", 682, Set.of(CountryCode.SA)),
    SBD("Solomon Islands Dollar", 90, Set.of(CountryCode.SB)),
    SCR("Seychelles Rupee", 690, Set.of(CountryCode.SC)),
    SDG("Sudanese Pound", 938, Set.of(CountryCode.SD)),
    SEK("Swedish Krona", 752, Set.of(CountryCode.SE)),
    SGD("Singapore Dollar", 702, Set.of(CountryCode.SG)),
    SHP("Saint Helena Pound", 654, Set.of(CountryCode.SH)),
    SLL("Leone", 694, Set.of(CountryCode.SL)),
    SOS("Somali Shilling", 706, Set.of(CountryCode.SO)),
    SRD("Surinam Dollar", 968, Set.of(CountryCode.SR)),
    SSP("South Sudanese Pound", 728, Set.of(CountryCode.SS)),
    STN("Dobra", 930, Set.of(CountryCode.ST)),
    SVC("El Salvador Colon", 222, Set.of(CountryCode.SV)),
    SYP("Syrian Pound", 760, Set.of(CountryCode.SY)),
    SZL("Lilangeni", 748, Set.of(CountryCode.SZ)),
    THB("Baht", 764, Set.of(CountryCode.TH)),
    TJS("Somoni", 972, Set.of(CountryCode.TJ)),
    TMT("Turkmenistan New Manat", 934, Set.of(CountryCode.TM)),
    TND("Tunisian Dinar", 788, Set.of(CountryCode.TN)),
    TOP("Paʻanga", 776, Set.of(CountryCode.TO)),
    TRY("Turkish Lira", 949, Set.of(CountryCode.TR)),
    TTD("Trinidad and Tobago Dollar", 780, Set.of(CountryCode.TT)),
    TWD("New Taiwan Dollar", 901, Set.of(CountryCode.TW)),
    TZS("Tanzanian Shilling", 834, Set.of(CountryCode.TZ)),
    UAH("Hryvnia", 980, Set.of(CountryCode.UA)),
    UGX("Uganda Shilling", 800, Set.of(CountryCode.UG)),
    USD("US Dollar", 840, Set.of(CountryCode.AS, CountryCode.BQ, CountryCode.EC, CountryCode.FM, CountryCode.GU, CountryCode.HT, CountryCode.IO, CountryCode.MH, CountryCode.MP, CountryCode.PA, CountryCode.PR, CountryCode.PW, CountryCode.SV, CountryCode.TC, CountryCode.TL, CountryCode.UM, CountryCode.US, CountryCode.VG, CountryCode.VI)),
    USN("US Dollar (Next day)", 997, Set.of(CountryCode.US)),
    USS("US Dollar (Same day)", 998, Set.of(CountryCode.US)),
    UYI("Uruguay Peso en Unidades Indexadas (URUIURUI)", 940, Set.of(CountryCode.UY)),
    UYU("Peso Uruguayo", 858, Set.of(CountryCode.UY)),
    UZS("Uzbekistan Sum", 860, Set.of(CountryCode.UZ)),
    VEF("Bolivar", 937, Set.of(CountryCode.VE)),
    VES("Bolivar Soberano", 928, Set.of(CountryCode.VE)),
    VND("Dong", 704, Set.of(CountryCode.VN)),
    VUV("Vatu", 548, Set.of(CountryCode.VU)),
    WST("Tala", 882, Set.of(CountryCode.WS)),
    XAF("CFA Franc BEAC", 950, Set.of(CountryCode.CF, CountryCode.CG, CountryCode.CM, CountryCode.GA, CountryCode.GQ, CountryCode.TD)),
    XAG("Silver", 961, Set.of()),
    XAU("Gold", 959, Set.of()),
    XBA("Bond Markets Unit European Composite Unit (EURCO)", 955, Set.of()),
    XBB("Bond Markets Unit European Monetary Unit (E.M.U.-6)", 956, Set.of()),
    XBC("Bond Markets Unit European Unit of Account 9 (E.U.A.-9)", 957, Set.of()),
    XBD("Bond Markets Unit European Unit of Account 17 (E.U.A.-17)", 958, Set.of()),
    XCD("East Caribbean Dollar", 951, Set.of(CountryCode.AG, CountryCode.AI, CountryCode.DM, CountryCode.GD, CountryCode.KN, CountryCode.LC, CountryCode.MS, CountryCode.VC)),
    XDR("SDR (Special Drawing Right)", 960, Set.of()),
    XOF("CFA Franc BCEAO", 952, Set.of(CountryCode.BF, CountryCode.BJ, CountryCode.CI, CountryCode.GW, CountryCode.ML, CountryCode.NE, CountryCode.SN, CountryCode.TG)),
    XPD("Palladium", 964, Set.of()),
    XPF("CFP Franc", 953, Set.of(CountryCode.NC, CountryCode.PF, CountryCode.WF)),
    XPT("Platinum", 962, Set.of()),
    XSU("Sucre", 994, Set.of()),
    XTS("Codes specifically reserved for testing purposes", 963, Set.of()),
    XUA("ADB Unit of Account", 965, Set.of()),
    XXX("The codes assigned for transactions where no currency is involved", 999, Set.of()),
    YER("Yemeni Rial", 886, Set.of(CountryCode.YE)),
    ZAR("Rand", 710, Set.of(CountryCode.LS, CountryCode.NA, CountryCode.ZA)),
    ZMW("Zambian Kwacha", 967, Set.of(CountryCode.ZM)),
    ZWL("Zimbabwe Dollar", 932, Set.of(CountryCode.ZW));

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
