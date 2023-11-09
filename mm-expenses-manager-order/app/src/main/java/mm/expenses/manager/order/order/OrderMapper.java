package mm.expenses.manager.order.order;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.order.api.order.model.*;
import mm.expenses.manager.order.currency.Price;
import mm.expenses.manager.order.currency.PriceMapper;
import mm.expenses.manager.order.order.OrderEntity.OrderedProductEntity;
import mm.expenses.manager.order.order.model.*;
import mm.expenses.manager.order.order.model.Order.OrderedProduct;
import mm.expenses.manager.order.product.model.Product;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {PriceMapper.class})
abstract class OrderMapper extends AbstractMapper {

    @Mapping(target = "name", source = "newProduct.name", qualifiedByName = "trimString")
    @Mapping(target = "orderedProducts", source = "productOrders")
    @Mapping(target = "priceSummary", source = "productOrders", qualifiedByName = "calculatePrice")
    @Mapping(target = "createdAt", source = "creationTime")
    @Mapping(target = "lastModifiedAt", source = "creationTime")
    abstract Order map(final CreateNewOrderRequest newProduct, final List<OrderedProduct> productOrders, final Instant creationTime);

    @Mapping(target = "name", source = "updateProduct.name", qualifiedByName = "trimString")
    @Mapping(target = "orderedProducts", source = "updatedProductOrders")
    @Mapping(target = "priceSummary", source = "updatedProductOrders", qualifiedByName = "calculatePrice")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "lastModifiedAt", expression = "java(createInstantNow())")
    abstract Order map(final UpdateOrderRequest updateProduct, final OrderEntity entity, final List<OrderedProduct> updatedProductOrders);

    @Mapping(target = "name", source = "domain.name", qualifiedByName = "trimString")
    @Mapping(target = "orderedProducts", source = "domain.orderedProducts")
    abstract OrderEntity map(final Order domain);

    @Mapping(target = "name", source = "entity.name", qualifiedByName = "trimString")
    abstract Order map(final OrderEntity entity);

    @Mapping(target = "id", expression = "java(generateId())")
    @Mapping(target = "quantity", source = "newProduct.quantity")
    @Mapping(target = "createdAt", expression = "java(createInstantNow())")
    @Mapping(target = "lastModifiedAt", expression = "java(createInstantNow())")
    abstract OrderedProduct map(final CreateNewOrderedProductRequest newProduct, final Product product);

    @Mapping(target = "priceSummary", source = "domain.priceSummary")
    abstract OrderedProductEntity map(final OrderedProduct domain);

    abstract OrderedProduct map(final OrderedProductEntity entity);

    @Named("calculatePrice")
    protected Price calculatePrice(final List<OrderedProduct> products) {
        return Order.calculatePriceSummary(products);
    }

    abstract CreateNewOrderRequest mapToNewRequest(final CreateNewOrderRequest request);

    abstract UpdateOrderRequest mapToUpdatedRequest(UpdateOrderRequest request);

    abstract OrderResponse mapToResponse(final Order domain);

    OrderPage mapToPageResponse(final Page<Order> orderPage) {
        final var content = orderPage.getContent()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        var page = new OrderPage();
        page.content(content);
        page.empty(CollectionUtils.isEmpty(content));
        page.setTotalElements(orderPage.getTotalElements());
        page.setTotalPages(orderPage.getTotalPages());
        page.setFirst(orderPage.isFirst());
        page.setLast(orderPage.isLast());
        page.setHasNext(orderPage.hasNext());
        page.page(orderPage.getNumber());
        page.size(orderPage.getSize());
        page.elements(orderPage.getNumberOfElements());

        return page;
    }

}

