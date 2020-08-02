package mm.expenses.manager.order.product;

import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.order.currency.PriceMapper;
import mm.expenses.manager.order.product.model.CreateNewProduct;
import mm.expenses.manager.order.product.model.Product;
import mm.expenses.manager.order.product.model.ProductDto;
import mm.expenses.manager.order.product.model.UpdateProduct;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, uses = {PriceMapper.class})
abstract class ProductMapper extends AbstractMapper {

    @Mapping(target = "name", source = "newProduct.name", qualifiedByName = "trimString")
    @Mapping(target = "createdAt", source = "creationTime")
    @Mapping(target = "lastModifiedAt", source = "creationTime")
    abstract Product map(final CreateNewProduct newProduct, final Instant creationTime);

    @Mapping(target = "name", source = "updateProduct.name", qualifiedByName = "trimString")
    @Mapping(target = "price", source = "updateProduct.price")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "lastModifiedAt", expression = "java(createInstantNow())")
    abstract Product map(final UpdateProduct updateProduct, final ProductEntity entity);

    abstract ProductEntity map(final Product domain);

    abstract Product map(final ProductEntity entity);

    abstract ProductDto mapToDto(final Product domain);

}

