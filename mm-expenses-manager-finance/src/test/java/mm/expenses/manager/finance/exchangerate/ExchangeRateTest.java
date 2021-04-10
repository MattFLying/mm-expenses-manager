package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.BaseInitTest;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

import static mm.expenses.manager.finance.exchangerate.ExchangeRateHelper.*;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;
import static org.assertj.core.api.Assertions.assertThat;

class ExchangeRateTest extends BaseInitTest {

    @Test
    void twoDifferentExchangeRatesForTheSameCurrencyAndDateShouldNotBeEqual() {
        // given
        final var currency = CurrencyCode.EUR;
        final var date = LocalDate.now();

        // when
        final var exchangeRate_1 = createNewExchangeRate(currency, date);
        final var rate_1 = exchangeRate_1.getRateByProvider(PROVIDER_NAME);

        final var exchangeRate_2 = createNewExchangeRate(currency, date);
        final var rate_2 = exchangeRate_2.getRateByProvider(PROVIDER_NAME);

        // then
        assertThat(exchangeRate_1).isNotEqualTo(exchangeRate_2);
        assertThat(exchangeRate_1.getRatesByProvider()).isNotEqualTo(exchangeRate_2.getRatesByProvider());
        assertThat(rate_1).isNotEqualTo(rate_2);
        assertThat(rate_1.getTo()).isNotEqualTo(rate_2.getTo());

        // should be equal
        assertThat(exchangeRate_1.getDetailsByProvider()).isEqualTo(exchangeRate_2.getDetailsByProvider());
        assertThat(rate_1.getFrom()).isEqualTo(rate_2.getFrom());
    }

    @Test
    void shouldCreateNewRatesMapWhenAddNewRateAndMapIsNull() {
        // given
        final var currency = CurrencyCode.CAD;
        final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
        final var createdModifiedAt = Instant.now();

        final var exchangeRate = new ExchangeRate(ID, currency, date, createdModifiedAt, createdModifiedAt, INITIAL_VERSION);
        final var rate = createNewRandomRateToPLN(CurrencyCode.JPY);

        // when
        exchangeRate.addRateForProvider(PROVIDER_NAME, rate);

        // then
        assertThat(exchangeRate.getRatesByProvider()).isNotNull().isNotEmpty();
        assertThat(exchangeRate.getRateByProvider(PROVIDER_NAME)).isNotNull().isEqualTo(rate);
        assertThat(exchangeRate.getRateByProvider(PROVIDER_NAME, true)).isNotNull().isEqualTo(rate);
    }

    @Test
    void shouldRetrieveAlternativeRateWhenForGivenProviderDoesNotExists() {
        // given
        final var providerName = "test-provider";
        final var currency = CurrencyCode.GBP;
        final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
        final var createdModifiedAt = Instant.now();

        final var exchangeRate = new ExchangeRate(ID, currency, date, createdModifiedAt, createdModifiedAt, INITIAL_VERSION);
        final var rate = createNewRandomRateToPLN(CurrencyCode.CHF);
        exchangeRate.addRateForProvider(providerName, rate);

        // when
        final var result = exchangeRate.getRateByProvider(PROVIDER_NAME, true);

        // then
        assertThat(exchangeRate.getRatesByProvider()).isNotNull().isNotEmpty();
        assertThat(exchangeRate.getRateByProvider(PROVIDER_NAME)).isNotNull().isEqualTo(ExchangeRate.Rate.empty());
        assertThat(exchangeRate.getRateByProvider(providerName)).isNotNull().isEqualTo(rate);
    }

    @Test
    void shouldReturnEmptyRateIfDoesNotExistsForProviderAndIsNotRequiredToReturnAnyInThisCase() {
        // given
        final var currency = CurrencyCode.CAD;
        final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(3));
        final var createdModifiedAt = Instant.now();

        final var exchangeRate = new ExchangeRate(ID, currency, date, createdModifiedAt, createdModifiedAt, INITIAL_VERSION);

        // when
        exchangeRate.getRateByProvider(PROVIDER_NAME, true);

        // then
        assertThat(exchangeRate.getRatesByProvider()).isNotNull().isEmpty();
    }

    @Test
    void shouldCreateNewDetailsMapWhenAddNewDetailsAndMapIsNull() {
        // given
        final var currency = CurrencyCode.CHF;
        final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(2));
        final var createdModifiedAt = Instant.now();

        final var exchangeRate = new ExchangeRate(ID, currency, date, createdModifiedAt, createdModifiedAt, INITIAL_VERSION);

        // when
        exchangeRate.addDetailsForProvider(PROVIDER_NAME, Map.of());

        // then
        assertThat(exchangeRate.getDetailsByProvider()).isNotNull().isNotEmpty();
    }

    @Test
    void shouldCreateEmptyMapsForDetailsAndRatesWhenAreNullDuringCheckingProvider() {
        // given
        final var currency = CurrencyCode.CHF;
        final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(2));
        final var createdModifiedAt = Instant.now();

        final var exchangeRate = new ExchangeRate(ID, currency, date, createdModifiedAt, createdModifiedAt, INITIAL_VERSION);

        // when
        final var result = exchangeRate.hasProvider(PROVIDER_NAME);

        // then
        assertThat(exchangeRate.getDetailsByProvider()).isNotNull().isEmpty();
        assertThat(exchangeRate.getRatesByProvider()).isNotNull().isEmpty();
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnThereIsNoProviderIfThereIsOnlyRatesForThisProvider() {
        // given
        final var currency = CurrencyCode.CHF;
        final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(2));
        final var createdModifiedAt = Instant.now();

        final var exchangeRate = new ExchangeRate(ID, currency, date, createdModifiedAt, createdModifiedAt, INITIAL_VERSION);
        exchangeRate.addRateForProvider(PROVIDER_NAME, ExchangeRate.Rate.empty());

        // when
        final var result = exchangeRate.hasProvider(PROVIDER_NAME);

        // then
        assertThat(exchangeRate.getDetailsByProvider()).isNotNull().isEmpty();
        assertThat(exchangeRate.getRatesByProvider()).isNotNull().isNotEmpty();
        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnThereIsNoProviderIfThereIsOnlyDetailsForThisProvider() {
        // given
        final var currency = CurrencyCode.CHF;
        final var date = DateUtils.localDateToInstant(LocalDate.now().minusDays(2));
        final var createdModifiedAt = Instant.now();

        final var exchangeRate = new ExchangeRate(ID, currency, date, createdModifiedAt, createdModifiedAt, INITIAL_VERSION);
        exchangeRate.addDetailsForProvider(PROVIDER_NAME, Map.of());

        // when
        final var result = exchangeRate.hasProvider(PROVIDER_NAME);

        // then
        assertThat(exchangeRate.getDetailsByProvider()).isNotNull().isNotEmpty();
        assertThat(exchangeRate.getRatesByProvider()).isNotNull().isEmpty();
        assertThat(result).isFalse();
    }

    @Test
    void shouldVerifyEqualsForExchangeRate() {
        EqualsVerifier.simple()
                .forClass(ExchangeRate.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    void shouldVerifyEqualsForRate() {
        EqualsVerifier.simple()
                .forClass(ExchangeRate.Rate.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

    @Test
    void shouldVerifyEqualsForCurrencyValue() {
        EqualsVerifier.simple()
                .forClass(ExchangeRate.CurrencyValue.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}