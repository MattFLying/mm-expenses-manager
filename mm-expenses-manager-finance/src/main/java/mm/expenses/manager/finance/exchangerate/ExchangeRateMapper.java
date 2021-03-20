package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRates;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRates.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.model.dto.ExchangeRatesDto;
import mm.expenses.manager.finance.exchangerate.model.dto.ExchangeRatesDto.ExchangeRateDto;
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
    abstract ExchangeRate mapFromEntity(final ExchangeRateEntity entity);

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(domain.getDate()))")
    @Mapping(target = "ratesByProvider", expression = "java(map(domain, provider.getName()))")
    @Mapping(target = "detailsByProvider", expression = "java(map(provider.getName(), domain))")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "modifiedAt", source = "now")
    abstract ExchangeRateEntity map(final CurrencyRate domain, final Instant now);

    @Mapping(target = "date", source = "exchangeRate.date")
    @Mapping(target = "rate", expression = "java(map(exchangeRate.getRateByProvider(provider.getName())))")
    abstract ExchangeRateDto map(final ExchangeRate exchangeRate);

    @Mapping(target = "currency", source = "exchangeRates.currency")
    @Mapping(target = "rates", expression = "java(exchangeRates.getRates().stream().map(this::map).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto map(final ExchangeRates exchangeRates);

    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "rates", expression = "java(entities.stream().map(this::mapFromEntity).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRates map(final CurrencyCode currency, final Collection<ExchangeRateEntity> entities);

    abstract ExchangeRateDto.RateDto map(final ExchangeRate.Rate rate);

    protected Map<String, Map<String, Object>> map(final String providerName, final CurrencyRate domain) {
        return Map.of(providerName, domain.getDetails());
    }

    protected Map<String, ExchangeRateEntity.Rate> map(final CurrencyRate domain, final String providerName) {
        return Map.of(providerName, map(domain.getCurrency(), provider.getDefaultCurrency(), domain.getRate()));
    }

    protected ExchangeRateEntity.Rate map(final CurrencyCode currencyFrom, final CurrencyCode currencyTo, final Double currencyValueTo) {
        return ExchangeRateEntity.Rate.of(currencyFrom, currencyTo, currencyValueTo);
    }

    protected List<ExchangeRates> groupAndSortResult(final Stream<ExchangeRateEntity> result) {
        return result
                .collect(Collectors.groupingBy(
                        ExchangeRateEntity::getCurrency,
                        () -> new TreeMap<>(Comparator.comparing(CurrencyCode::getCode)),
                        Collectors.toList()
                ))
                .entrySet()
                .stream()
                .map(entry -> map(entry.getKey(), sortByDateByTheNewest(entry.getValue())))
                .collect(Collectors.toList());
    }

    private Collection<ExchangeRateEntity> sortByDateByTheNewest(final Collection<ExchangeRateEntity> entities) {
        return entities.stream().sorted(Comparator.comparing(ExchangeRateEntity::getDate).reversed()).collect(Collectors.toList());
    }

}

