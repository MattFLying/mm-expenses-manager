package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRates;
import mm.expenses.manager.finance.exchangerate.model.dto.ExchangeRatesDto;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.DefaultCurrencyProvider;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
abstract class ExchangeRateMapper extends AbstractMapper {

    @Autowired
    protected DefaultCurrencyProvider<?> provider;

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(entity.getDate()))")
    abstract ExchangeRateEntity mapToEntityFromDomain(final ExchangeRate entity);

    @Mapping(target = "date", expression = "java(fromInstantToLocalDate(entity.getDate()))")
    abstract ExchangeRate mapFromEntity(final ExchangeRateEntity entity);

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(domain.getDate()))")
    @Mapping(target = "ratesByProvider", expression = "java(mapToRatesByProvider(domain, provider.getName()))")
    @Mapping(target = "detailsByProvider", expression = "java(mapToDetailsByProvider(domain, provider.getName()))")
    @Mapping(target = "createdAt", expression = "java(createInstantNow())")
    abstract ExchangeRateEntity mapToEntity(final CurrencyRate domain);

    @Mapping(target = "date", source = "exchangeRate.date")
    @Mapping(target = "rate", expression = "java(exchangeRate.getRateByProvider(provider.getName()))")
    abstract ExchangeRatesDto.ExchangeRateDto mapToDto(final ExchangeRate exchangeRate);

    public ExchangeRatesDto mapToDto(final ExchangeRates exchangeRates) {
        return ExchangeRatesDto.builder()
                .currency(exchangeRates.getCurrency())
                .rates(exchangeRates.getRates().stream().map(this::mapToDto).collect(Collectors.toList()))
                .build();
    }

    protected ExchangeRates map(final CurrencyCode currency, final Collection<ExchangeRateEntity> entities) {
        return ExchangeRates.builder()
                .currency(currency)
                .rates(entities.stream().map(this::mapFromEntity).collect(Collectors.toList()))
                .build();
    }

    protected Map<String, Double> mapToRatesByProvider(final CurrencyRate domain, final String providerName) {
        return Map.of(providerName, domain.getRate());
    }

    protected Map<String, Map<String, Object>> mapToDetailsByProvider(final CurrencyRate domain, final String providerName) {
        return Map.of(providerName, domain.getDetails());
    }

}

