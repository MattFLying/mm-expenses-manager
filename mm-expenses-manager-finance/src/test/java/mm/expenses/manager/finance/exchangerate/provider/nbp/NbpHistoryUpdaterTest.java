package mm.expenses.manager.finance.exchangerate.provider.nbp;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import mm.expenses.manager.finance.exception.HistoricalCurrencyException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;

import static mm.expenses.manager.common.util.DateUtils.ZONE_UTC;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.HISTORY_FROM_YEAR;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.MAX_DAYS_TO_FETCH;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.PROVIDER_NAME;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.TABLE_TYPE;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.createNbpCurrencyRate;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyRatesAssert.assertNbpCurrencyRates;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class NbpHistoryUpdaterTest extends FinanceApplicationTest {

    private NbpApiConfig nbpApiConfig;

    @MockBean
    private NbpCurrencyProvider nbpCurrencyProvider;

    @MockBean
    private CurrencyProviders currencyProviders;

    private NbpHistoryUpdater nbpHistoryUpdater;

    @Override
    protected void setupBeforeEachTest() {
        nbpApiConfig = new NbpApiConfig();
        nbpApiConfig.setName(PROVIDER_NAME);

        final var details = new NbpApiConfig.Details();
        details.setMaxDaysToFetch(MAX_DAYS_TO_FETCH);
        details.setHistoryFromYear(HISTORY_FROM_YEAR);

        nbpApiConfig.setDetails(details);

        when(nbpCurrencyProvider.getProviderConfig()).thenReturn(nbpApiConfig);

        nbpHistoryUpdater = new NbpHistoryUpdater(nbpCurrencyProvider);
    }

    @Override
    protected void setupAfterEachTest() {
        reset(currencyProviders);
        reset(nbpCurrencyProvider);
    }

    @Test
    void shouldFetchHistoricalCurrencies() throws HistoricalCurrencyException, CurrencyProviderException {
        // given
        final var dateOfFirstAvailableRate = LocalDate.of(2021, 3, 15);
        final var tableNumber = "test/table/number/01";

        final var rate_1 = 3.6d;
        final var rate_2 = 9.5d;
        final var rate_3 = 2.4d;

        final var expected_1 = createNbpCurrencyRate(CurrencyCode.CHF, dateOfFirstAvailableRate, rate_1, TABLE_TYPE, tableNumber);
        final var expected_2 = createNbpCurrencyRate(CurrencyCode.JPY, dateOfFirstAvailableRate, rate_2, TABLE_TYPE, tableNumber);
        final var expected_3 = createNbpCurrencyRate(CurrencyCode.NZD, dateOfFirstAvailableRate, rate_3, TABLE_TYPE, tableNumber);

        final var dateTo = LocalDate.now(ZONE_UTC);
        final var expectedCountOfPreparedRatesPerCurrency = DateUtils.daysBetween(dateOfFirstAvailableRate, dateTo);

        // when
        when(nbpCurrencyProvider.getCurrencyRatesForDateRange(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(expected_1, expected_2, expected_3));
        final var result = nbpHistoryUpdater.fetch();

        // then
        assertNbpCurrencyRates(result)
                .forCurrencyHasElements(CurrencyCode.CHF, expectedCountOfPreparedRatesPerCurrency)
                .allForCurrencySameAsWithoutDate(CurrencyCode.CHF, expected_1)
                .allForCurrencyInDateRange(CurrencyCode.CHF, dateOfFirstAvailableRate, dateTo)

                .forCurrencyHasElements(CurrencyCode.JPY, expectedCountOfPreparedRatesPerCurrency)
                .allForCurrencySameAsWithoutDate(CurrencyCode.JPY, expected_2)
                .allForCurrencyInDateRange(CurrencyCode.JPY, dateOfFirstAvailableRate, dateTo)

                .forCurrencyHasElements(CurrencyCode.NZD, expectedCountOfPreparedRatesPerCurrency)
                .allForCurrencySameAsWithoutDate(CurrencyCode.NZD, expected_3)
                .allForCurrencyInDateRange(CurrencyCode.NZD, dateOfFirstAvailableRate, dateTo);
    }

    @Test
    void shouldRetrieveEmptyList_whenCurrencyProviderExceptionHasThrown() throws HistoricalCurrencyException, CurrencyProviderException {
        // given && when
        when(nbpCurrencyProvider.getCurrencyRatesForDateRange(any(LocalDate.class), any(LocalDate.class))).thenThrow(new CurrencyProviderException(FinanceExceptionMessage.CURRENCY_PROVIDER_UNKNOWN_ALL_CURRENCIES_AND_DATE_RANGE.withParameters("", "")));

        final var result = nbpHistoryUpdater.fetch();

        // then
        assertNbpCurrencyRates(result).isEmpty();
    }

    @Test
    void shouldThrowHistoricalCurrencyException_whenOtherExceptionIsThrown() {
        // given && when
        when(nbpCurrencyProvider.getProviderConfig()).thenReturn(null);

        // then
        assertThatThrownBy(() -> nbpHistoryUpdater.fetch())
                .isInstanceOf(HistoricalCurrencyException.class)
                .hasMessage(FinanceExceptionMessage.SAVE_HISTORIC_EXCHANGE_RATES_UNKNOWN_ERROR.getMessage());
    }

}