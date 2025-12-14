package mm.expenses.manager.order.order;

import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
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

    @Mapping(target = "name", source = "order.name")
    @Mapping(target = "orderedProducts", expression = "java(createdOrderedProductListToOrderedProductResponseList(order.getProducts(), defaultCurrency))")
    @Mapping(target = "priceSummary", expression = "java(mapPriceToResponse(new mm.expenses.manager.common.utils.price.Prices(order.getPriceSummary(defaultCurrency))))")
    OrderResponse mapToResponse(final Order order, final CurrencyCode defaultCurrency);

    @Mapping(target = "content", expression = "java(orderPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(orderPage.hasNext())")
    @Mapping(target = "elements", source = "orderPage.numberOfElements")
    @Mapping(target = "page", source = "orderPage.number")
    OrderPage mapToPageResponse(final Page<Order> orderPage);

    @Mapping(target = "currency", expression = "java(value.getCurrency().getCode())")
    @Mapping(target = "value", expression = "java(mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper.of(value.getValue()))")
    PriceResponse mapPriceToResponse(final Price value);

    @Mapping(target = "productId", expression = "java(mapProductId(orderedProduct))")
    @Mapping(target = "price", expression = "java(mapPriceToResponse(orderedProduct))")
    OrderedProductResponse orderedProductToOrderedProductResponse(final OrderedProduct orderedProduct);

    List<OrderedProductResponse> createdOrderedProductListToOrderedProductResponseList(final List<OrderedProduct> list);

    @Mapping(target = "productId", expression = "java(mapProductId(orderedProduct))")
    @Mapping(target = "price", expression = "java(mapPriceToResponse(orderedProduct, defaultCurrency))")
    @Mapping(target = "priceSummary", expression = "java(mapPriceToResponse(new mm.expenses.manager.common.utils.price.Prices(orderedProduct.getPriceSummary(defaultCurrency))))")
    OrderedProductResponse orderedProductToOrderedProductResponse(final OrderedProduct orderedProduct, final CurrencyCode defaultCurrency);

    default List<OrderedProductResponse> createdOrderedProductListToOrderedProductResponseList(final List<OrderedProduct> list, final CurrencyCode defaultCurrency) {
        return list.stream()
                .map(orderedProduct -> orderedProductToOrderedProductResponse(orderedProduct, defaultCurrency))
                .toList();
    }

    default List<PriceResponse> mapPriceToResponse(final Prices value) {
        return Objects.nonNull(value) ? value.stream().map(this::mapPriceToResponse).toList() : Collections.emptyList();
    }

    default List<PriceResponse> mapPriceToResponse(final OrderedProduct orderedProduct) {
        val prices = orderedProduct.getPrices();
        if (Objects.nonNull(prices)) {
            val originalPriceOpt = orderedProduct.getPrices()
                    .stream()
                    .filter(OrderedProductPrice::isPriceOriginal)
                    .findAny();

            if (originalPriceOpt.isPresent()) {
                val originalPrice = originalPriceOpt.get();

                val price = new PriceResponse();
                price.setCurrency(originalPrice.getCurrency().getCode());
                price.setValue(BigDecimalWrapper.of(originalPrice.getValue().doubleValue()));
                price.setIsOriginal(originalPrice.isPriceOriginal());
                return List.of(price);
            }
        }
        return null;
    }

    default List<PriceResponse> mapPriceToResponse(final OrderedProduct orderedProduct, final CurrencyCode defaultCurrency) {
        val prices = orderedProduct.getPrices();
        if (Objects.nonNull(prices)) {
            val originalPriceOpt = orderedProduct.getPrices()
                    .stream()
                    .filter(price -> price.getCurrency().equals(defaultCurrency))
                    .findAny();

            if (originalPriceOpt.isPresent()) {
                val originalPrice = originalPriceOpt.get();

                val price = new PriceResponse();
                price.setCurrency(originalPrice.getCurrency().getCode());
                price.setValue(BigDecimalWrapper.of(originalPrice.getValue()));
                price.setIsOriginal(originalPrice.isPriceOriginal());
                return List.of(price);
            }
        }
        return null;
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

