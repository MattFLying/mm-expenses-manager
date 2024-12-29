package mm.expenses.manager.order.product;

import mm.expenses.manager.common.kafka.message.PriceMessage;
import mm.expenses.manager.common.kafka.message.ProductManagementMessage;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.util.DateUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collectors.class, StringUtils.class, DateUtils.class}
)
public interface ProductMapper extends AbstractMapper {

    @Mapping(target = "price", expression = "java(mapPrice(message.getPrice(), message.getLastModifiedAt()))")
    Product mapCreate(final ProductManagementMessage message);

    default Product mapUpdate(Product product, final ProductManagementMessage message) {
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

    default Prices mapPrice(final Product product, final PriceMessage message) {
        var originalPrice = product.getPrice();
        if (Objects.isNull(message)) {
            return originalPrice;
        }
        if (Objects.nonNull(message.getValue())) {
            if (originalPrice.size() == 1) {
                originalPrice.get(0).setValue(message.getValue());
            }
        }
        if (Objects.nonNull(message.getCurrency())) {
            if (originalPrice.size() == 1) {
                originalPrice.get(0).setCurrency(message.getCurrency());
            }
        }
        return originalPrice;
    }

    default Prices mapPrice(final PriceMessage message, final Instant date) {
        var price = new Price();
        if (Objects.nonNull(message)) {
            price.setValue(message.getValue());
            price.setCurrency(message.getCurrency());
            price.setDate(date);
        }
        return new Prices(price);
    }

}

