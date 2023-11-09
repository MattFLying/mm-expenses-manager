package mm.expenses.manager.finance.exchangerate.provider.nbp;

import lombok.SneakyThrows;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.exception.api.feign.ApiFeignClientException;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;

import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.TABLE_TYPE;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.createNbpCurrencyRate;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.createTableExchangeRatesDto;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.createTableRateDto;
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
    }

    @Test
    void shouldGetHistoricCurrencies() {
        // given && when
        final var historicComponent = nbpCurrencyProvider.getHistoricCurrencies();

        // then
        assertThat(historicComponent).isNotNull().isInstanceOf(NbpHistoryUpdater.class);
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
                    .hasMessage(FinanceExceptionMessage.CURRENCY_PROVIDER_FEIGN_ALL_CURRENCIES.getMessage());
        }

        @Test
        void shouldThrowCurrencyProviderException_whenOtherExceptionIsThrown() throws CurrencyProviderException {
            // given && when
            when(nbpClient.getAvailableTableType()).thenThrow(new CurrencyProviderException(FinanceExceptionMessage.CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES));

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrentCurrencyRates())
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(FinanceExceptionMessage.CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES.getMessage());
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
                    .hasMessage(FinanceExceptionMessage.CURRENCY_PROVIDER_FEIGN_ALL_CURRENCIES_AND_DATE_RANGE.withParameters(dateFrom, dateTo).getMessage());
        }

        @Test
        void shouldThrowCurrencyProviderException_whenOtherExceptionIsThrown() throws CurrencyProviderException {
            // given && when
            final var dateTo = LocalDate.now();
            final var dateFrom = LocalDate.now().minusDays(1);
            when(nbpClient.getAvailableTableType()).thenThrow(new CurrencyProviderException(FinanceExceptionMessage.CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES_AND_DATE_RANGE));

            // then
            assertThatThrownBy(() -> nbpCurrencyProvider.getCurrencyRatesForDateRange(dateFrom, dateTo))
                    .isInstanceOf(CurrencyProviderException.class)
                    .hasMessage(FinanceExceptionMessage.CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES_AND_DATE_RANGE.withParameters(dateFrom, dateTo).getMessage());
        }

    }

}