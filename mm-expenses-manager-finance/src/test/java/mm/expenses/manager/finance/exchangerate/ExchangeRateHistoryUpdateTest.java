package mm.expenses.manager.finance.exchangerate;

import lombok.SneakyThrows;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.exception.CurrencyProviderException;
import mm.expenses.manager.finance.exchangerate.exception.HistoricalCurrencyException;
import mm.expenses.manager.finance.exchangerate.provider.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

class ExchangeRateHistoryUpdateTest extends FinanceApplicationTest {

    private static final String TEST_PROVIDER_NAME = "test-provider-1";
    private static final String TEST_PROVIDER_NAME_2 = "test-provider-2";

    @MockBean
    private ExchangeRateRepository repository;

    @MockBean
    private CurrencyProviders providers;

    @Autowired
    private ExchangeRateHistoryUpdate historyUpdate;

    @Captor
    private ArgumentCaptor<Consumer<CurrencyRateProvider<? extends CurrencyRate>>> providerConsumerCaptor;

    private final TestProvider provider_1 = new TestProvider(TEST_PROVIDER_NAME, false);
    private final TestProvider provider_2 = new TestProvider(TEST_PROVIDER_NAME_2, true);

    @SneakyThrows
    @Override
    protected void setupBeforeEachTest() {
        doReturn(Map.of(
                TEST_PROVIDER_NAME, provider_1,
                TEST_PROVIDER_NAME_2, provider_2
        )).when(providers).getProviders();
        doReturn(TEST_PROVIDER_NAME).when(providers).getProviderName();
    }

    @Override
    protected void setupAfterEachTest() {
        reset(repository);
        reset(providers);
    }


    @Test
    void historyUpdate() {
        // given
        doReturn(provider_1).when(providers).getProvider();

        // when
        historyUpdate.update();

        // then
        verify(repository).findAll();
        verify(repository).saveAll(anyCollection());
    }

    @Test
    void shouldFetchHistoricalRatesForAlternativeProvider_whenSomethingWentWrongInDefaultProvider() {
        // given
        doReturn(provider_2).when(providers).getProvider();

        // when
        historyUpdate.update();

        // then
        verify(providers).executeOnAllProviders(providerConsumerCaptor.capture());
        final var providerConsumer = providerConsumerCaptor.getValue();
        providerConsumer.accept(provider_1);
    }

    private static class TestRate extends CurrencyRate {

        TestRate(final CurrencyCode currency, final LocalDate date, final Double rate, final Map<String, Object> details) {
            super(currency, date, rate, details);
        }
    }

    private static class TestHistoryUpdater extends HistoricCurrencies<TestRate> {

        private final boolean shouldThrowHistoricalCurrencyException;

        TestHistoryUpdater(final TestProvider provider, final boolean shouldThrowHistoricalCurrencyException) {
            super(provider);
            this.shouldThrowHistoricalCurrencyException = shouldThrowHistoricalCurrencyException;
        }

        @Override
        public Collection<TestRate> fetchHistoricalCurrencies() throws HistoricalCurrencyException {
            if (shouldThrowHistoricalCurrencyException) {
                throw new HistoricalCurrencyException("");
            }
            return List.of();
        }

    }

    private static class TestProvider implements CurrencyRateProvider<TestRate> {

        private final String name;
        private final boolean shouldThrowHistoricalCurrencyException;

        private TestProvider(final String name, final boolean shouldThrowHistoricalCurrencyException) {
            this.name = name;
            this.shouldThrowHistoricalCurrencyException = shouldThrowHistoricalCurrencyException;
        }

        private TestProvider() {
            this(null, false);
        }

        @Override
        public Optional<TestRate> getCurrencyRateForDate(CurrencyCode currencyCode, LocalDate date) throws CurrencyProviderException {
            return Optional.empty();
        }

        @Override
        public Collection<TestRate> getCurrencyRateForDateRange(CurrencyCode currencyCode, LocalDate from, LocalDate to) throws CurrencyProviderException {
            return null;
        }

        @Override
        public Collection<TestRate> getCurrencyRatesForDate(LocalDate date) throws CurrencyProviderException {
            return null;
        }

        @Override
        public Collection<TestRate> getCurrencyRatesForDateRange(LocalDate from, LocalDate to) throws CurrencyProviderException {
            return null;
        }

        @Override
        public HistoricCurrencies<TestRate> getHistoricCurrencies() {
            return new TestHistoryUpdater(this, shouldThrowHistoricalCurrencyException);
        }

        @Override
        public Optional<TestRate> getCurrentCurrencyRate(CurrencyCode currencyCode) throws CurrencyProviderException {
            return Optional.empty();
        }

        @Override
        public Collection<TestRate> getCurrentCurrencyRates() throws CurrencyProviderException {
            return null;
        }

        @Override
        public ProviderConfig getProviderConfig() {
            final var config = new TestConfig();
            config.setName(name);

            return config;
        }

    }

    private static class TestConfig extends ProviderConfig {

    }

}