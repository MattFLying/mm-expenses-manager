package mm.expenses.manager.finance.converter;

import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;
import mm.expenses.manager.finance.converter.strategy.ConversionStrategy;
import mm.expenses.manager.finance.converter.strategy.ConversionStrategyType;
import mm.expenses.manager.finance.currency.CurrenciesService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return CurrencyConversion.of(id, result.date(), CurrencyRate.of(from, value), result);
    }

    CurrencyConversion convertForDate(final CurrencyCode from, final CurrencyCode to, final BigDecimal value, final LocalDate date, final String id) {
        final var result = findConversionStrategy(from, to).convert(from, to, value, date);
        return CurrencyConversion.of(id, result.date(), CurrencyRate.of(from, value), result);
    }

    List<CurrencyConversion> convertMultiple(final List<CurrencyConversionRequest> conversionRequests) {
        val result = new ArrayList<CurrencyConversion>();
        conversionRequests.stream()
                .map(request -> {
                    val currencyCodeFrom = CurrencyCode.getCurrencyFromString(request.getFrom().getCode(), false);
                    val currencyCodeTo = CurrencyCode.getCurrencyFromString(Objects.nonNull(request.getTo()) ? request.getTo().getCode() : currenciesService.getCurrentCurrency().getCode(), false);
                    val converted = findConversionStrategy(currencyCodeFrom, currencyCodeTo).convert(currencyCodeFrom, currencyCodeTo, BigDecimalWrapper.of(request.getFrom().getValue()));

                    return CurrencyConversion.of(request.getId(), converted.date(), CurrencyRate.of(currencyCodeFrom, BigDecimalWrapper.of(request.getFrom().getValue())), converted);
                })
                .forEach(result::add);

        return result.stream()
                .collect(Collectors.toMap(CurrencyConversion::id, Function.identity(), this::mergeDuplicate))
                .values()
                .stream()
                .toList();
    }

    private ConversionStrategy findConversionStrategy(final CurrencyCode from, final CurrencyCode to) {
        final var defaultCurrency = currenciesService.getCurrentCurrency();
        return strategies.get(ConversionStrategyType.findConversionStrategy(defaultCurrency, from, to));
    }

    private CurrencyConversion mergeDuplicate(final CurrencyConversion first, final CurrencyConversion second) {
        return CurrencyConversion.builder()
                .id(first.id())
                .date(first.date())
                .from(first.from().toBuilder().value(first.from().value().add(second.from().value())).build())
                .to(first.to().toBuilder().value(first.to().value().add(second.to().value())).build())
                .build();
    }

}
