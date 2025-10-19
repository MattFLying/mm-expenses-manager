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
import mm.expenses.manager.order.order.OrderedProduct;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {StringUtils.class, Collectors.class, DateUtils.class, IdUtils.class}
)
public interface CurrencyMapper extends AbstractMapper {

    @Mapping(target = "code", source = "price.currency.code")
    @Mapping(target = "value", expression = "java(mapToDouble(price.getValue()))")
    CurrencyConversionValueDto map(final Price price);

    @Mapping(target = "code", source = "toCode")
    CurrencyConversionValueDto mapTo(final CurrencyCode toCode);

    @Mapping(target = "id", expression = "java(orderedProduct.getId().toString())")
    @Mapping(target = "date", expression = "java(dateOfLastModifiedOrCreatedOrderedProduct(orderedProduct))")
    @Mapping(target = "to", expression = "java(mapTo(toCode))")
    @Mapping(target = "from", expression = "java(map(price))")
    CurrencyConversionRequest map(final OrderedProduct orderedProduct, final Price price, final CurrencyCode toCode);

    @Mapping(target = "currency", source = "orderedProduct.currency")
    @Mapping(target = "value", expression = "java(map(orderedProduct.getValue()))")
    @Mapping(target = "date", source = "orderedProduct.lastModifiedAt")
    Price mapTo(final OrderedProduct orderedProduct);

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

    default BigDecimal map(final BigDecimal value) {
        return BigDecimalWrapper.of(value);
    }

    default Double mapToDouble(final BigDecimal value) {
        return BigDecimalWrapper.of(value).doubleValue();
    }

}

