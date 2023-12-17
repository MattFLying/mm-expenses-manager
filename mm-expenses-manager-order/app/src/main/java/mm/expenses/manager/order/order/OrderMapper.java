package mm.expenses.manager.order.order;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.util.IdUtils;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.order.OrderEntity.OrderedProductEntity;
import mm.expenses.manager.order.order.model.*;
import mm.expenses.manager.order.order.model.Order.OrderedProduct;
import mm.expenses.manager.order.product.model.Product;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {StringUtils.class, Collectors.class, Order.class, DateUtils.class, IdUtils.class}
)
public interface OrderMapper extends AbstractMapper {

    @Mapping(target = "name", expression = "java(StringUtils.trim(newProduct.getName()))")
    @Mapping(target = "orderedProducts", source = "productOrders")
    @Mapping(target = "priceSummary", expression = "java(Order.calculatePriceSummary(productOrders))")
    @Mapping(target = "createdAt", source = "creationTime")
    @Mapping(target = "lastModifiedAt", source = "creationTime")
    @Mapping(target = "id", ignore = true)
    Order map(final CreateNewOrderRequest newProduct, final List<OrderedProduct> productOrders, final Instant creationTime);

    @Mapping(target = "name", expression = "java(StringUtils.trim(updateProduct.getName()))")
    @Mapping(target = "orderedProducts", source = "updatedProductOrders")
    @Mapping(target = "priceSummary", expression = "java(Order.calculatePriceSummary(updatedProductOrders))")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "lastModifiedAt", expression = "java(DateUtils.nowAsInstant())")
    Order map(final UpdateOrderRequest updateProduct, final OrderEntity entity, final List<OrderedProduct> updatedProductOrders);

    @Mapping(target = "name", expression = "java(StringUtils.trim(domain.getName()))")
    OrderEntity map(final Order domain);

    @Mapping(target = "name", expression = "java(StringUtils.trim(entity.getName()))")
    Order map(final OrderEntity entity);

    @Mapping(target = "id", expression = "java(IdUtils.generateId())")
    @Mapping(target = "quantity", source = "newProduct.quantity")
    @Mapping(target = "createdAt", expression = "java(DateUtils.nowAsInstant())")
    @Mapping(target = "lastModifiedAt", expression = "java(DateUtils.nowAsInstant())")
    @Mapping(target = "boughtProduct", source = "product")
    @Mapping(target = "boughtProduct.productId", source = "product.id")
    OrderedProduct map(final CreateNewOrderedProductRequest newProduct, final Product product);

    OrderedProductEntity map(final OrderedProduct domain);

    OrderedProduct map(final OrderedProductEntity entity);

    CreateNewOrderRequest map(final CreateNewOrderRequest request);

    UpdateOrderRequest map(final UpdateOrderRequest request);

    OrderResponse mapToResponse(final Order domain);

    @Mapping(target = "content", expression = "java(orderPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(orderPage.hasNext())")
    @Mapping(target = "elements", source = "orderPage.numberOfElements")
    @Mapping(target = "page", source = "orderPage.number")
    OrderPage mapToPageResponse(final Page<Order> orderPage);

}

