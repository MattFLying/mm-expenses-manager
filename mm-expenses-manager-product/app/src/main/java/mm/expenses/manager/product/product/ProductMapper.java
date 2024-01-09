package mm.expenses.manager.product.product;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.product.api.product.model.*;
import mm.expenses.manager.product.price.Price;
import mm.expenses.manager.product.product.command.CreateProductCommand;
import mm.expenses.manager.product.product.command.UpdateProductCommand;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collectors.class, StringUtils.class, DateUtils.class}
)
public interface ProductMapper extends AbstractMapper {

    @Mapping(target = "price", source = "price")
    CreateProductCommand map(final CreateProductRequest createProductRequest, final Price price);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "price", source = "price", conditionExpression = "java(price != null)")
    UpdateProductCommand map(final UUID id, final UpdateProductRequest updateProductRequest, final Price price);

    @Mapping(target = "price", source = "priceResponse")
    ProductResponse map(final Product product, final PriceResponse priceResponse);

    ProductResponse mapProductResponse(final Product product);

    @Mapping(target = "content", expression = "java(productPage.getContent().stream().map(product -> mapProductResponse(product)).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(productPage.hasNext())")
    @Mapping(target = "elements", source = "productPage.numberOfElements")
    @Mapping(target = "page", source = "productPage.number")
    ProductPage map(final Page<Product> productPage);

}
