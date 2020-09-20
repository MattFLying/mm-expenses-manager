package mm.expenses.manager.finance.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.financial.CurrencyRateProvider;
import mm.expenses.manager.finance.nbp.model.NbpCurrencyRate.NbpDetails;

@Getter
@RequiredArgsConstructor
public enum CurrencyProviderType {
    NBP("nbp-provider", NbpDetails.class);

    private final String providerName;
    private final Class<? extends CurrencyRateProvider.CurrencyDetails> classType;
}
