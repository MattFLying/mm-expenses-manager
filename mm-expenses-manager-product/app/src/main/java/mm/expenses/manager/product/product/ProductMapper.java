package mm.expenses.manager.product.product;

import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.product.api.product.model.*;
import mm.expenses.manager.product.config.MapperImplNaming;
import mm.expenses.manager.product.price.Price;
import mm.expenses.manager.product.product.command.CreateProductCommand;
import mm.expenses.manager.product.product.command.UpdateProductCommand;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    @Mapping(target = "currency", expression = "java(getCurrency(createPriceRequest.getCurrency()))")
    abstract Price map(final CreatePriceRequest createPriceRequest);

    @Mapping(target = "currency", expression = "java(getCurrencyOrNull(updatePriceRequest.getCurrency()))")
    abstract Price map(final UpdatePriceRequest updatePriceRequest);

    @Mapping(target = "price", expression = "java(map(createProductRequest.getPrice()))")
    abstract CreateProductCommand map(final CreateProductRequest createProductRequest);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "price", expression = "java(map(updateProductRequest.getPrice()))")
    abstract UpdateProductCommand map(final String id, final UpdateProductRequest updateProductRequest);

    ProductPage map(final Page<Product> productPage) {
        final var content = productPage.getContent()
                .stream()
                .map(this::map)
                .collect(Collectors.toList());

        var page = new ProductPage();
        page.content(content);
        page.empty(CollectionUtils.isEmpty(content));
        page.setTotalElements(productPage.getTotalElements());
        page.setTotalPages(productPage.getTotalPages());
        page.setFirst(productPage.isFirst());
        page.setLast(productPage.isLast());
        page.setHasNext(productPage.hasNext());
        page.page(productPage.getNumber());
        page.size(productPage.getSize());
        page.elements(productPage.getNumberOfElements());

        return page;
    }

    protected CurrencyCode getCurrency(final String currency) {
        return CurrencyCode.getCurrencyFromString(currency, true);
    }

    protected CurrencyCode getCurrencyOrNull(final String currency) {
        return StringUtils.isBlank(currency) ? null : getCurrency(currency);
    }

    public SortOrder mapSortOrder(final mm.expenses.manager.product.api.product.model.SortOrderRequest sortOrderApi) {
        if (sortOrderApi == null) {
            return null;
        }
        return SortOrder.valueOf(sortOrderApi.name());
    }

}
