package mm.expenses.manager.order.currency;

import mm.expenses.manager.common.mapper.AbstractMapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class PriceMapper extends AbstractMapper {

    public abstract Price map(final PriceDto dto);

    public abstract PriceDto map(final Price price);

}

