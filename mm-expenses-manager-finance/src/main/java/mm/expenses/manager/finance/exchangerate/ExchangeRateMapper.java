package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.financial.CurrencyDetailsParser;
import mm.expenses.manager.finance.financial.CurrencyRate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
abstract class ExchangeRateMapper extends AbstractMapper {

    @Autowired
    protected CurrencyDetailsParser detailsParser;

    @Mapping(target = "date", expression = "java(fromInstantToLocalDate(entity.getDate()))")
    @Mapping(target = "details", expression = "java(detailsParser.parseJsonDetailsToCurrencyRateDetailsTypes(entity.getDetails()))")
    abstract ExchangeRate map(final ExchangeRateEntity entity);

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(domain.getDate()))")
    @Mapping(target = "createdAt", expression = "java(createInstantNow())")
    @Mapping(target = "details", expression = "java(detailsParser.parseCurrencyRateDetailsToJson(domain))")
    abstract ExchangeRateEntity map(final CurrencyRate domain);

}

