package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.config.MapperImplNaming;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto.ExchangeRateDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto.ExchangeRateDto.RateDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = MapperImplNaming.EXCHANGE_RATE_CACHE_MAPPER)
public abstract class ExchangeRateCacheMapper extends AbstractMapper {

    @Mapping(target = "date", expression = "java(exchangeRateCache.getDate())")
    @Mapping(target = "rate", expression = "java(mapLatestRateCache(exchangeRateCache))")
    abstract ExchangeRateDto mapToDtoCache(final ExchangeRateCache exchangeRateCache);

    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "rates", expression = "java(exchangeRateCaches.stream().map(this::mapToDtoCache).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto mapCache(final CurrencyCode currency, final Collection<ExchangeRateCache> exchangeRateCaches);

    @Mapping(target = "currency", expression = "java(exchangeRateCache.getCurrency())")
    @Mapping(target = "rates", expression = "java(java.util.stream.Stream.of(exchangeRateCache).map(this::mapToDtoCache).collect(java.util.stream.Collectors.toList()))")
    public abstract ExchangeRatesDto mapCache(final ExchangeRateCache exchangeRateCache);

    protected RateDto mapLatestRateCache(final ExchangeRateCache rate) {
        return RateDto.builder()
                .from(ExchangeRateDto.CurrencyValueDto.builder().currency(rate.getFrom().getCurrency()).value(rate.getFrom().getRate()).build())
                .to(ExchangeRateDto.CurrencyValueDto.builder().currency(rate.getTo().getCurrency()).value(rate.getTo().getRate()).build())
                .build();
    }

    public List<ExchangeRatesDto> groupAndSortResultCache(final Stream<ExchangeRateCache> result) {
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

    private Collection<ExchangeRateCache> sortByDateByTheNewestCache(final Collection<ExchangeRateCache> exchangeRateCaches) {
        return exchangeRateCaches.stream().sorted(Comparator.comparing(ExchangeRateCache::getDate).reversed()).collect(Collectors.toList());
    }

}

