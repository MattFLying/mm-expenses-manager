package mm.expenses.manager.finance.converter.strategy;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;
import mm.expenses.manager.finance.currency.CurrenciesService;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.cache.exchangerate.latest.LatestRatesCacheService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
class ConvertToDefault extends BaseConversion {

    ConvertToDefault(final ExchangeRateService exchangeRateService,
                     final ExchangeRateCacheService exchangeRateCacheService,
                     final LatestRatesCacheService latestRatesCacheService,
                     final CurrenciesService currenciesService,
                     final CurrencyProviders currencyProviders) {
        super(exchangeRateService, exchangeRateCacheService, latestRatesCacheService, currenciesService, currencyProviders);
    }

    @Override
    protected BigDecimal calculate(final BigDecimal from, final BigDecimal to, final BigDecimal value) {
        return to.multiply(value, DECIMAL_DIGITS);
    }

    @Override
    public ConversionStrategyType getStrategy() {
        return ConversionStrategyType.TO_DEFAULT;
    }

    @Override
    public CurrencyRate convert(final CurrencyCode from, final CurrencyCode to, final BigDecimal value) {
        final var ratePair = getRateLatest(from);
        final var rate = ratePair.getRight();
        final var toValue = valueOf(rate.getTo());

        return CurrencyRate.of(ratePair.getLeft(), to, calculate(null, toValue, value));
    }

    @Override
    public CurrencyRate convert(final CurrencyCode from, final CurrencyCode to, final BigDecimal value, final LocalDate date) {
        final var ratePairFrom = getRateForDate(from, date);
        final var toValue = valueOf(ratePairFrom.getRight().getTo());

        return CurrencyRate.of(date, to, calculate(null, toValue, value));
    }

}
