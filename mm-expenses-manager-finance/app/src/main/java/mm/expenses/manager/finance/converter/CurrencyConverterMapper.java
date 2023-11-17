package mm.expenses.manager.finance.converter;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionDto;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.finance.config.MapperImplNaming;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        implementationName = MapperImplNaming.CURRENCY_CONVERTER_MAPPER
)
public interface CurrencyConverterMapper extends AbstractMapper {

    @Mapping(target = "from", expression = "java(map(currencyConversion.getFrom()))")
    @Mapping(target = "to", expression = "java(map(currencyConversion.getTo()))")
    CurrencyConversionDto map(final CurrencyConversion currencyConversion);

    @Mapping(target = "code", expression = "java(currencyConversion.getCode().getCode())")
    CurrencyConversionValueDto map(final CurrencyRate currencyConversion);

}

