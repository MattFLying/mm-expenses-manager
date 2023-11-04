package mm.expenses.manager.finance.converter;

import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionDto;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.finance.config.MapperImplNaming;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = MapperImplNaming.CURRENCY_CONVERTER_MAPPER)
abstract class CurrencyConverterMapper extends AbstractMapper {

    @Mapping(target = "from", expression = "java(map(currencyConversion.getFrom()))")
    @Mapping(target = "to", expression = "java(map(currencyConversion.getTo()))")
    abstract CurrencyConversionDto map(final CurrencyConversion currencyConversion);

    @Mapping(target = "code", expression = "java(currencyConversion.getCode().getCode())")
    abstract CurrencyConversionValueDto map(final CurrencyRate currencyConversion);

}

