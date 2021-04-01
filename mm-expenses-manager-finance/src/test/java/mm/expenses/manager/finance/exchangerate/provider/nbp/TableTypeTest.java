package mm.expenses.manager.finance.exchangerate.provider.nbp;

import mm.expenses.manager.common.i18n.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TableTypeTest {

    @Test
    void shouldReturnUnknownTableType_whenUndefinedWasPassed() {
        // given
        final var currency = CurrencyCode.UNDEFINED;

        // when
        final var result = TableType.findTableForCurrency(currency);

        // then
        assertThat(result).isEqualTo(TableType.UNKNOWN);
    }

    @Test
    void shouldReturnUnknownTableType_whenNullWasPassed() {
        // given && when
        final var result = TableType.findTableForCurrency(null);

        // then
        assertThat(result).isEqualTo(TableType.UNKNOWN);
    }

    @Test
    void shouldParseTableTypeToUnknown_whenNullWasPassed() {
        // given && when
        final var result = TableType.parse(null);

        // then
        assertThat(result).isEqualTo(TableType.UNKNOWN);
    }

    @ParameterizedTest
    @ArgumentsSource(TableTypeArgument.class)
    void shouldParseTableTypeToUnknown_whenUnknownTypesWerePassed(final Object object) {
        // given && when
        final var result = TableType.parse(object);

        // then
        assertThat(result).isEqualTo(TableType.UNKNOWN);
    }

    private static class TableTypeArgument implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(final ExtensionContext context) {
            return Stream.of(
                    Arguments.of(5),
                    Arguments.of(5.5),
                    Arguments.of("test"),
                    Arguments.of(new Object())
            );
        }
    }

}