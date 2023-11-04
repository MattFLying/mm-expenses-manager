package mm.expenses.manager.finance.converter.strategy;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache.RateCache;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheService;
import mm.expenses.manager.finance.cache.exchangerate.latest.LatestRatesCacheService;
import mm.expenses.manager.finance.currency.CurrenciesService;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Slice;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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
    protected final ExchangeRateCacheService exchangeRateCacheService;
    protected final LatestRatesCacheService latestRatesCacheService;

    private final CurrenciesService currenciesService;
    private final CurrencyProviders currencyProviders;

    /**
     * Defines the way how currency should be converted in specific conversion type
     *
     * @param from  currency value from which will be made conversion
     * @param to    currency value to which will be made conversion
     * @param value value to be converted
     * @return converted value
     */
    protected abstract BigDecimal calculate(final BigDecimal from, final BigDecimal to, final BigDecimal value);

    protected Pair<LocalDate, ExchangeRateCache> getRateLatest(final CurrencyCode code) {
        return latestRatesCacheService.getLatest(code).map(rate -> Pair.of(rate.getDate(), rate)).orElse(Pair.of(LocalDate.now(), ExchangeRateCache.empty(code)));
    }

    protected Map<CurrencyCode, Pair<LocalDate, ExchangeRateCache>> getRatesLatest(final Set<CurrencyCode> codes) {
        return latestRatesCacheService.getLatest(codes).entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        rateEntry -> Pair.of(rateEntry.getValue().getDate(), rateEntry.getValue())
                ));
    }

    protected Pair<LocalDate, ExchangeRateCache> getRateForDate(final CurrencyCode code, final LocalDate date) {
        final var fromCache = exchangeRateCacheService.findForCurrencyAndSpecificDate(code, date);
        if (fromCache.isPresent()) {
            return Pair.of(date, fromCache.get());
        }

        final var fresh = exchangeRateService.findForCurrencyAndSpecificDate(code, date);
        fresh.ifPresent(exchangeRate -> CompletableFuture.runAsync(() -> exchangeRateCacheService.saveFresh(List.of(exchangeRate), false)));

        return fresh.map(rate -> Pair.of(instantToLocalDateUTC(rate.getDate()), ExchangeRateCache.of(rate, currencyProviders.getProviderName()))).orElse(Pair.of(date, ExchangeRateCache.empty(code)));
    }

    protected Map<CurrencyCode, Pair<LocalDate, ExchangeRateCache>> getRatesForDate(final Set<CurrencyCode> codes, final LocalDate date) {
        final var fromCache = exchangeRateCacheService.findForCurrencyCodesAndSpecificDate(codes, date);
        if (!fromCache.isEmpty() && fromCache.size() == codes.size()) {
            return fromCache.stream().collect(Collectors.toMap(ExchangeRateCache::getCurrency, rateCache -> Pair.of(rateCache.getDate(), rateCache)));
        }

        final var fresh = exchangeRateService.findForCurrencyCodesAndSpecificDate(codes, date, exchangeRateService.pageRequest(0, currenciesService.getAllAvailableCurrenciesCount()))
                .filter(Objects::nonNull)
                .map(Slice::getContent)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        CompletableFuture.runAsync(() -> exchangeRateCacheService.saveFresh(fresh, false));

        return fresh.stream()
                .collect(Collectors.toMap(
                        ExchangeRate::getCurrency,
                        rateEntry -> Pair.of(instantToLocalDateUTC(rateEntry.getDate()), ExchangeRateCache.of(rateEntry, currencyProviders.getProviderName()))
                ));
    }

    protected BigDecimal valueOf(final double value) {
        return BigDecimal.valueOf(value);
    }

    protected BigDecimal valueOf(final RateCache value) {
        return valueOf(value.getRate());
    }

}
