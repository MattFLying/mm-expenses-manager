package mm.expenses.manager.finance.converter.strategy;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.cache.exchangerate.latest.LatestRatesCacheService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
class ConvertFromDefault extends BaseConversion {

    ConvertFromDefault(final ExchangeRateService exchangeRateService, final ExchangeRateCacheService exchangeRateCacheService, final LatestRatesCacheService latestRatesCacheService, final CurrencyRatesConfig config) {
        super(exchangeRateService, exchangeRateCacheService, latestRatesCacheService, config);
    }

    @Override
    protected BigDecimal calculate(final BigDecimal from, final BigDecimal to, final BigDecimal value) {
        return from.multiply(value, DECIMAL_DIGITS).divide(to, DECIMAL_DIGITS);
    }

    @Override
    public ConversionStrategyType getStrategy() {
        return ConversionStrategyType.FROM_DEFAULT;
    }

    @Override
    public CurrencyRate convert(final CurrencyCode from, final CurrencyCode to, final BigDecimal value) {
        final var ratePair = getRateLatest(to);
        final var rate = ratePair.getRight();

        final var fromValue = valueOf(rate.getFrom());
        final var toValue = valueOf(rate.getTo());

        return CurrencyRate.of(ratePair.getKey(), to, calculate(fromValue, toValue, value));
    }

    @Override
    public CurrencyRate convert(final CurrencyCode from, final CurrencyCode to, final BigDecimal value, final LocalDate date) {
        final var ratePair = getRateForDate(to, date);
        final var rate = ratePair.getRight();

        final var fromValue = valueOf(rate.getFrom());
        final var toValue = valueOf(rate.getTo());

        return CurrencyRate.of(date, to, calculate(fromValue, toValue, value));
    }

}
