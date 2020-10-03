package mm.expenses.manager.finance.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.finance.financial.CurrencyRateProvider;
import mm.expenses.manager.finance.nbp.model.NbpCurrencyRate.NbpDetails;

import java.util.stream.Stream;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum CurrencyProviderType {
    UNKNOWN("unknown-provider", CurrencyRateProvider.CurrencyDetails.class),
    NBP("nbp-provider", NbpDetails.class);

    private final String providerName;
    private final Class<? extends CurrencyRateProvider.CurrencyDetails> classType;

    public static CurrencyProviderType findByName(final String name) {
        try {
            return Stream.of(CurrencyProviderType.values())
                    .filter(provider -> provider.getProviderName().equals(name))
                    .findAny()
                    .orElseGet(() -> {
                        log.error("Cannot find provider type from name: {}", name);
                        return UNKNOWN;
                    });
        } catch (final Exception exception) {
            log.error("Cannot parse provider type from name: {}", name, exception);
            return UNKNOWN;
        }
    }

}
