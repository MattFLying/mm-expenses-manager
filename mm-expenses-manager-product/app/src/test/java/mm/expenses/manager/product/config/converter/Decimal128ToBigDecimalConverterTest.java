package mm.expenses.manager.product.config.converter;

import mm.expenses.manager.product.BaseInitTest;
import mm.expenses.manager.product.exception.ConversionException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.assertj.core.api.Assertions;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Decimal128ToBigDecimalConverterTest extends BaseInitTest {

    private final Decimal128ToBigDecimalConverter converter = new Decimal128ToBigDecimalConverter();

    @Test
    void shouldConvertDecimal128ToBigDecimal() {
        // given
        final var bigDecimal = BigDecimal.valueOf(5.7);
        final var toConvert = new Decimal128(bigDecimal);

        // when
        final var result = converter.convert(toConvert);

        // then
        assertThat(result).isEqualTo(bigDecimal);
    }

    @Test
    void shouldThrowConversionException_whenSomethingWrong() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(ConversionException.class)
                .hasMessage(ProductExceptionMessage.CANNOT_CONVERT_BIG_DECIMAL_TO_DECIMAL128.getMessage());
    }
}