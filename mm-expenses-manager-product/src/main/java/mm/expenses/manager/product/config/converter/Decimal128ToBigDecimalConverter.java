package mm.expenses.manager.product.config.converter;

import mm.expenses.manager.product.exception.ConversionException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.bson.types.Decimal128;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.math.BigDecimal;

/**
 * Convert representation of BigDecimal in MongoDB - Decimal128 into BigDecimal.
 */
@ReadingConverter
public class Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {

    @Override
    public BigDecimal convert(final Decimal128 source) {
        try {
            return source.bigDecimalValue();
        } catch (final IllegalArgumentException | NullPointerException exception) {
            throw new ConversionException(ProductExceptionMessage.CANNOT_CONVERT_DECIMAL128_TO_BIG_DECIMAL, exception);
        }
    }

}
