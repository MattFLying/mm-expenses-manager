package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.finance.exchangerate.model.ExchangeRate;
import mm.expenses.manager.finance.financial.CurrencyRate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
abstract class ExchangeRateMapper extends AbstractMapper {

    @Mapping(target = "date", expression = "java(fromInstantToLocalDate(entity.getDate()))")
    abstract ExchangeRate map(final ExchangeRateEntity entity);

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(domain.getDate()))")
    abstract ExchangeRateEntity map(final ExchangeRate domain);

    @Mapping(target = "date", expression = "java(fromLocalDateToInstant(domain.getDate()))")
    @Mapping(target = "createdAt", expression = "java(createInstantNow())")
    abstract ExchangeRateEntity map(final CurrencyRate domain);

}

