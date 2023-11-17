package mm.expenses.manager.finance.currency;

import mm.expenses.manager.common.utils.i18n.CountryCode;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
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

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        implementationName = MapperImplNaming.CURRENCY_MAPPER,
        imports = {Collectors.class, Comparator.class}
)
public interface CurrencyMapper extends AbstractMapper {

    @Mapping(target = "code", expression = "java(countryCode.name())")
    CountryDto map(final CountryCode countryCode);

    @Mapping(target = "code", expression = "java(currencyCode.name())")
    @Mapping(target = "usedInCountries", expression = "java(currencyCode.getCountryCodesList().stream().map(this::map).sorted(Comparator.comparing(CountryDto::getCode)).collect(Collectors.toList()))")
    CurrencyCodeDto map(final CurrencyCode currencyCode);

    List<CurrencyCodeDto> mapToCurrencyCodeDto(final Collection<CurrencyCode> currencyCode);

    CurrencyDto map(final Collection<String> codes, final Integer currenciesCount);

    default CurrencyDto mapToCurrencyDto(final Collection<String> currencyCodes) {
        return map(currencyCodes, currencyCodes.size());
    }

    default CurrencyInfoDto mapToCurrencyInfo(final Collection<CurrencyCodeDto> currencies) {
        var info = new CurrencyInfoDto();
        info.setCurrencies(currencies.stream().sorted(Comparator.comparing(CurrencyCodeDto::getCode)).toList());
        return info;
    }

}

