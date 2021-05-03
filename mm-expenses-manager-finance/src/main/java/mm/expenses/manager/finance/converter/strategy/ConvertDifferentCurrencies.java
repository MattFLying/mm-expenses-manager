package mm.expenses.manager.finance.converter.strategy;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.cache.exchangerate.latest.LatestRatesCache;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Component
class ConvertDifferentCurrencies extends BaseConversion {

    ConvertDifferentCurrencies(final ExchangeRateService exchangeRateService, final ExchangeRateCacheService exchangeRateCacheService, final LatestRatesCache latestRatesCache, final CurrencyRatesConfig config) {
        super(exchangeRateService, exchangeRateCacheService, latestRatesCache, config);
    }

    @Override
    protected BigDecimal calculate(final BigDecimal from, final BigDecimal to, final BigDecimal value) {
        return from.multiply(value, DECIMAL_DIGITS).divide(to, DECIMAL_DIGITS);
    }

    @Override
    public ConversionStrategyType getStrategy() {
        return ConversionStrategyType.DIFFERENT;
    }

    @Override
    public CurrencyRate convert(final CurrencyCode from, final CurrencyCode to, final BigDecimal value) {
        final var rates = getRatesLatest(Set.of(from, to));
        final var ratePairFrom = rates.get(from);
        final var ratePairTo = rates.get(to);

        final var fromValue = valueOf(ratePairFrom.getRight().getTo());
        final var toValue = valueOf(ratePairTo.getRight().getTo());

        return CurrencyRate.of(ratePairTo.getKey(), to, calculate(fromValue, toValue, value));
    }

    @Override
    public CurrencyRate convert(final CurrencyCode from, final CurrencyCode to, final BigDecimal value, final LocalDate date) {
        final var rates = getRatesForDate(Set.of(from, to), date);
        final var ratePairFrom = rates.get(from);
        final var ratePairTo = rates.get(to);

        final var fromValue = valueOf(ratePairFrom.getRight().getTo());
        final var toValue = valueOf(ratePairTo.getRight().getTo());

        return CurrencyRate.of(date, to, calculate(fromValue, toValue, value));
    }

}
