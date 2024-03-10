package mm.expenses.manager.order.order;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.util.IdUtils;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.product.Product;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {StringUtils.class, Collectors.class, DateUtils.class, IdUtils.class}
)
public interface OrderMapper extends AbstractMapper {

    @Mapping(target = "name", expression = "java(StringUtils.trim(newProduct.getName()))")
    @Mapping(target = "products", source = "productOrders")
    @Mapping(target = "priceSummary", expression = "java(Order.calculatePriceSummary(productOrders))")
    @Mapping(target = "createdAt", source = "creationTime")
    @Mapping(target = "lastModifiedAt", source = "creationTime")
    @Mapping(target = "id", ignore = true)
    Order map(final CreateNewOrderRequest newProduct, final List<OrderedProduct> productOrders, final Instant creationTime);

    @Mapping(target = "name", expression = "java(updateProduct.getName() != null ? StringUtils.trim(updateProduct.getName()) : entity.getName())")
    @Mapping(target = "products", source = "updatedProductOrders")
    @Mapping(target = "priceSummary", expression = "java(Order.calculatePriceSummary(updatedProductOrders))")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "lastModifiedAt", expression = "java(DateUtils.nowAsInstant())")
    Order map(final UpdateOrderRequest updateProduct, final Order entity, final Collection<OrderedProduct> updatedProductOrders);

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "quantity", source = "newProduct.quantity")
    @Mapping(target = "createdAt", expression = "java(DateUtils.nowAsInstant())")
    @Mapping(target = "lastModifiedAt", expression = "java(DateUtils.nowAsInstant())")
    @Mapping(target = "price", source = "product.price")
    OrderedProduct map(final CreateNewOrderedProductRequest newProduct, final Product product);

    @Mapping(target = "orderedProducts", source = "order.products")
    OrderResponse mapToResponse(final Order order);

    @Mapping(target = "content", expression = "java(orderPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(orderPage.hasNext())")
    @Mapping(target = "elements", source = "orderPage.numberOfElements")
    @Mapping(target = "page", source = "orderPage.number")
    OrderPage mapToPageResponse(final Page<Order> orderPage);

}

