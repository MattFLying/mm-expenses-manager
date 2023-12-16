package mm.expenses.manager.common.beans.converter;

import mm.expenses.manager.common.beans.exception.BeansExceptionMessage;
import mm.expenses.manager.common.beans.exception.ConversionException;
import org.assertj.core.api.Assertions;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BigDecimalToDecimal128ConverterTest {

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
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(ConversionException.class)
                .hasMessage(BeansExceptionMessage.CANNOT_CONVERT_BIG_DECIMAL_TO_DECIMAL128.getMessage());
    }

}