package mm.expenses.manager.finance.exchangerate;

import lombok.Setter;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.exception.api.ApiFeignClientException;
import mm.expenses.manager.finance.exchangerate.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.exception.HistoricalCurrencyException;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import mm.expenses.manager.finance.exchangerate.provider.HistoricCurrencies;
import mm.expenses.manager.finance.exchangerate.provider.ProviderConfig;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.*;

class TestProvider implements CurrencyRateProvider<TestProvider.TestRate> {

    public static final String TEST_PROVIDER_NAME_1 = "test-provider-1";
    public static final String TEST_PROVIDER_NAME_2 = "test-provider-2";

    private final String name;
    private final boolean shouldThrowHistoricalCurrencyException;

    @Setter
    private boolean shouldReturnEmptyCurrentCurrencies;

    @Setter
    private boolean shouldThrowCurrencyProviderExceptionInCurrentCurrencyRatesWithHttpStatus = false;

    @Setter
    private boolean shouldThrowCurrencyProviderExceptionInCurrentCurrencyRates = false;

    TestProvider(final String name, final boolean shouldThrowHistoricalCurrencyException, final boolean shouldReturnEmptyCurrentCurrencies) {
        this.name = name;
        this.shouldThrowHistoricalCurrencyException = shouldThrowHistoricalCurrencyException;
        this.shouldReturnEmptyCurrentCurrencies = shouldReturnEmptyCurrentCurrencies;
    }

    TestProvider(final String name, final boolean shouldThrowHistoricalCurrencyException) {
        this(name, shouldThrowHistoricalCurrencyException, true);
    }

    @Override
    public Optional<TestProvider.TestRate> getCurrencyRateForDate(CurrencyCode currencyCode, LocalDate date) throws CurrencyProviderException {
        return Optional.empty();
    }

    @Override
    public Collection<TestProvider.TestRate> getCurrencyRateForDateRange(CurrencyCode currencyCode, LocalDate from, LocalDate to) throws CurrencyProviderException {
        return null;
    }

    @Override
    public Collection<TestProvider.TestRate> getCurrencyRatesForDate(LocalDate date) throws CurrencyProviderException {
        return null;
    }

    @Override
    public Collection<TestProvider.TestRate> getCurrencyRatesForDateRange(LocalDate from, LocalDate to) throws CurrencyProviderException {
        return null;
    }

    @Override
    public HistoricCurrencies<TestProvider.TestRate> getHistoricCurrencies() {
        return new TestProvider.TestHistoryUpdater(this, shouldThrowHistoricalCurrencyException);
    }

    @Override
    public Optional<TestProvider.TestRate> getCurrentCurrencyRate(CurrencyCode currencyCode) throws CurrencyProviderException {
        return Optional.empty();
    }

    @Override
    public Collection<TestProvider.TestRate> getCurrentCurrencyRates() throws CurrencyProviderException {
        if (shouldThrowCurrencyProviderExceptionInCurrentCurrencyRatesWithHttpStatus) {
            throw new CurrencyProviderException("", ApiFeignClientException.builder().status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        }
        if (shouldThrowCurrencyProviderExceptionInCurrentCurrencyRates) {
            throw new CurrencyProviderException("", new Exception());
        }
        if (shouldReturnEmptyCurrentCurrencies) {
            return Collections.emptyList();
        }
        return List.of(new TestProvider.TestRate(CurrencyCode.CAD, LocalDate.now(), 5.2, Map.of()));
    }

    @Override
    public ProviderConfig getProviderConfig() {
        final var config = new TestProvider.TestConfig();
        config.setName(name);

        return config;
    }

    static class TestConfig extends ProviderConfig {

    }

    static class TestRate extends CurrencyRate {

        TestRate(final CurrencyCode currency, final LocalDate date, final Double rate, final Map<String, Object> details) {
            super(currency, date, rate, details);
        }
    }

    static class TestHistoryUpdater extends HistoricCurrencies<TestProvider.TestRate> {

        private final boolean shouldThrowHistoricalCurrencyException;

        TestHistoryUpdater(final TestProvider provider, final boolean shouldThrowHistoricalCurrencyException) {
            super(provider);
            this.shouldThrowHistoricalCurrencyException = shouldThrowHistoricalCurrencyException;
        }

        @Override
        public Collection<TestProvider.TestRate> fetchHistoricalCurrencies() throws HistoricalCurrencyException {
            if (shouldThrowHistoricalCurrencyException) {
                throw new HistoricalCurrencyException("");
            }
            return List.of();
        }

    }

}
