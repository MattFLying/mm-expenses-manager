package mm.expenses.manager.order.order;

import lombok.val;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.price.Prices;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.util.IdUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.order.api.order.model.*;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {StringUtils.class, Collectors.class, DateUtils.class, IdUtils.class}
)
public interface OrderMapper extends AbstractMapper {

    @Mapping(target = "priceSummary", expression = "java(mapPriceToResponse(order.getPriceSummary()))")
    @Mapping(target = "orderedProducts", source = "order.products")
    OrderResponse mapToResponse(final Order order);

    @Mapping(target = "content", expression = "java(orderPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(orderPage.hasNext())")
    @Mapping(target = "elements", source = "orderPage.numberOfElements")
    @Mapping(target = "page", source = "orderPage.number")
    OrderPage mapToPageResponse(final Page<Order> orderPage);

    @Mapping(target = "currency", expression = "java(value.getCurrency().getCode())")
    @Mapping(target = "amount", expression = "java(mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper.of(value.getValue()))")
    PriceResponse mapPriceToResponse(final Price value);

    @Mapping(target = "productId", expression = "java(mapProductId(orderedProduct))")
    @Mapping(target = "price", expression = "java(mapPriceToResponse(orderedProduct))")
    OrderedProductResponse orderedProductToOrderedProductResponse(final OrderedProduct orderedProduct);

    default List<PriceResponse> mapPriceToResponse(final Prices value) {
        return Objects.nonNull(value) ? value.stream().map(this::mapPriceToResponse).toList() : Collections.emptyList();
    }

    default List<PriceResponse> mapPriceToResponse(final OrderedProduct orderedProduct) {
        val price = new PriceResponse();
        price.setCurrency(orderedProduct.getCurrency().getCode());
        price.setAmount(BigDecimalWrapper.of(orderedProduct.getValue()));
        price.setIsOriginal(orderedProduct.isPriceOriginal());

        return List.of(price);
    }

    default UUID mapProductId(final OrderedProduct orderedProduct) {
        if (Objects.nonNull(orderedProduct)) {
            val product = orderedProduct.getProduct();
            if (Objects.nonNull(product)) {
                return product.getId();
            }
        }
        return null;
    }

}

