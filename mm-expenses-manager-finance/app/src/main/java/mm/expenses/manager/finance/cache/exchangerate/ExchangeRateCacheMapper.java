package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.finance.api.exchangerate.model.CurrencyValueDto;
import mm.expenses.manager.finance.api.exchangerate.model.ExchangeRateDto;
import mm.expenses.manager.finance.api.exchangerate.model.ExchangeRatesDto;
import mm.expenses.manager.finance.api.exchangerate.model.RateDto;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache.RateCache;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collectors.class, List.class}
)
public interface ExchangeRateCacheMapper extends AbstractMapper {

    @Mapping(target = "rate", expression = "java(map(exchangeRateCache))")
    ExchangeRateDto mapToDtoCache(final ExchangeRateCache exchangeRateCache);

    @Mapping(target = "currency", expression = "java(currency.getCode())")
    @Mapping(target = "rates", expression = "java(exchangeRateCaches.stream().map(this::mapToDtoCache).collect(Collectors.toList()))")
    ExchangeRatesDto mapCache(final CurrencyCode currency, final Collection<ExchangeRateCache> exchangeRateCaches);

    @Mapping(target = "currency", expression = "java(exchangeRateCache.getCurrency().getCode())")
    @Mapping(target = "rates", expression = "java(List.of(mapToDtoCache(exchangeRateCache)))")
    ExchangeRatesDto mapCache(final ExchangeRateCache exchangeRateCache);

    @Mapping(target = "value", source = "rateCache.rate")
    @Mapping(target = "currency", expression = "java(rateCache.getCurrency().getCode())")
    CurrencyValueDto map(final RateCache rateCache);

    RateDto map(final ExchangeRateCache rateCache);

    default List<ExchangeRatesDto> groupAndSortResultCache(final Stream<ExchangeRateCache> result) {
        return result
                .collect(Collectors.groupingBy(
                        ExchangeRateCache::getCurrency,
                        () -> new TreeMap<>(Comparator.comparing(CurrencyCode::getCode)),
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> mapCache(entry.getKey(), sortByDateByTheNewestCache(entry.getValue())))
                .collect(Collectors.toList());
    }

    default Collection<ExchangeRateCache> sortByDateByTheNewestCache(final Collection<ExchangeRateCache> exchangeRateCaches) {
        return exchangeRateCaches.stream().sorted(Comparator.comparing(ExchangeRateCache::getDate).reversed()).collect(Collectors.toList());
    }

}

