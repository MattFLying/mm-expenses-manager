package mm.expenses.manager.finance.converter.strategy;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRate.CurrencyValue;
import mm.expenses.manager.finance.exchangerate.ExchangeRate.Rate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.latest.LatestRatesCache;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Slice;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static mm.expenses.manager.common.util.DateUtils.instantToLocalDateUTC;

/**
 * Abstraction with common logic for all conversions
 */
@RequiredArgsConstructor
abstract class BaseConversion implements ConversionStrategy {

    /**
     * A MathContext object with a precision setting matching the IEEE 754R Decimal32 format, 7 digits, and a rounding mode of HALF_EVEN, the IEEE 754R default.
     */
    protected static final MathContext DECIMAL_DIGITS = MathContext.DECIMAL32;

    protected final ExchangeRateService exchangeRateService;
    protected final LatestRatesCache latestRatesCache;
    protected final CurrencyRatesConfig config;

    /**
     * Defines the way how currency should be converted in specific conversion type
     *
     * @param from  currency value from which will be made conversion
     * @param to    currency value to which will be made conversion
     * @param value value to be converted
     * @return converted value
     */
    protected abstract BigDecimal calculate(final BigDecimal from, final BigDecimal to, final BigDecimal value);

    protected Pair<LocalDate, Rate> getRateLatest(final CurrencyCode code) {
        return latestRatesCache.getLatest(code).map(rate -> Pair.of(
                instantToLocalDateUTC(rate.getDate()), getRate(rate)
        )).orElse(Pair.of(LocalDate.now(), Rate.empty()));
    }

    protected Map<CurrencyCode, Pair<LocalDate, Rate>> getRatesLatest(final Set<CurrencyCode> codes) {
        return latestRatesCache.getLatest(codes).entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        rateEntry -> Pair.of(instantToLocalDateUTC(rateEntry.getValue().getDate()), getRate(rateEntry.getValue()))
                ));
    }

    protected Pair<LocalDate, Rate> getRateForDate(final CurrencyCode code, final LocalDate date) {
        return exchangeRateService.findForCurrencyAndSpecificDate(code, date).map(rate -> Pair.of(
                instantToLocalDateUTC(rate.getDate()), getRate(rate)
        )).orElse(Pair.of(date, Rate.empty()));
    }

    protected Map<CurrencyCode, Pair<LocalDate, Rate>> getRatesForDate(final Set<CurrencyCode> codes, final LocalDate date) {
        final var pageSize = config.getAllRequiredCurrenciesCode().size();
        return exchangeRateService.findForCurrencyCodesAndSpecificDate(codes, date, exchangeRateService.pageRequest(0, pageSize))
                .filter(Objects::nonNull)
                .map(Slice::getContent)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        ExchangeRate::getCurrency,
                        rateEntry -> Pair.of(instantToLocalDateUTC(rateEntry.getDate()), getRate(rateEntry))
                ));
    }

    protected BigDecimal valueOf(final double value) {
        return BigDecimal.valueOf(value);
    }

    protected BigDecimal valueOf(final CurrencyValue value) {
        return valueOf(value.getValue());
    }

    private Rate getRate(final ExchangeRate rate) {
        return rate.getRateByProvider(config.getDefaultProvider(), true);
    }

}
