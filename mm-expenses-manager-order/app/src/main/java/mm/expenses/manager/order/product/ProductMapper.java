package mm.expenses.manager.order.product;

import mm.expenses.manager.common.kafka.message.PriceMessage;
import mm.expenses.manager.common.kafka.message.ProductManagementMessage;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
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

    @Mapping(target = "prices", expression = "java(mapPrice(message.getPrice(), message.getLastModifiedAt()))")
    Product mapCreate(final ProductManagementMessage message);

    ProductPrice mapProductPrice(final PriceMessage message);

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
            product.setPrices(mapPrices(product, message.getPrice()));
        }
        return product;
    }

    default ProductPrices mapPrices(final Product product, final PriceMessage message) {
        final var currentPrices = product.getPrices();
        if (Objects.isNull(message)) {
            return currentPrices;
        }

        final var priceOfCurrencyExists = currentPrices.exists(message.getCurrency());
        if (priceOfCurrencyExists) {
            currentPrices.update(message.getCurrency(), message.getValue(), message.getDate(), message.getIsOriginal());
        } else {
            currentPrices.add(mapProductPrice(message));
        }
        return currentPrices;
    }

    default ProductPrices mapPrice(final PriceMessage message, final Instant date) {
        final var prices = new ProductPrices();
        final var price = new ProductPrice();
        if (Objects.nonNull(message)) {
            price.setValue(message.getValue());
            price.setCurrency(message.getCurrency());
            price.setDate(DateUtils.instantToLocalDate(date).toString());
            price.setOriginal(message.getIsOriginal());

            prices.add(mapProductPrice(message));
        }
        return prices;
    }

}

