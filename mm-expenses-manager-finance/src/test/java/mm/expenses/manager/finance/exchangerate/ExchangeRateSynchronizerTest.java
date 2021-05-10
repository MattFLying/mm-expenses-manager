package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import mm.expenses.manager.finance.currency.CurrencyRatesConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static mm.expenses.manager.finance.exchangerate.TestProvider.TEST_PROVIDER_NAME_1;
import static org.mockito.Mockito.*;

class ExchangeRateSynchronizerTest extends FinanceApplicationTest {

    @MockBean
    private CurrencyProviders currencyProviders;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @MockBean
    private TaskScheduler taskScheduler;

    @Autowired
    private ExchangeRateSynchronizer exchangeRateSynchronizer;

    @Captor
    private ArgumentCaptor<Consumer<CurrencyRateProvider<? extends CurrencyRate>>> providerConsumerCaptor;

    @Captor
    private ArgumentCaptor<Predicate<CurrencyRateProvider<? extends CurrencyRate>>> providerPredicateCaptor;

    private TestProvider provider;


    @Override
    protected void setupBeforeEachTest() {
        this.provider = new TestProvider(TEST_PROVIDER_NAME_1, false);

        doReturn(provider).when(currencyProviders).getProvider();
        doReturn(TEST_PROVIDER_NAME_1).when(currencyProviders).getProviderName();

        final var globalConfig = new CurrencyRatesConfig();
        globalConfig.setRescheduleWhenSynchronizationFailedCron("*/15 * * * * *");

        doReturn(globalConfig).when(currencyProviders).getGlobalConfig();
    }

    @Override
    protected void setupAfterEachTest() {
        reset(currencyProviders);
        reset(exchangeRateService);
        reset(taskScheduler);
    }


    @Test
    void shouldUpdateLatestExchangeRates_whenLatestFromCurrentProviderAreEmptyThenCallAlternativeProviders() {
        // given
        provider.setShouldReturnEmptyCurrentCurrencies(true);

        // when
        exchangeRateSynchronizer.scheduleUpdateLatestExchangeRates();

        // then
        verify(currencyProviders).getProvider();
        verify(taskScheduler).schedule(any(ExchangeRateSynchronizer.RescheduleFailedProvider.class), any(CronTrigger.class));
        verify(currencyProviders).executeOnAllProviders(any(), any());

        verify(currencyProviders).executeOnAllProviders(providerPredicateCaptor.capture(), providerConsumerCaptor.capture());
        final var providerConsumer = providerConsumerCaptor.getValue();
        final var providerPredicate = providerPredicateCaptor.getValue();
        providerConsumer.accept(provider);
        providerPredicate.test(provider);
    }

    @Test
    void shouldReschedule_whenServerErrorOccurred() {
        // given
        provider.setShouldReturnEmptyCurrentCurrencies(true);
        provider.setShouldThrowCurrencyProviderExceptionInCurrentCurrencyRatesWithHttpStatus(true);

        // when
        exchangeRateSynchronizer.scheduleUpdateLatestExchangeRates();

        // then
        verify(currencyProviders).getProvider();
        verify(taskScheduler).schedule(any(ExchangeRateSynchronizer.RescheduleFailedProvider.class), any(CronTrigger.class));

        verify(currencyProviders).executeOnAllProviders(providerPredicateCaptor.capture(), providerConsumerCaptor.capture());
        final var providerConsumer = providerConsumerCaptor.getValue();
        final var providerPredicate = providerPredicateCaptor.getValue();
        providerConsumer.accept(provider);
        providerPredicate.test(provider);
        verify(currencyProviders, times(2)).executeOnAllProviders(any(), any());
    }

    @Test
    void shouldReschedule_whenUnknownErrorOccurred() {
        // given
        provider.setShouldReturnEmptyCurrentCurrencies(true);
        provider.setShouldThrowCurrencyProviderExceptionInCurrentCurrencyRates(true);

        // when
        exchangeRateSynchronizer.scheduleUpdateLatestExchangeRates();

        // then
        verify(currencyProviders).getProvider();
        verify(taskScheduler).schedule(any(ExchangeRateSynchronizer.RescheduleFailedProvider.class), any(CronTrigger.class));

        verify(currencyProviders).executeOnAllProviders(providerPredicateCaptor.capture(), providerConsumerCaptor.capture());
        final var providerConsumer = providerConsumerCaptor.getValue();
        final var providerPredicate = providerPredicateCaptor.getValue();
        providerConsumer.accept(provider);
        providerPredicate.test(provider);
        verify(currencyProviders, times(2)).executeOnAllProviders(any(), any());
    }

    @Test
    void shouldUpdateLatestExchangeRates_whenLatestFromCurrentProviderExistThenDirectlyUpdate() {
        // given
        provider.setShouldReturnEmptyCurrentCurrencies(false);

        // when
        exchangeRateSynchronizer.scheduleUpdateLatestExchangeRates();

        // then
        verify(currencyProviders).getProvider();
        verify(exchangeRateService).synchronize(anyCollection());
    }

}