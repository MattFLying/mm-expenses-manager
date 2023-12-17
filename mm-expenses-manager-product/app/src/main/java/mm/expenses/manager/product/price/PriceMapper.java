package mm.expenses.manager.product.price;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.product.api.product.model.*;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {CurrencyCode.class}
)
public interface PriceMapper extends AbstractMapper {

    @Mapping(target = "currency", expression = "java(price.getCurrency().getCode())")
    @Mapping(target = "value", source = "price.value")
    PriceResponse map(final Price price);

    @Mapping(target = "currency", expression = "java(CurrencyCode.getCurrencyFromString(createPriceRequest.getCurrency()))")
    Price map(final CreatePriceRequest createPriceRequest);

    @Mapping(target = "currency", expression = "java(CurrencyCode.getCurrencyFromStringOrNull(updatePriceRequest.getCurrency()))")
    Price map(final UpdatePriceRequest updatePriceRequest);

}
