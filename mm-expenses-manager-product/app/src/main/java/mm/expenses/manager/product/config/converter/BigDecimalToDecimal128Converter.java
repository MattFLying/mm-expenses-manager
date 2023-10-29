package mm.expenses.manager.product.config.converter;

import mm.expenses.manager.product.exception.ConversionException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.bson.types.Decimal128;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.math.BigDecimal;

/**
 * BigDecimal converter to Decimal128 - representation of BigDecimal in MongoDB.
 */
@WritingConverter
public class BigDecimalToDecimal128Converter implements Converter<BigDecimal, Decimal128> {

    @Override
    public Decimal128 convert(final BigDecimal source) {
        try {
            return new Decimal128(source);
        } catch (final IllegalArgumentException | NullPointerException exception) {
            throw new ConversionException(ProductExceptionMessage.CANNOT_CONVERT_BIG_DECIMAL_TO_DECIMAL128, exception);
        }
    }

}
