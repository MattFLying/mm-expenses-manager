package mm.expenses.manager.product.product;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.product.api.product.model.*;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collectors.class, StringUtils.class, DateUtils.class}
)
public interface ProductMapper extends AbstractMapper {

    @Mapping(target = "price", source = "priceResponse")
    ProductResponse map(final Product product, final PriceResponse priceResponse);

    ProductResponse mapProductResponse(final Product product);

    @Mapping(target = "content", expression = "java(productPage.getContent().stream().map(product -> mapProductResponse(product)).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(productPage.hasNext())")
    @Mapping(target = "elements", source = "productPage.numberOfElements")
    @Mapping(target = "page", source = "productPage.number")
    ProductPage map(final Page<Product> productPage);

}
