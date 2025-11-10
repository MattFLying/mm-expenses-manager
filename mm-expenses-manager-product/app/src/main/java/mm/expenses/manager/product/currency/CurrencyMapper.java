package mm.expenses.manager.product.currency;

import lombok.val;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.util.IdUtils;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionValueDto;
import mm.expenses.manager.product.price.ProductPrice;
import mm.expenses.manager.product.product.Product;
import mm.expenses.manager.product.product.ProductFilterView;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {StringUtils.class, Collectors.class, DateUtils.class, IdUtils.class}
)
public interface CurrencyMapper extends AbstractMapper {

    @Mapping(target = "code", source = "product.priceCurrency.code")
    @Mapping(target = "value", source = "product.priceValue")
    CurrencyConversionValueDto map(final ProductFilterView product);

    @Mapping(target = "code", source = "priceOriginalOrAny.currency")
    @Mapping(target = "value", source = "priceOriginalOrAny.value")
    CurrencyConversionValueDto map(final Product product, final ProductPrice priceOriginalOrAny);

    @Mapping(target = "code", source = "toCode")
    CurrencyConversionValueDto mapTo(final CurrencyCode toCode);

    @Mapping(target = "id", expression = "java(product.getProductId().toString())")
    @Mapping(target = "date", expression = "java(dateOfLastModifiedOrCreatedProduct(product))")
    @Mapping(target = "to", expression = "java(mapTo(toCode))")
    @Mapping(target = "from", expression = "java(map(product))")
    CurrencyConversionRequest map(final ProductFilterView product, final CurrencyCode toCode);

    @Mapping(target = "id", expression = "java(product.getId().toString())")
    @Mapping(target = "date", expression = "java(dateOfLastModifiedOrCreatedProduct(priceOriginalOrAny.getDate()))")
    @Mapping(target = "to", expression = "java(mapTo(toCode))")
    @Mapping(target = "from", expression = "java(map(product, priceOriginalOrAny))")
    CurrencyConversionRequest map(final Product product, final CurrencyCode toCode, final ProductPrice priceOriginalOrAny);

    @Mapping(target = "id", source = "correlationId")
    @Mapping(target = "date", expression = "java(dateOfLastModifiedOrCreatedProduct(priceOriginalOrAny.getDate()))")
    @Mapping(target = "to", expression = "java(mapTo(toCode))")
    @Mapping(target = "from", expression = "java(map(product, priceOriginalOrAny))")
    CurrencyConversionRequest map(final Product product, final CurrencyCode toCode, final ProductPrice priceOriginalOrAny, final String correlationId);

    default LocalDate dateOfLastModifiedOrCreatedProduct(final ProductFilterView product) {
        val latestDate = Stream.of(product.getCreatedAt(), product.getLastModifiedAt())
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(null);

        return DateUtils.instantToLocalDate(latestDate);
    }

    default LocalDate dateOfLastModifiedOrCreatedProduct(final String date) {
        return DateUtils.fromStringToLocalDate(date);
    }

}

