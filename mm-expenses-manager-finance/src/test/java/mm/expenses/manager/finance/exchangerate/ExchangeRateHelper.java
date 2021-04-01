package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.DEFAULT_CURRENCY;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;

public class ExchangeRateHelper {

    public static final Long INITIAL_VERSION = 0L;
    public static final String ID = UUID.randomUUID().toString();

    public static ExchangeRate createNewExchangeRate(final String id, final CurrencyCode currency, final Instant date, final Instant createdAt,
                                                     final Instant modifiedAt, final Map<String, ExchangeRate.Rate> ratesByProvider, final Map<String, Map<String, Object>> detailsByProvider) {
        return ExchangeRate.builder()
                .id(id)
                .currency(currency)
                .date(date)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .ratesByProvider(ratesByProvider)
                .detailsByProvider(detailsByProvider)
                .version(INITIAL_VERSION)
                .build();
    }

    public static ExchangeRate createNewExchangeRate(final String id, final CurrencyCode currency, final Instant date, final Instant createdModifiedAt,
                                                     final Map<String, ExchangeRate.Rate> ratesByProvider, final Map<String, Map<String, Object>> detailsByProvider) {
        return createNewExchangeRate(id, currency, date, createdModifiedAt, createdModifiedAt, ratesByProvider, detailsByProvider);
    }

    public static ExchangeRate.Rate createNewRate(final CurrencyCode currencyFrom, final Double valueFrom, final CurrencyCode currencyTo, final Double valueTo) {
        return ExchangeRate.Rate.builder()
                .from(ExchangeRate.CurrencyValue.of(currencyFrom, valueFrom))
                .to(ExchangeRate.CurrencyValue.of(currencyTo, valueTo))
                .build();
    }

    public static ExchangeRate.Rate createNewRateToPLN(final CurrencyCode currencyFrom, final Double valueTo) {
        return createNewRate(currencyFrom, 1.0, DEFAULT_CURRENCY, valueTo);
    }

    public static ExchangeRate currencyRateToExchangeRate(final CurrencyRate domain, final Instant now) {
        return createNewExchangeRate(
                ID, domain.getCurrency(), Objects.nonNull(domain.getDate()) ? DateUtils.localDateToInstant(domain.getDate()) : null, now,
                new HashMap<>(Map.of(PROVIDER_NAME, ExchangeRate.Rate.of(domain.getCurrency(), DEFAULT_CURRENCY, domain.getRate()))),
                new HashMap<>(Map.of(PROVIDER_NAME, domain.getDetails()))
        );
    }

}
