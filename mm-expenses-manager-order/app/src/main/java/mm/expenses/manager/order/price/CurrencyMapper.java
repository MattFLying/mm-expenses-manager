package mm.expenses.manager.order.price;

import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.util.IdUtils;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.order.api.order.model.OrderedProductResponse;
import mm.expenses.manager.order.api.order.model.PriceResponse;
import mm.expenses.manager.order.processor.OrderedProduct;
import mm.expenses.manager.order.processor.OrderedProductPrice;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {StringUtils.class, Collectors.class, DateUtils.class, IdUtils.class}
)
public interface CurrencyMapper extends AbstractMapper {

    @Mapping(target = "currency", source = "price.currency.code")
    @Mapping(target = "value", source = "price.value")
    PriceResponse mapPrice(final Price price);

    @Mapping(target = "code", source = "price.currency.code")
    @Mapping(target = "value", expression = "java(mapToDouble(price.getValue()))")
    CurrencyConversionValueDto map(final Price price);

    @Mapping(target = "code", source = "toCode")
    CurrencyConversionValueDto mapTo(final CurrencyCode toCode);

    @Mapping(target = "id", expression = "java(orderedProduct.getId().toString())")
    @Mapping(target = "date", expression = "java(dateOfLastModifiedOrCreatedOrderedProduct(orderedProduct, price))")
    @Mapping(target = "to", expression = "java(mapTo(toCode))")
    @Mapping(target = "from", expression = "java(map(price))")
    CurrencyConversionRequest map(final OrderedProduct orderedProduct, final Price price, final CurrencyCode toCode);

    @Mapping(target = "id", expression = "java(orderedProduct.getId().toString())")
    @Mapping(target = "date", expression = "java(dateOfLastModifiedOrCreatedOrderedProduct(orderedProduct, price))")
    @Mapping(target = "to", expression = "java(mapTo(toCode))")
    @Mapping(target = "from", expression = "java(map(price))")
    CurrencyConversionRequest map(final OrderedProductResponse orderedProduct, final Price price, final CurrencyCode toCode);

    @Mapping(target = "currency", expression = "java(mapOriginalPriceCurrency(orderedProduct.getPrices()))")
    @Mapping(target = "value", expression = "java(mapOriginalPriceValue(orderedProduct.getPrices()))")
    @Mapping(target = "date", source = "orderedProduct.lastModifiedAt")
    Price mapTo(final OrderedProduct orderedProduct);

    @Mapping(target = "currency", expression = "java(mapOriginalPriceCurrencyFromResponse(orderedProduct.getPrice()))")
    @Mapping(target = "value", expression = "java(mapOriginalPriceValueFromResponse(orderedProduct.getPrice()))")
    @Mapping(target = "date", source = "orderedProduct.lastModifiedAt")
    Price mapTo(final OrderedProductResponse orderedProduct);

    default List<CurrencyConversionRequest> map(final OrderedProductResponse orderedProduct, final CurrencyCode toCode) {
        return List.of(map(orderedProduct, mapTo(orderedProduct), toCode));
    }

    default List<CurrencyConversionRequest> map(final OrderedProduct orderedProduct, final CurrencyCode toCode) {
        return List.of(map(orderedProduct, mapTo(orderedProduct), toCode));
    }

    default LocalDate dateOfLastModifiedOrCreatedOrderedProduct(final OrderedProduct orderedProduct) {
        val latestDate = Stream.of(orderedProduct.getCreatedAt(), orderedProduct.getLastModifiedAt())
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);

        return DateUtils.instantToLocalDate(latestDate);
    }

    default LocalDate dateOfLastModifiedOrCreatedOrderedProduct(final OrderedProductResponse orderedProduct) {
        val latestDate = Stream.of(orderedProduct.getCreatedAt(), orderedProduct.getLastModifiedAt())
                .filter(Objects::nonNull)
                .map(x -> DateUtils.localDateToInstant(DateUtils.fromStringToLocalDate(x)))
                .max(Instant::compareTo)
                .orElse(null);

        return DateUtils.instantToLocalDate(latestDate);
    }

    default LocalDate dateOfLastModifiedOrCreatedOrderedProduct(final OrderedProduct orderedProduct, final Price price) {
        if (Objects.nonNull(price) && Objects.nonNull(price.getDate())) {
            return DateUtils.instantToLocalDate(price.getDate());
        }
        return dateOfLastModifiedOrCreatedOrderedProduct(orderedProduct);
    }

    default LocalDate dateOfLastModifiedOrCreatedOrderedProduct(final OrderedProductResponse orderedProduct, final Price price) {
        if (Objects.nonNull(price) && Objects.nonNull(price.getDate())) {
            return DateUtils.instantToLocalDate(price.getDate());
        }
        return dateOfLastModifiedOrCreatedOrderedProduct(orderedProduct);
    }

    default BigDecimal map(final BigDecimal value) {
        return BigDecimalWrapper.of(value);
    }

    default Double mapToDouble(final BigDecimal value) {
        return BigDecimalWrapper.of(value).doubleValue();
    }

    default CurrencyCode mapOriginalPriceCurrency(final Collection<OrderedProductPrice> prices) {
        if (CollectionUtils.isNotEmpty(prices)) {
            return prices.stream()
                    .filter(OrderedProductPrice::isPriceOriginal)
                    .findAny()
                    .map(OrderedProductPrice::getCurrency)
                    .orElse(null);
        }
        return null;
    }

    default CurrencyCode mapOriginalPriceCurrencyFromResponse(final Collection<PriceResponse> prices) {
        if (CollectionUtils.isNotEmpty(prices)) {
            return prices.stream()
                    .filter(PriceResponse::getIsOriginal)
                    .findAny()
                    .map(priceResponse -> CurrencyCode.valueOf(priceResponse.getCurrency()))
                    .orElse(null);
        }
        return null;
    }

    default BigDecimal mapOriginalPriceValue(final Collection<OrderedProductPrice> prices) {
        if (CollectionUtils.isNotEmpty(prices)) {
            return prices.stream()
                    .filter(OrderedProductPrice::isPriceOriginal)
                    .findAny()
                    .map(OrderedProductPrice::getValue)
                    .orElse(null);
        }
        return null;
    }

    default BigDecimal mapOriginalPriceValueFromResponse(final Collection<PriceResponse> prices) {
        if (CollectionUtils.isNotEmpty(prices)) {
            return prices.stream()
                    .filter(PriceResponse::getIsOriginal)
                    .findAny()
                    .map(PriceResponse::getValue)
                    .orElse(null);
        }
        return null;
    }

}

