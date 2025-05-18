package mm.expenses.manager.product.price;

import lombok.val;
import mm.expenses.manager.common.kafka.message.PriceMessage;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.product.api.product.model.*;
import mm.expenses.manager.product.product.ProductFilterView;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {CurrencyCode.class}
)
public interface ProductPriceMapper extends AbstractMapper {

    ProductPriceMapper INSTANCE = Mappers.getMapper(ProductPriceMapper.class);

    @Mapping(target = "currency", expression = "java(CurrencyCode.getCurrencyFromString(createPriceRequest.getCurrency()))")
    Price map(final CreatePriceRequest createPriceRequest);

    @Mapping(target = "value", expression = "java(createProductRequest.getPrice().getValue())")
    @Mapping(target = "currency", expression = "java(CurrencyCode.getCurrencyFromString(createProductRequest.getPrice().getCurrency()))")
    @Mapping(target = "isOriginal", expression = "java(true)")
    ProductPrice map(final CreateProductRequest createProductRequest);

    @Mapping(target = "value", expression = "java(createProductRequest.getPrice().getValue())")
    @Mapping(target = "currency", expression = "java(CurrencyCode.getCurrencyFromString(createProductRequest.getPrice().getCurrency()))")
    @Mapping(target = "isOriginal", expression = "java(true)")
    @Mapping(target = "date", source = "date")
    ProductPrice map(final CreateProductRequest createProductRequest, final String date);

    @Mapping(target = "value", source = "product.priceValue")
    @Mapping(target = "currency", source = "product.priceCurrency")
    @Mapping(target = "isOriginal", expression = "java(product.isPriceOriginal())")
    PriceResponse mapToPrice(final ProductFilterView product);

    PriceMessage mapToPriceMessage(final ProductPrice price);

    @Mapping(target = "isOriginal", expression = "java(price.isOriginal())")
    PriceResponse map(final ProductPrice price);

    default PriceMessage mapOriginalToMessage(final List<ProductPrice> prices) {
        return mapToPriceMessage(prices.stream().filter(ProductPrice::isOriginal).findAny().orElse(null));
    }

    default ProductPrice getExpectedCurrencyOrOriginalPrice(final List<ProductPrice> productPrices, final CurrencyCode expectedCurrency) {
        val expectedPrice = productPrices.stream()
                .filter(productPrice -> Objects.equals(expectedCurrency, productPrice.getCurrency()))
                .findAny();

        return expectedPrice.orElseGet(
                () -> productPrices.stream()
                        .filter(ProductPrice::isOriginal)
                        .findAny()
                        .orElseGet(() -> null)
        );
    }

}
