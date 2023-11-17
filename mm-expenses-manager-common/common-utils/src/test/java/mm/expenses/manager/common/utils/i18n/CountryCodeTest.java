package mm.expenses.manager.common.utils.i18n;

import mm.expenses.manager.common.utils.BaseInitTest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class CountryCodeTest extends BaseInitTest {

    @ParameterizedTest
    @ArgumentsSource(CountryCodeArgument.class)
    void getCode_test(final CountryCode country) throws Exception {
        final var code = country.getCode();

        assertThat(code).isEqualTo(country.name());
    }

    @ParameterizedTest
    @ArgumentsSource(CountryCodeArgument.class)
    void shouldReturnCorrectValuesForCountry_test(final CountryCode country) throws Exception {
        final var name = country.getName();
        final var code = country.getCode();
        final var threeDigitsAbbreviation = country.getThreeDigitsAbbreviation();
        final var isoCode = country.getIsoCode();

        assertThat(name).isEqualTo(country.getName());
        assertThat(code).isEqualTo(country.name());
        assertThat(isoCode).isEqualTo(country.getIsoCode());

        if (country == CountryCode.UNDEFINED) {
            assertThat(threeDigitsAbbreviation).isEqualTo(country.getThreeDigitsAbbreviation()).hasSize(0);
        } else {
            assertThat(threeDigitsAbbreviation).isEqualTo(country.getThreeDigitsAbbreviation()).hasSize(3);
        }
    }

}