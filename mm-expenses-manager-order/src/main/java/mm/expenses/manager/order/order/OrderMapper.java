package mm.expenses.manager.order.order;

import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.order.currency.Price;
import mm.expenses.manager.order.currency.PriceMapper;
import mm.expenses.manager.order.order.OrderEntity.OrderedProductEntity;
import mm.expenses.manager.order.order.model.*;
import mm.expenses.manager.order.order.model.CreateNewOrder.CreateNewOrderedProduct;
import mm.expenses.manager.order.order.model.Order.OrderedProduct;
import mm.expenses.manager.order.order.model.OrderDto.OrderedProductDto;
import mm.expenses.manager.order.product.model.Product;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.util.List;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {PriceMapper.class})
abstract class OrderMapper extends AbstractMapper {

    @Mapping(target = "name", source = "newProduct.name", qualifiedByName = "trimString")
    @Mapping(target = "orderedProducts", source = "productOrders")
    @Mapping(target = "priceSummary", source = "productOrders", qualifiedByName = "calculatePrice")
    @Mapping(target = "createdAt", source = "creationTime")
    @Mapping(target = "lastModifiedAt", source = "creationTime")
    abstract Order map(final CreateNewOrder newProduct, final List<OrderedProduct> productOrders, final Instant creationTime);

    @Mapping(target = "name", source = "updateProduct.name", qualifiedByName = "trimString")
    @Mapping(target = "orderedProducts", source = "updatedProductOrders")
    @Mapping(target = "priceSummary", source = "updatedProductOrders", qualifiedByName = "calculatePrice")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "lastModifiedAt", expression = "java(createInstantNow())")
    abstract Order map(final UpdateOrder updateProduct, final OrderEntity entity, final List<OrderedProduct> updatedProductOrders);

    @Mapping(target = "name", source = "domain.name", qualifiedByName = "trimString")
    @Mapping(target = "orderedProducts", source = "domain.orderedProducts")
    abstract OrderEntity map(final Order domain);

    @Mapping(target = "name", source = "entity.name", qualifiedByName = "trimString")
    abstract Order map(final OrderEntity entity);

    @Mapping(target = "orderedProducts", source = "domain.orderedProducts")
    abstract OrderDto mapToDto(final Order domain);

    @Mapping(target = "id", expression = "java(generateId())")
    @Mapping(target = "quantity", source = "newProduct.quantity")
    @Mapping(target = "createdAt", expression = "java(createInstantNow())")
    @Mapping(target = "lastModifiedAt", expression = "java(createInstantNow())")
    abstract OrderedProduct map(final CreateNewOrderedProduct newProduct, final Product product);

    @Mapping(target = "priceSummary", source = "domain.priceSummary")
    abstract OrderedProductEntity map(final OrderedProduct domain);

    abstract OrderedProduct map(final OrderedProductEntity entity);

    public abstract OrderedProductDto mapToDto(final OrderedProduct domain);

    @Named("calculatePrice")
    protected Price calculatePrice(final List<OrderedProduct> products) {
        return Order.calculatePriceSummary(products);
    }

}

