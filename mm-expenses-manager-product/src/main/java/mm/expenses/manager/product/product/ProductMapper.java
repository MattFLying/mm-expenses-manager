package mm.expenses.manager.product.product;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.mapper.AbstractMapper;
import mm.expenses.manager.product.config.MapperImplNaming;
import mm.expenses.manager.product.price.Price;
import mm.expenses.manager.product.product.command.CreateProductCommand;
import mm.expenses.manager.product.product.dto.request.PriceRequest;
import mm.expenses.manager.product.product.dto.request.ProductRequest;
import mm.expenses.manager.product.product.dto.response.PriceResponse;
import mm.expenses.manager.product.product.dto.response.ProductPage;
import mm.expenses.manager.product.product.dto.response.ProductResponse;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = MapperImplNaming.PRODUCT_MAPPER)
abstract class ProductMapper extends AbstractMapper {

    @Mapping(target = "currency", expression = "java(price.getCurrency().getCode())")
    @Mapping(target = "value", expression = "java(price.getValue())")
    abstract PriceResponse map(final Price price);

    @Mapping(target = "price", expression = "java(map(product.getPrice()))")
    abstract ProductResponse map(final Product product);

    @Mapping(target = "currency", expression = "java(getCurrency(priceRequest.getCurrency()))")
    abstract Price map(final PriceRequest priceRequest);

    @Mapping(target = "price", expression = "java(map(productRequest.getPrice()))")
    abstract CreateProductCommand map(final ProductRequest productRequest);

    ProductPage map(final Page<Product> productPage) {
        final var content = productPage.getContent()
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

        return ProductPage.builder()
                .content(content)
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .hasNext(productPage.hasNext())
                .isFirst(productPage.isFirst())
                .isLast(productPage.isLast())
                .build();
    }

    protected CurrencyCode getCurrency(final String currency) {
        return CurrencyCode.getCurrencyFromString(currency, true);
    }

}
