package mm.expenses.manager.finance.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mapper implementations names can be used in pom to exclusions
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapperImplNaming {

    public static final String EXCHANGE_RATE_MAPPER = "ExchangeRateMapperImpl";
    public static final String CURRENCY_CONVERTER_MAPPER = "CurrencyConverterMapperImpl";

}
