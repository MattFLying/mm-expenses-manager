package mm.expenses.manager.finance.exchangerate;

import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyProviders;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRate;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRateProvider;
import mm.expenses.manager.finance.exchangerate.provider.CurrencyRatesConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

class ExchangeRateSynchronizerTest extends FinanceApplicationTest {

    private static final String TEST_PROVIDER_NAME = "test-provider";

    @MockBean
    private CurrencyProviders providers;

    @MockBean
    private ExchangeRateService service;

    @MockBean
    private TaskScheduler scheduler;

    @MockBean
    private CurrencyRatesConfig config;

    @Autowired
    private ExchangeRateSynchronizer synchronizer;

    @Captor
    private ArgumentCaptor<Consumer<CurrencyRateProvider<? extends CurrencyRate>>> providerConsumerCaptor;

    @Captor
    private ArgumentCaptor<Predicate<CurrencyRateProvider<? extends CurrencyRate>>> providerPredicateCaptor;

    private TestProvider provider;


    @Override
    protected void setupBeforeEachTest() {
        this.provider = new TestProvider(TEST_PROVIDER_NAME, false);

        doReturn(provider).when(providers).getProvider();
        doReturn(TEST_PROVIDER_NAME).when(providers).getProviderName();

        final var globalConfig = new CurrencyRatesConfig();
        globalConfig.setRescheduleWhenSynchronizationFailedCron("*/15 * * * * *");

        doReturn(globalConfig).when(providers).getGlobalConfig();
    }

    @Override
    protected void setupAfterEachTest() {
        reset(providers);
        reset(service);
        reset(scheduler);
        reset(config);
    }


    @Test
    void shouldUpdateLatestExchangeRates_whenLatestFromCurrentProviderAreEmptyThenCallAlternativeProviders() {
        // given
        provider.setShouldReturnEmptyCurrentCurrencies(true);

        // when
        synchronizer.scheduleUpdateLatestExchangeRates();

        // then
        verify(providers).getProvider();
        verify(scheduler).schedule(any(ExchangeRateSynchronizer.RescheduleFailedProvider.class), any(CronTrigger.class));
        verify(providers).executeOnAllProviders(any(), any());

        verify(providers).executeOnAllProviders(providerPredicateCaptor.capture(), providerConsumerCaptor.capture());
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
        synchronizer.scheduleUpdateLatestExchangeRates();

        // then
        verify(providers).getProvider();
        verify(scheduler).schedule(any(ExchangeRateSynchronizer.RescheduleFailedProvider.class), any(CronTrigger.class));

        verify(providers).executeOnAllProviders(providerPredicateCaptor.capture(), providerConsumerCaptor.capture());
        final var providerConsumer = providerConsumerCaptor.getValue();
        final var providerPredicate = providerPredicateCaptor.getValue();
        providerConsumer.accept(provider);
        providerPredicate.test(provider);
        verify(providers, times(2)).executeOnAllProviders(any(), any());
    }

    @Test
    void shouldReschedule_whenUnknownErrorOccurred() {
        // given
        provider.setShouldReturnEmptyCurrentCurrencies(true);
        provider.setShouldThrowCurrencyProviderExceptionInCurrentCurrencyRates(true);

        // when
        synchronizer.scheduleUpdateLatestExchangeRates();

        // then
        verify(providers).getProvider();
        verify(scheduler).schedule(any(ExchangeRateSynchronizer.RescheduleFailedProvider.class), any(CronTrigger.class));

        verify(providers).executeOnAllProviders(providerPredicateCaptor.capture(), providerConsumerCaptor.capture());
        final var providerConsumer = providerConsumerCaptor.getValue();
        final var providerPredicate = providerPredicateCaptor.getValue();
        providerConsumer.accept(provider);
        providerPredicate.test(provider);
        verify(providers, times(2)).executeOnAllProviders(any(), any());
    }

    @Test
    void shouldUpdateLatestExchangeRates_whenLatestFromCurrentProviderExistThenDirectlyUpdate() {
        // given
        provider.setShouldReturnEmptyCurrentCurrencies(false);

        // when
        synchronizer.scheduleUpdateLatestExchangeRates();

        // then
        verify(providers).getProvider();
        verify(service).createOrUpdate(anyCollection());
    }

}