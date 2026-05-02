package mm.expenses.manager.order.core;

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
import org.springframework.data.domain.PageImpl;

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

    @Mapping(target = "content", expression = "java(orderPage.getContent())")
    @Mapping(target = "hasNext", expression = "java(orderPage.hasNext())")
    @Mapping(target = "elements", source = "orderPage.numberOfElements")
    @Mapping(target = "page", source = "orderPage.number")
    @Mapping(target = "size", source = "orderPage.size")
    @Mapping(target = "totalElements", source = "orderPage.totalElements")
    @Mapping(target = "totalPages", source = "orderPage.totalPages")
    OrderPage mapToPageResponse(final Page<OrderResponse> orderPage);

    default Page<OrderResponse> mapToPageOrderResponse(final Page<Order> orderPage) {
        final var content = orderPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

        return new PageImpl<>(content, orderPage.getPageable(), orderPage.getTotalElements());
    }

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
        final var prices = orderedProduct.getPrices();
        if (Objects.nonNull(prices)) {
            final var originalPriceOpt = orderedProduct.getPrices()
                    .stream()
                    .filter(OrderedProductPrice::isPriceOriginal)
                    .findAny();

            if (originalPriceOpt.isPresent()) {
                final var originalPrice = originalPriceOpt.get();

                final var price = new PriceResponse();
                price.setCurrency(originalPrice.getCurrency().getCode());
                price.setValue(BigDecimalWrapper.of(originalPrice.getValue().doubleValue()));
                price.setIsOriginal(originalPrice.isPriceOriginal());
                return List.of(price);
            }
        }
        return null;
    }

    default List<PriceResponse> mapPriceToResponse(final OrderedProduct orderedProduct, final CurrencyCode defaultCurrency) {
        final var prices = orderedProduct.getPrices();
        if (Objects.nonNull(prices)) {
            final var originalPriceOpt = orderedProduct.getPrices()
                    .stream()
                    .filter(price -> price.getCurrency().equals(defaultCurrency))
                    .findAny();

            if (originalPriceOpt.isPresent()) {
                final var originalPrice = originalPriceOpt.get();

                final var price = new PriceResponse();
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
            final var product = orderedProduct.getProduct();
            if (Objects.nonNull(product)) {
                return product.getId();
            }
        }
        return null;
    }

}

