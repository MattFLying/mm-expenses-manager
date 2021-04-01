package mm.expenses.manager.finance.exchangerate.provider.nbp;

import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.common.util.DateUtils;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.exception.HistoricalCurrencyException;
import mm.expenses.manager.finance.exchangerate.provider.ProviderConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.List;

import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyHelper.createNbpCurrencyRate;
import static mm.expenses.manager.finance.exchangerate.provider.nbp.NbpCurrencyRatesAssert.assertNbpCurrencyRates;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class NbpHistoryUpdaterTest extends FinanceApplicationTest {

    private static final TableType TABLE_TYPE = TableType.A;
    private static final int MAX_MONTHS_TO_FETCH = 1;
    private static final int MAX_DAYS_TO_FETCH = 30;
    private static final int HISTORY_FROM_YEAR = 2021;

    @MockBean
    private NbpApiConfig config;

    @MockBean
    private NbpCurrencyProvider provider;

    private NbpHistoryUpdater updater;

    @Override
    protected void setupBeforeEachTest() {
        final var details = new ProviderConfig.Details();
        details.setMaxMonthsToFetch(MAX_MONTHS_TO_FETCH);
        details.setMaxDaysToFetch(MAX_DAYS_TO_FETCH);
        details.setHistoryFromYear(HISTORY_FROM_YEAR);
        when(config.getDetails()).thenReturn(details);

        when(provider.getProviderConfig()).thenReturn(config);

        this.updater = new NbpHistoryUpdater(provider);
    }

    @Override
    protected void setupAfterEachTest() {
        reset(config);
        reset(provider);
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

        final var dateTo = LocalDate.now();
        final var expectedCountOfPreparedRatesPerCurrency = DateUtils.daysBetween(dateOfFirstAvailableRate, dateTo);

        // when
        when(provider.getCurrencyRatesForDateRange(dateOfFirstAvailableRate.withDayOfMonth(1), dateOfFirstAvailableRate.withDayOfMonth(dateOfFirstAvailableRate.lengthOfMonth())))
                .thenReturn(List.of(expected_1, expected_2, expected_3));


        final var result = updater.fetchHistoricalCurrencies();

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
        when(provider.getCurrencyRatesForDateRange(any(LocalDate.class), any(LocalDate.class))).thenThrow(CurrencyProviderException.class);

        final var result = updater.fetchHistoricalCurrencies();

        // then
        assertNbpCurrencyRates(result).isEmpty();
    }

    @Test
    void shouldThrowHistoricalCurrencyException_whenOtherExceptionIsThrown() {
        // given && when
        when(provider.getProviderConfig()).thenReturn(null);

        // then
        assertThatThrownBy(() -> updater.fetchHistoricalCurrencies())
                .isInstanceOf(HistoricalCurrencyException.class)
                .hasMessage("Something went wrong during fetching historical currencies.");
    }

}