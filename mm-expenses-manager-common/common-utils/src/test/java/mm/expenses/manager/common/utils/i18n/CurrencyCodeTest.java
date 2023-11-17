package mm.expenses.manager.common.utils.i18n;

import mm.expenses.manager.common.utils.BaseInitTest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class CurrencyCodeTest extends BaseInitTest {

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void getCode_test(final CurrencyCode currency) throws Exception {
        final var code = currency.getCode();

        assertThat(code).isEqualTo(currency.name());
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void getCurrencyFromString_doNotIgnoreCase_test(final CurrencyCode currency) throws Exception {
        var expectedCurrency = CurrencyCode.getCurrencyFromString(currency.toString(), false);

        assertThat(expectedCurrency).isEqualTo(currency);
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void getCurrencyFromString_ignoreCase_test(final CurrencyCode currency) throws Exception {
        var expectedCurrency = CurrencyCode.getCurrencyFromString(currency.toString(), true);

        assertThat(expectedCurrency).isEqualTo(currency);
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void getCurrencyFromString_ignoreCaseAsDefault_test(final CurrencyCode currency) throws Exception {
        var expectedCurrency = CurrencyCode.getCurrencyFromString(currency.toString());

        assertThat(expectedCurrency).isEqualTo(currency);
    }

    @Test
    void getCurrencyFromString_unknownEnumAsUndefined_test() throws Exception {
        var expectedCurrency = CurrencyCode.getCurrencyFromString("TEST");

        assertThat(expectedCurrency).isEqualTo(CurrencyCode.UNDEFINED);
    }

    @Test
    void getCurrencyFromString_ignoreCaseUnknownEnumAsUndefined_test() throws Exception {
        var expectedCurrency = CurrencyCode.getCurrencyFromString("test");

        assertThat(expectedCurrency).isEqualTo(CurrencyCode.UNDEFINED);
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void getCurrencyFromStringOrNull_test(final CurrencyCode currency) throws Exception {
        var expectedCurrency = CurrencyCode.getCurrencyFromStringOrNull(currency.toString());

        assertThat(expectedCurrency).isEqualTo(currency);
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void getCurrencyFromStringOrNull_valueIsNotNullForNotNullValue_test(final CurrencyCode currency) throws Exception {
        var expectedCurrency = CurrencyCode.getCurrencyFromStringOrNull(currency.toString());

        assertThat(expectedCurrency).isEqualTo(currency);
    }

    @Test
    void getCurrencyFromStringOrNull_valueIsNullForNullValue_test() throws Exception {
        var value = CurrencyCode.getCurrencyFromStringOrNull(null);

        assertThat(value).isNull();
    }

    @Test
    void getCurrencyFromStringOrNull_valueIsNullForEmptyString_test() throws Exception {
        var value = CurrencyCode.getCurrencyFromStringOrNull(StringUtils.EMPTY);

        assertThat(value).isNull();
    }

    @Test
    void getCurrencyFromString_ignoreCaseUndefined_test() throws Exception {
        var value = "TEST";

        var result = CurrencyCode.getCurrencyFromString(value);

        assertThat(result).isEqualTo(CurrencyCode.UNDEFINED);
    }

    @Test
    void getCurrencyFromString_undefined_test() throws Exception {
        var value = "test";

        var result = CurrencyCode.getCurrencyFromString(value);

        assertThat(result).isEqualTo(CurrencyCode.UNDEFINED);
    }

    @Test
    void getCurrencyFromString_doNotIgnoreCaseUndefined_test() throws Exception {
        var value = "test";

        var result = CurrencyCode.getCurrencyFromString(value, false);

        assertThat(result).isEqualTo(CurrencyCode.UNDEFINED);
    }

    @Test
    void of_isNullSoUndefined_test() throws Exception {
        var result = CurrencyCode.of(null);

        assertThat(result).isEqualTo(CurrencyCode.UNDEFINED);
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void of_isNullSoUndefined_test(final CurrencyCode currency) throws Exception {
        var result = CurrencyCode.of(currency);

        assertThat(result).isEqualTo(currency);
    }

    @ParameterizedTest
    @ArgumentsSource(CurrencyCodeArgument.class)
    void exists_test(final CurrencyCode currency) throws Exception {
        var result = CurrencyCode.exists(currency.name());

        assertThat(result).isTrue();
    }

    @Test
    void exists_doesNotExists_test() throws Exception {
        var result = CurrencyCode.exists("TEST");

        assertThat(result).isFalse();
    }

    @Test
    void exists_doesNotExistsForEmptyString_test() throws Exception {
        var result = CurrencyCode.exists(StringUtils.EMPTY);

        assertThat(result).isFalse();
    }

    @Test
    void exists_doesNotExistsForNullValue_test() throws Exception {
        var result = CurrencyCode.exists(null);

        assertThat(result).isFalse();
    }

}