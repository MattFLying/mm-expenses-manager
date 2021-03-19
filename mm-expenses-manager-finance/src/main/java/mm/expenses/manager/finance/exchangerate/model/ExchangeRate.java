package mm.expenses.manager.finance.exchangerate.model;

import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.common.i18n.CurrencyCode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
public class ExchangeRate {

    private final String id;

    private final CurrencyCode currency;

    private final LocalDate date;

    private final Map<String, Double> ratesByProvider;

    private final Map<String, Map<String, Object>> detailsByProvider;

    private final Instant createdAt;

    @Builder(toBuilder = true)
    public ExchangeRate(final String id, final CurrencyCode currency, final LocalDate date, final Map<String, Double> ratesByProvider, final Map<String, Map<String, Object>> detailsByProvider, final Instant createdAt) {
        this.id = id;
        this.currency = currency;
        this.date = date;
        this.ratesByProvider = Objects.nonNull(ratesByProvider) ? ratesByProvider : new HashMap<>();
        this.detailsByProvider = Objects.nonNull(detailsByProvider) ? detailsByProvider : new HashMap<>();
        this.createdAt = createdAt;
    }

    public Double getRateByProvider(final String providerName) {
        return ratesByProvider.getOrDefault(providerName, 0.0);
    }

}
