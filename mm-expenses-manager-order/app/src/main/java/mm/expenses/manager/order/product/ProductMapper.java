package mm.expenses.manager.order.product;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.async.message.PriceMessage;
import mm.expenses.manager.order.async.message.ProductManagementConsumerMessage;
import mm.expenses.manager.order.currency.Price;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collectors.class, StringUtils.class, DateUtils.class}
)
public interface ProductMapper extends AbstractMapper {

    @Mapping(target = "price", expression = "java(mapPrice(message.getPrice()))")
    Product mapCreate(final ProductManagementConsumerMessage message);

    default Product mapUpdate(Product product, final ProductManagementConsumerMessage message) {
        if (MapUtils.isNotEmpty(message.getDetails())) {
            product.setDetails(message.getDetails());
        }
        if (Objects.nonNull(message.getIsDeleted())) {
            product.setDeleted(message.getIsDeleted());
        }
        if (Objects.nonNull(message.getLastModifiedAt())) {
            product.setLastModifiedAt(message.getLastModifiedAt());
        }
        if (Objects.nonNull(message.getPrice())) {
            var price = mapPrice(product, message.getPrice());
            product.setPrice(price);
        }
        return product;
    }

    default Price mapPrice(final Product product, final PriceMessage message) {
        var originalPrice = product.getPrice();
        if (Objects.isNull(message)) {
            return originalPrice;
        }
        if (Objects.nonNull(message.getValue())) {
            originalPrice.setAmount(message.getValue());
        }
        if (Objects.nonNull(message.getCurrency())) {
            originalPrice.setCurrency(message.getCurrency());
        }
        return originalPrice;
    }

    default Price mapPrice(final PriceMessage message) {
        var price = Price.builder();
        if (Objects.nonNull(message)) {
            price.amount(message.getValue()).currency(message.getCurrency());
        }
        return price.build();
    }

}

