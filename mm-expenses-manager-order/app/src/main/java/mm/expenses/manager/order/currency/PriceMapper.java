package mm.expenses.manager.order.currency;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.order.api.product.model.PriceRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class PriceMapper extends AbstractMapper {

    public abstract Price map(final PriceRequest dto);

    public abstract PriceRequest map(final Price price);

}

