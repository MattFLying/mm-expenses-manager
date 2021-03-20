package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.exchangerate.ExchangeRate.Rate;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto.ExchangeRateDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto.ExchangeRateDto.RateDto;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.DefaultCurrencyProvider;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
abstract class ExchangeRateMapper extends AbstractMapper {

    @Autowired
    protected DefaultCurrencyProvider<?> provider;

    @Mapping(target = "date", expression = "java(fromInstantToLocalDate(entity.getDate()))")
    @Mapping(target = "rate", expression = "java(map(entity.getRateByProvider(provider.getName())))")
    abstract ExchangeRateDto mapFromEntity(final ExchangeRate entity);

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(domain.getDate()))")
    @Mapping(target = "ratesByProvider", expression = "java(map(domain, provider.getName()))")
    @Mapping(target = "detailsByProvider", expression = "java(map(provider.getName(), domain))")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "modifiedAt", source = "now")
    abstract ExchangeRate map(final CurrencyRate domain, final Instant now);

    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "rates", expression = "java(entities.stream().map(this::mapFromEntity).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto map(final CurrencyCode currency, final Collection<ExchangeRate> entities);

    @Mapping(target = "currency", expression = "java(entity.getCurrency())")
    @Mapping(target = "rates", expression = "java(java.util.stream.Stream.of(entity).map(this::mapFromEntity).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto map(final ExchangeRate entity);

    abstract RateDto map(final Rate rate);

    protected Map<String, Map<String, Object>> map(final String providerName, final CurrencyRate domain) {
        return Map.of(providerName, domain.getDetails());
    }

    protected Map<String, Rate> map(final CurrencyRate domain, final String providerName) {
        return Map.of(providerName, map(domain.getCurrency(), provider.getDefaultCurrency(), domain.getRate()));
    }

    protected Rate map(final CurrencyCode currencyFrom, final CurrencyCode currencyTo, final Double currencyValueTo) {
        return Rate.of(currencyFrom, currencyTo, currencyValueTo);
    }

    protected List<ExchangeRatesDto> groupAndSortResult(final Stream<ExchangeRate> result) {
        return result
                .collect(Collectors.groupingBy(
                        ExchangeRate::getCurrency,
                        () -> new TreeMap<>(Comparator.comparing(CurrencyCode::getCode)),
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> map(entry.getKey(), sortByDateByTheNewest(entry.getValue())))
                .collect(Collectors.toList());
    }

    private Collection<ExchangeRate> sortByDateByTheNewest(final Collection<ExchangeRate> entities) {
        return entities.stream().sorted(Comparator.comparing(ExchangeRate::getDate).reversed()).collect(Collectors.toList());
    }

}

