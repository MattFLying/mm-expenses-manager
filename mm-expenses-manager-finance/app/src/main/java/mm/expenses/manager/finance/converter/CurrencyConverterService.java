package mm.expenses.manager.finance.converter;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;
import mm.expenses.manager.finance.converter.strategy.ConversionStrategy;
import mm.expenses.manager.finance.converter.strategy.ConversionStrategyType;
import mm.expenses.manager.finance.currency.CurrenciesService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class CurrencyConverterService {

    private final CurrenciesService currenciesService;
    private final Map<ConversionStrategyType, ConversionStrategy> strategies;

    CurrencyConverterService(final CurrenciesService currenciesService, final List<ConversionStrategy> strategies) {
        this.currenciesService = currenciesService;
        this.strategies = strategies.stream().collect(Collectors.toMap(ConversionStrategy::getStrategy, Function.identity()));
    }

    CurrencyConversion convertLatest(final CurrencyCode from, final CurrencyCode to, final BigDecimal value, final String id) {
        final var result = findConversionStrategy(from, to).convert(from, to, value);
        return CurrencyConversion.of(id, result.getDate(), CurrencyRate.of(from, value), result);
    }

    CurrencyConversion convertForDate(final CurrencyCode from, final CurrencyCode to, final BigDecimal value, final LocalDate date, final String id) {
        final var result = findConversionStrategy(from, to).convert(from, to, value, date);
        return CurrencyConversion.of(id, result.getDate(), CurrencyRate.of(from, value), result);
    }

    private ConversionStrategy findConversionStrategy(final CurrencyCode from, final CurrencyCode to) {
        final var defaultCurrency = currenciesService.getCurrentCurrency();
        return strategies.get(ConversionStrategyType.findConversionStrategy(defaultCurrency, from, to));
    }

}
