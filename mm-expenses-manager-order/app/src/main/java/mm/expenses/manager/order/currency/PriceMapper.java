package mm.expenses.manager.order.currency;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.order.api.product.model.PriceRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PriceMapper extends AbstractMapper {

    Price map(final PriceRequest dto);

    PriceRequest map(final Price price);

}

