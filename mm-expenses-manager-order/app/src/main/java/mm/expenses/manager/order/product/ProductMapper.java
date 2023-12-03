package mm.expenses.manager.order.product;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.order.api.product.model.CreateNewProductRequest;
import mm.expenses.manager.order.api.product.model.ProductPage;
import mm.expenses.manager.order.api.product.model.ProductResponse;
import mm.expenses.manager.order.api.product.model.UpdateProductRequest;
import mm.expenses.manager.order.config.MapperImplNaming;
import mm.expenses.manager.order.product.model.Product;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        implementationName = MapperImplNaming.PRODUCT_MAPPER,
        imports = {Collectors.class, StringUtils.class, DateUtils.class}
)
public interface ProductMapper extends AbstractMapper {

    @Mapping(target = "name", expression = "java(StringUtils.trim(newProduct.getName()))")
    @Mapping(target = "createdAt", source = "creationTime")
    @Mapping(target = "lastModifiedAt", source = "creationTime")
    Product map(final CreateNewProductRequest newProduct, final Instant creationTime);

    @Mapping(target = "name", expression = "java(StringUtils.trim(updateProduct.getName()))")
    @Mapping(target = "price", source = "updateProduct.price")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "lastModifiedAt", expression = "java(DateUtils.nowAsInstant())")
    Product map(final UpdateProductRequest updateProduct, final ProductEntity entity);

    ProductEntity map(final Product domain);

    Product map(final ProductEntity entity);

    CreateNewProductRequest map(final CreateNewProductRequest request);

    UpdateProductRequest map(final UpdateProductRequest request);

    ProductResponse mapToResponse(final Product domain);

    @Mapping(target = "content", expression = "java(productPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(productPage.hasNext())")
    @Mapping(target = "elements", source = "productPage.numberOfElements")
    @Mapping(target = "page", source = "productPage.number")
    ProductPage mapToPageResponse(final Page<Product> productPage);

}

