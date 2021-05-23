package mm.expenses.manager.product.config.converter;

import mm.expenses.manager.product.BaseInitTest;
import mm.expenses.manager.product.exception.ConversionException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BigDecimalToDecimal128ConverterTest extends BaseInitTest {

    private final BigDecimalToDecimal128Converter converter = new BigDecimalToDecimal128Converter();

    @Test
    void shouldConvertBigDecimalToDecimal128() {
        // given
        final var toConvert = BigDecimal.valueOf(5.7);
        final var expected = new Decimal128(toConvert);

        // when
        final var result = converter.convert(toConvert);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowConversionException_whenSomethingWrong() {
        assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(ConversionException.class)
                .hasMessage(ProductExceptionMessage.CANNOT_CONVERT_BIG_DECIMAL_TO_DECIMAL128.getMessage());
    }

}