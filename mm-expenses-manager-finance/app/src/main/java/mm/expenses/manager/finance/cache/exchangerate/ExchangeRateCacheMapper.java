package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.finance.api.exchangerate.model.CurrencyValueDto;
import mm.expenses.manager.finance.api.exchangerate.model.ExchangeRateDto;
import mm.expenses.manager.finance.api.exchangerate.model.ExchangeRatesDto;
import mm.expenses.manager.finance.api.exchangerate.model.RateDto;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache.RateCache;
import mm.expenses.manager.finance.config.MapperImplNaming;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = MapperImplNaming.EXCHANGE_RATE_CACHE_MAPPER)
public abstract class ExchangeRateCacheMapper extends AbstractMapper {

    @Mapping(target = "date", expression = "java(exchangeRateCache.getDate())")
    @Mapping(target = "rate", expression = "java(map(exchangeRateCache))")
    abstract ExchangeRateDto mapToDtoCache(final ExchangeRateCache exchangeRateCache);

    @Mapping(target = "currency", expression = "java(currency.getCode())")
    @Mapping(target = "rates", expression = "java(exchangeRateCaches.stream().map(this::mapToDtoCache).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto mapCache(final CurrencyCode currency, final Collection<ExchangeRateCache> exchangeRateCaches);

    @Mapping(target = "currency", expression = "java(exchangeRateCache.getCurrency().getCode())")
    @Mapping(target = "rates", expression = "java(java.util.stream.Stream.of(exchangeRateCache).map(this::mapToDtoCache).collect(java.util.stream.Collectors.toList()))")
    public abstract ExchangeRatesDto mapCache(final ExchangeRateCache exchangeRateCache);

    protected CurrencyValueDto map(final RateCache rate) {
        var dto = new CurrencyValueDto();
        dto.setValue(rate.getRate());
        dto.setCurrency(rate.getCurrency().getCode());

        return dto;
    }

    protected RateDto map(final ExchangeRateCache rate) {
        var dto = new RateDto();
        dto.setFrom(map(rate.getFrom()));
        dto.setTo(map(rate.getTo()));

        return dto;
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

