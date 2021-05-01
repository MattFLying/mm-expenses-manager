package mm.expenses.manager.finance.converter;

import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.converter.dto.CurrencyConversionDto;
import mm.expenses.manager.finance.converter.dto.CurrencyConversionDto.CurrencyConversionValueDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = "CurrencyConverterMapperImpl")
abstract class CurrencyConverterMapper extends AbstractMapper {

    @Mapping(target = "from", expression = "java(map(currencyConversion.getFrom()))")
    @Mapping(target = "to", expression = "java(map(currencyConversion.getTo()))")
    abstract CurrencyConversionDto map(final CurrencyConversion currencyConversion);

    abstract CurrencyConversionValueDto map(final CurrencyConversion.CurrencyRate currencyConversion);

}

