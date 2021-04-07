package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.exchangerate.ExchangeRate.Rate;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto.ExchangeRateDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesDto.ExchangeRateDto.RateDto;
import mm.expenses.manager.finance.exchangerate.dto.ExchangeRatesAccumulatePage.ExchangeRatePage;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.DefaultCurrencyProvider;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
abstract class ExchangeRateMapper extends AbstractMapper {

    @Autowired
    protected CurrencyProviders providers;

    @Mapping(target = "date", expression = "java(fromInstantToLocalDate(entity.getDate()))")
    @Mapping(target = "rate", expression = "java(map(entity.getRateByProvider(providers.getProviderName(), true)))")
    abstract ExchangeRateDto mapToDto(final ExchangeRate entity);

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(domain.getDate()))")
    @Mapping(target = "ratesByProvider", expression = "java(map(domain, providers.getProviderName()))")
    @Mapping(target = "detailsByProvider", expression = "java(map(providers.getProviderName(), domain))")
    @Mapping(target = "createdAt", source = "now")
    @Mapping(target = "modifiedAt", source = "now")
    abstract ExchangeRate map(final CurrencyRate domain, final Instant now);

    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "rates", expression = "java(entities.stream().map(this::mapToDto).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto map(final CurrencyCode currency, final Collection<ExchangeRate> entities);

    @Mapping(target = "currency", expression = "java(entity.getCurrency())")
    @Mapping(target = "rates", expression = "java(java.util.stream.Stream.of(entity).map(this::mapToDto).collect(java.util.stream.Collectors.toList()))")
    abstract ExchangeRatesDto map(final ExchangeRate entity);

    abstract RateDto map(final Rate rate);

    protected DefaultCurrencyProvider<?> getProvider() {
        return providers.getProvider();
    }

    protected Map<String, Map<String, Object>> map(final String providerName, final CurrencyRate domain) {
        return new HashMap<>(Map.of(providerName, domain.getDetails()));
    }

    protected Map<String, Rate> map(final CurrencyRate domain, final String providerName) {
        return new HashMap<>(Map.of(providerName, map(domain.getCurrency(), providers.getCurrency(), domain.getRate())));
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

    protected Collection<ExchangeRatePage> groupAndSortPagedResult(final Stream<Page<ExchangeRate>> result) {
        return result
                .map(page -> new ExchangeRatePage(map(getCurrencyForPage(page), sortByDateByTheNewest(page.getContent())), page))
                .filter(page -> !page.getContent().getRates().isEmpty())
                .sorted(Comparator.comparing(page -> page.getContent().getCurrency()))
                .collect(Collectors.toList());
    }

    private CurrencyCode getCurrencyForPage(final Page<ExchangeRate> page) {
        return page.getContent().stream().findFirst().map(ExchangeRate::getCurrency).orElse(CurrencyCode.UNDEFINED);
    }

    private Collection<ExchangeRate> sortByDateByTheNewest(final Collection<ExchangeRate> entities) {
        return entities.stream().sorted(Comparator.comparing(ExchangeRate::getDate).reversed()).collect(Collectors.toList());
    }

}

