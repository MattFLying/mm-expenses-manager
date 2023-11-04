package mm.expenses.manager.finance.currency;

import mm.expenses.manager.common.i18n.CountryCode;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.api.currency.model.CountryDto;
import mm.expenses.manager.finance.api.currency.model.CurrencyCodeDto;
import mm.expenses.manager.finance.api.currency.model.CurrencyDto;
import mm.expenses.manager.finance.api.currency.model.CurrencyInfoDto;
import mm.expenses.manager.finance.config.MapperImplNaming;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = MapperImplNaming.CURRENCY_MAPPER)
abstract class CurrencyMapper extends AbstractMapper {

    @Mapping(target = "code", expression = "java(countryCode.name())")
    @Mapping(target = "name", expression = "java(countryCode.getName())")
    abstract CountryDto map(final CountryCode countryCode);

    @Mapping(target = "code", expression = "java(currencyCode.name())")
    @Mapping(target = "usedInCountries", expression = "java(map(currencyCode.getCountryCodesList()))")
    abstract CurrencyCodeDto mapToCurrencyCodeDto(final CurrencyCode currencyCode);

    abstract List<CurrencyCodeDto> mapToCurrencyCodeDto(final Collection<CurrencyCode> currencyCode);

    abstract CurrencyDto mapToCurrencyDto(final Collection<String> codes, final Integer currenciesCount);

    public CurrencyDto mapToCurrencyDto(final Collection<String> currencyCodes) {
        return mapToCurrencyDto(currencyCodes, currencyCodes.size());
    }

    public CurrencyInfoDto mapToCurrencyInfo(final Collection<CurrencyCodeDto> currencies) {
        var info = new CurrencyInfoDto();
        info.setCurrencies(currencies.stream().sorted(Comparator.comparing(CurrencyCodeDto::getCode)).toList());
        return info;
    }

    public List<CountryDto> map(final Collection<CountryCode> countryCodes) {
        return countryCodes.stream()
                .map(this::map)
                .sorted(Comparator.comparing(CountryDto::getCode))
                .collect(Collectors.toList());
    }

}

