package mm.expenses.manager.product.product;

import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.kafka.message.ProductManagementMessage;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.product.api.product.model.*;
import mm.expenses.manager.product.price.ProductPriceMapper;
import mm.expenses.manager.product.price.ProductPrice;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {Collectors.class, StringUtils.class, DateUtils.class},
        uses = {ProductPriceMapper.class}
)
public interface ProductMapper extends AbstractMapper {

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "details", source = "product.details")
    @Mapping(target = "price", expression = "java(mm.expenses.manager.product.price.ProductPriceMapper.INSTANCE.map(map(product.getPrices(), expectedCurrency)))")
    ProductResponse mapProductResponse(final Product product, final CurrencyCode expectedCurrency);

    @Mapping(target = "id", source = "product.id")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "createdAt", source = "product.createdAt")
    @Mapping(target = "lastModifiedAt", source = "product.lastModifiedAt")
    ProductResponse mapProductResponse(final Product product, final ProductPrice price);

    @Mapping(target = "price", expression = "java(mm.expenses.manager.product.price.ProductPriceMapper.INSTANCE.mapOriginalToMessage(product.getPrices()))")
    @Mapping(target = "isDeleted", source = "product.deleted")
    @Mapping(target = "operation", source = "operation")
    ProductManagementMessage map(final Product product, final AsyncKafkaOperation operation);

    @Mapping(target = "id", source = "product.productId")
    @Mapping(target = "name", source = "product.name")
    @Mapping(target = "details", source = "product.details")
    @Mapping(target = "price", expression = "java(mm.expenses.manager.product.price.ProductPriceMapper.INSTANCE.mapToPrice(product))")
    ProductResponse map(final ProductFilterView product);

    @Mapping(target = "content", expression = "java(productPage.getContent().stream().map(product -> map(product)).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(productPage.hasNext())")
    @Mapping(target = "elements", source = "productPage.numberOfElements")
    @Mapping(target = "page", source = "productPage.number")
    ProductPage map(final Page<ProductFilterView> productPage);

    default ProductPrice map(final List<ProductPrice> prices, final CurrencyCode expectedCurrency) {
        return ProductPriceMapper.INSTANCE.getExpectedCurrencyOrOriginalPrice(prices, expectedCurrency);
    }

}
