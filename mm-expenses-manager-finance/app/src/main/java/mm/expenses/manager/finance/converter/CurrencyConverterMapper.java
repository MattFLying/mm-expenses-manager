package mm.expenses.manager.finance.converter;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.finance.converter.CurrencyConversion.CurrencyRate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface CurrencyConverterMapper extends AbstractMapper {

    @Mapping(target = "from", expression = "java(map(currencyConversion.from()))")
    @Mapping(target = "to", expression = "java(map(currencyConversion.to()))")
    CurrencyConversionResponse map(final CurrencyConversion currencyConversion);

    @Mapping(target = "code", expression = "java(currencyConversion.code().getCode())")
    @Mapping(target = "value", expression = "java(mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper.of(currencyConversion.value()).doubleValue())")
    CurrencyConversionValueDto map(final CurrencyRate currencyConversion);

    List<CurrencyConversionResponse> map(final List<CurrencyConversion> currencyConversion);

}

