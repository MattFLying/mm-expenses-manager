package mm.expenses.manager.common.beans.converter;

import mm.expenses.manager.common.beans.exception.BeansExceptionMessage;
import mm.expenses.manager.common.beans.exception.ConversionException;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Decimal128ToBigDecimalConverterTest {

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
        assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(ConversionException.class)
                .hasMessage(BeansExceptionMessage.CANNOT_CONVERT_BIG_DECIMAL_TO_DECIMAL128.getMessage());
    }

}