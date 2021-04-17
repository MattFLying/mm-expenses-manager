package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.SneakyThrows;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.api.ApiFeignClientException;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.exception.ProviderException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.*;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyRateAssert.assertNbpCurrencyRate;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyRatesAssert.assertNbpCurrencyRates;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class NbpCurrencyProviderTest extends FinanceApplicationTest {

    @MockBean
    private NbpClient nbpClient;

    @MockBean
    private NbpApiConfig nbpApiConfig;

    @Autowired
    private NbpCurrencyProvider nbpCurrencyProvider;

    @SneakyThrows
    @Override
    protected void setupBeforeEachTest() {
        when(nbpApiConfig.getDataFormat()).thenReturn(DATA_FORMAT_JSON_NAME);
        when(nbpApiConfig.getName()).thenReturn(PROVIDER_NAME);
        when(nbpApiConfig.getCurrency()).thenReturn(DEFAULT_CURRENCY);

        when(nbpClient.getAvailableTableType()).thenReturn(TABLE_TYPE.name());
    }

    @Override
    protected void setupAfterEachTest() {
        reset(nbpClient);
        reset(nbpApiConfig);
    }

    @Test
    void shouldGetCorrectConfig() {
        // given && when
        final var config = nbpCurrencyProvider.getProviderConfig();

        // then
        assertThat(config).isNotNull().isInstanceOf(NbpApiConfig.class);
        assertThat(config.getDataFormat()).isEqualTo(DATA_FORMAT_JSON_NAME);
        assertThat(config.getName()).isEqualTo(PROVIDER_NAME);
        assertThat(config.getCurrency()).isEqualTo(DEFAULT_CURRENCY);
    }

    @Test
    void shouldGetHistoricCurrencies() {
        // given && when
        final var historicComponent = nbpCurrencyProvider.getHistoricCurrencies();

        // then
        assertThat(historicComponent).isNotNull().isInstanceOf(NbpHistoryUpdater.class);
    }

    @Nested
    class CurrentCurrencyRate {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldGetCurrentCurrencyRate(final CurrencyCode currency) throws ApiFeignClientException, CurrencyProviderException {
            // given
            final var date = LocalDate.now();
            final var rate = 4.3d;
            final var tableNumber = "test/table/number/00";

            final var rateDto = createRateDto(tableNumber, rate, date);
            final var exchangeRateDto = createExchangeRateDto(TABLE_TYPE, currency, rateDto);

            // when
            when(nbpClient.fetchCurrentExchangeRateForCurrencyFromTableType(TABLE_TYPE.name(), currency.getCode(), DATA_FORMAT_JSON_NAME)).thenReturn(Optional.of(exchangeRateDto));

            final var result = nbpCurrencyProvider.getCurrentCurrencyRate(currency);

            // then
            assertNbpCurrencyRate(result).isOfCurrency(currency).hasDate(date).hasRate(rate).hasDetails(createNbpDetails(TABLE_TYPE, tableNumber));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldThrowCurrencyProviderException_whenApiFeignExceptionIsThrown(final CurrencyCode currency) throws ApiFeignClientException {
            // given && when
            when(nbpClient.fetchCurrentExchangeRateForCurrencyFromTableType(TABLE_TYPE.name(), currency.getCode(), DATA_FORMAT_JSON_NAME)).thenThrow(ApiFeignClientException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrentCurrencyRate(currency))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Cannot fetch current currency rate for currency: %s. Client provider error.", currency));
        }

        @Test
        void shouldThrowCurrencyProviderException_whenOtherExceptionIsThrown() {
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrentCurrencyRate(null))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Something went wrong during fetch current currency rate for currency: %s.", null));
        }

    }

    @Nested
    class CurrencyRateForDate {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldGetCurrencyRateForDate(final CurrencyCode currency) throws ApiFeignClientException, CurrencyProviderException {
            // given
            final var date = LocalDate.now().minusDays(5);
            final var rate = 5.8d;
            final var tableNumber = "test/table/number/01";

            final var rateDto = createRateDto(tableNumber, rate, date);
            final var exchangeRateDto = createExchangeRateDto(TABLE_TYPE, currency, rateDto);

            // when
            when(nbpClient.fetchExchangeRateForCurrencyFromTableTypeAndDate(TABLE_TYPE.name(), currency.getCode(), date, DATA_FORMAT_JSON_NAME)).thenReturn(Optional.of(exchangeRateDto));

            final var result = nbpCurrencyProvider.getCurrencyRateForDate(currency, date);

            // then
            assertNbpCurrencyRate(result).isOfCurrency(currency).hasDate(date).hasRate(rate).hasDetails(createNbpDetails(TABLE_TYPE, tableNumber));
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldThrowCurrencyProviderException_whenApiFeignExceptionIsThrown(final CurrencyCode currency) throws ApiFeignClientException {
            // given && when
            final var date = LocalDate.now().minusDays(5);
            when(nbpClient.fetchExchangeRateForCurrencyFromTableTypeAndDate(TABLE_TYPE.name(), currency.getCode(), date, DATA_FORMAT_JSON_NAME)).thenThrow(ApiFeignClientException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRateForDate(currency, date))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Cannot fetch currency rate for currency: %s and date: %s. Client provider error.", currency, date));
        }

        @Test
        void shouldThrowCurrencyProviderException_whenOtherExceptionIsThrown() {
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRateForDate(null, null))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Something went wrong during fetch currency rate for currency: %s and date: %s.", null, null));
        }

    }

    @Nested
    class CurrencyRateForDateRange {

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldGetCurrencyRateForDateRange(final CurrencyCode currency) throws ApiFeignClientException, CurrencyProviderException {
            // given
            final var dateTo = LocalDate.now();
            final var dateFrom = LocalDate.now().minusDays(10);

            final var date_1 = LocalDate.now().minusDays(9);
            final var date_2 = LocalDate.now().minusDays(3);

            final var rate_1 = 5.8d;
            final var rate_2 = 3.4d;

            final var tableNumber_1 = "test/table/number/02";
            final var tableNumber_2 = "test/table/number/03";

            final var rateDto_1 = createRateDto(tableNumber_1, rate_1, date_1);
            final var rateDto_2 = createRateDto(tableNumber_2, rate_2, date_2);
            final var exchangeRateDto = createExchangeRateDto(TABLE_TYPE, currency, rateDto_1, rateDto_2);

            final var expected = List.of(
                    createNbpCurrencyRate(currency, date_1, rate_1, TABLE_TYPE, tableNumber_1),
                    createNbpCurrencyRate(currency, date_2, rate_2, TABLE_TYPE, tableNumber_2)
            );

            // when
            when(nbpClient.fetchExchangeRateForCurrencyFromTableTypeAndDateRange(TABLE_TYPE.name(), currency.getCode(), dateFrom, dateTo, DATA_FORMAT_JSON_NAME)).thenReturn(Optional.of(exchangeRateDto));

            final var result = nbpCurrencyProvider.getCurrencyRateForDateRange(currency, dateFrom, dateTo);

            // then
            assertNbpCurrencyRates(result).areOfCurrency(currency).containsExactlyTheSameObjectsAs(expected).hasDetails();
        }

        @ParameterizedTest
        @ArgumentsSource(CurrencyCodeArgument.class)
        void shouldThrowCurrencyProviderException_whenApiFeignExceptionIsThrown(final CurrencyCode currency) throws ApiFeignClientException {
            // given && when
            final var dateTo = LocalDate.now();
            final var dateFrom = LocalDate.now().minusDays(3);
            when(nbpClient.fetchExchangeRateForCurrencyFromTableTypeAndDateRange(TABLE_TYPE.name(), currency.getCode(), dateFrom, dateTo, DATA_FORMAT_JSON_NAME)).thenThrow(ApiFeignClientException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRateForDateRange(currency, dateFrom, dateTo))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Cannot fetch currency rate for currency: %s and date between: %s - %s. Client provider error.", currency, dateFrom, dateTo));
        }

        @Test
        void shouldThrowCurrencyProviderException_whenOtherExceptionIsThrown() {
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRateForDateRange(null, null, null))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Something went wrong during fetch currency rate for currency: %s and date between: %s - %s.", null, null, null));
        }

    }

    @Nested
    class CurrentCurrencyRates {

        @Test
        void shouldGetCurrentCurrencyRates() throws ApiFeignClientException, CurrencyProviderException {
            // given
            final var date = LocalDate.now();
            final var tableNumber = "test/table/number/06";

            final var rate_1 = 1.3d;
            final var rate_2 = 2.5d;

            final var tableRateDto_1 = createTableRateDto(CurrencyCode.EUR, rate_1);
            final var tableRateDto_2 = createTableRateDto(CurrencyCode.GBP, rate_2);
            final var tableExchangeRatesDto = createTableExchangeRatesDto(TABLE_TYPE, tableNumber, date, tableRateDto_1, tableRateDto_2);

            final var expected = List.of(
                    createNbpCurrencyRate(CurrencyCode.EUR, date, rate_1, TABLE_TYPE, tableNumber),
                    createNbpCurrencyRate(CurrencyCode.GBP, date, rate_2, TABLE_TYPE, tableNumber)
            );

            // when
            when(nbpClient.fetchCurrentAllExchangeRatesForTableType(TABLE_TYPE.name(), DATA_FORMAT_JSON_NAME)).thenReturn(List.of(tableExchangeRatesDto));

            final var result = nbpCurrencyProvider.getCurrentCurrencyRates();

            // then
            assertNbpCurrencyRates(result).containsExactlyTheSameObjectsAs(expected).hasDetails();
        }

        @Test
        void shouldThrowCurrencyProviderException_whenApiFeignExceptionIsThrown() throws ApiFeignClientException {
            // given && when
            when(nbpClient.fetchCurrentAllExchangeRatesForTableType(TABLE_TYPE.name(), DATA_FORMAT_JSON_NAME)).thenThrow(ApiFeignClientException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrentCurrencyRates())
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage("Cannot fetch current currency rates. Client provider error.");
        }

        @Test
        void shouldThrowCurrencyProviderException_whenOtherExceptionIsThrown() throws ProviderException {
            // given && when
            when(nbpClient.getAvailableTableType()).thenThrow(ProviderException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrentCurrencyRates())
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage("Something went wrong during fetch current currency rates.");
        }

    }

    @Nested
    class CurrencyRatesForDate {

        @Test
        void shouldGetCurrencyRatesForDate() throws ApiFeignClientException, CurrencyProviderException {
            // given
            final var date = LocalDate.now().minusDays(1);
            final var tableNumber = "test/table/number/07";

            final var rate_1 = 0.8d;
            final var rate_2 = 1.4d;
            final var rate_3 = 2.2d;

            final var tableRateDto_1 = createTableRateDto(CurrencyCode.AUD, rate_1);
            final var tableRateDto_2 = createTableRateDto(CurrencyCode.CAD, rate_2);
            final var tableRateDto_3 = createTableRateDto(CurrencyCode.UNDEFINED, rate_2);
            final var tableExchangeRatesDto = createTableExchangeRatesDto(TABLE_TYPE, tableNumber, date, tableRateDto_1, tableRateDto_2, tableRateDto_3);

            final var expected = List.of(
                    createNbpCurrencyRate(CurrencyCode.AUD, date, rate_1, TABLE_TYPE, tableNumber),
                    createNbpCurrencyRate(CurrencyCode.CAD, date, rate_2, TABLE_TYPE, tableNumber)
            );
            final var notExpected = createNbpCurrencyRate(CurrencyCode.UNDEFINED, date, rate_3, TABLE_TYPE, tableNumber);

            // when
            when(nbpClient.fetchAllExchangeRatesForTableTypeAndDate(TABLE_TYPE.name(), date, DATA_FORMAT_JSON_NAME)).thenReturn(List.of(tableExchangeRatesDto));

            final var result = nbpCurrencyProvider.getCurrencyRatesForDate(date);

            // then
            assertNbpCurrencyRates(result).containsExactlyTheSameObjectsAs(expected).doesNotContainAnyOf(notExpected).hasDetails();
        }

        @Test
        void shouldThrowCurrencyProviderException_whenApiFeignExceptionIsThrown() throws ApiFeignClientException {
            // given && when
            final var date = LocalDate.now().minusDays(1);
            when(nbpClient.fetchAllExchangeRatesForTableTypeAndDate(TABLE_TYPE.name(), date, DATA_FORMAT_JSON_NAME)).thenThrow(ApiFeignClientException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRatesForDate(date))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Cannot fetch currency rates for date: %s. Client provider error.", date));
        }

        @Test
        void shouldThrowCurrencyProviderException_whenOtherExceptionIsThrown() throws ProviderException {
            // given && when
            final var date = LocalDate.now().minusDays(1);
            when(nbpClient.getAvailableTableType()).thenThrow(ProviderException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRatesForDate(date))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Something went wrong during fetch currency rates for date: %s.", date));
        }

    }

    @Nested
    class CurrencyRatesForDateRange {

        @Test
        void shouldGetCurrencyRatesForDateRange() throws ApiFeignClientException, CurrencyProviderException {
            // given
            final var dateTo = LocalDate.now();
            final var dateFrom = LocalDate.now().minusDays(15);

            final var date_1 = LocalDate.now().minusDays(11);
            final var date_2 = LocalDate.now().minusDays(2);

            final var tableNumber = "test/table/number/08";

            final var rate_1 = 3.6d;
            final var rate_2 = 9.5d;
            final var rate_3 = 2.4d;

            final var tableRateDto_1 = createTableRateDto(CurrencyCode.CHF, rate_1);
            final var tableRateDto_2 = createTableRateDto(CurrencyCode.JPY, rate_2);
            final var tableRateDto_3 = createTableRateDto(CurrencyCode.NZD, rate_3);
            final var tableExchangeRatesDto_1 = createTableExchangeRatesDto(TABLE_TYPE, tableNumber, date_1, tableRateDto_1);
            final var tableExchangeRatesDto_2 = createTableExchangeRatesDto(TABLE_TYPE, tableNumber, date_2, tableRateDto_2, tableRateDto_3);

            final var expected = List.of(
                    createNbpCurrencyRate(CurrencyCode.CHF, date_1, rate_1, TABLE_TYPE, tableNumber),
                    createNbpCurrencyRate(CurrencyCode.JPY, date_2, rate_2, TABLE_TYPE, tableNumber),
                    createNbpCurrencyRate(CurrencyCode.NZD, date_2, rate_3, TABLE_TYPE, tableNumber)
            );

            // when
            when(nbpClient.fetchAllExchangeRatesForTableTypeAndDateRange(TABLE_TYPE.name(), dateFrom, dateTo, DATA_FORMAT_JSON_NAME)).thenReturn(List.of(tableExchangeRatesDto_1, tableExchangeRatesDto_2));

            final var result = nbpCurrencyProvider.getCurrencyRatesForDateRange(dateFrom, dateTo);

            // then
            assertNbpCurrencyRates(result).containsExactlyTheSameObjectsAs(expected).hasDetails();
        }

        @Test
        void shouldThrowCurrencyProviderException_whenApiFeignExceptionIsThrown() throws ApiFeignClientException {
            // given && when
            final var dateTo = LocalDate.now();
            final var dateFrom = LocalDate.now().minusDays(1);
            when(nbpClient.fetchAllExchangeRatesForTableTypeAndDateRange(TABLE_TYPE.name(), dateFrom, dateTo, DATA_FORMAT_JSON_NAME)).thenThrow(ApiFeignClientException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRatesForDateRange(dateFrom, dateTo))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Cannot fetch currency rates for date range between: %s - %s. Client provider error.", dateFrom, dateTo));
        }

        @Test
        void shouldThrowCurrencyProviderException_whenOtherExceptionIsThrown() throws ProviderException {
            // given && when
            final var dateTo = LocalDate.now();
            final var dateFrom = LocalDate.now().minusDays(1);
            when(nbpClient.getAvailableTableType()).thenThrow(ProviderException.class);

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRatesForDateRange(dateFrom, dateTo))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(format("Something went wrong during fetch currency rates for date range between: %s - %s.", dateFrom, dateTo));
        }

    }

}