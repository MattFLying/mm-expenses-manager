package mm.expenses.manager.finance.exchangerate;

import lombok.SneakyThrows;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.provider.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.*;
import java.util.function.Consumer;

import static mm.expenses.manager.finance.exchangerate.TestProvider.TEST_PROVIDER_NAME_1;
import static mm.expenses.manager.finance.exchangerate.TestProvider.TEST_PROVIDER_NAME_2;
import static org.mockito.Mockito.*;

class ExchangeRateHistoryUpdateTest extends FinanceApplicationTest {

    @MockBean
    private ExchangeRateRepository exchangeRateRepository;

    @MockBean
    private CurrencyProviders currencyProviders;

    @Autowired
    private ExchangeRateHistoryUpdate exchangeRateHistoryUpdate;

    @Captor
    private ArgumentCaptor<Consumer<CurrencyRateProvider<? extends CurrencyRate>>> providerConsumerCaptor;

    private final TestProvider provider_1 = new TestProvider(TEST_PROVIDER_NAME_1, false);
    private final TestProvider provider_2 = new TestProvider(TEST_PROVIDER_NAME_2, true);

    @SneakyThrows
    @Override
    protected void setupBeforeEachTest() {
        doReturn(Map.of(
                TEST_PROVIDER_NAME_1, provider_1,
                TEST_PROVIDER_NAME_2, provider_2
        )).when(currencyProviders).getProviders();
        doReturn(TEST_PROVIDER_NAME_1).when(currencyProviders).getProviderName();
    }

    @Override
    protected void setupAfterEachTest() {
        reset(exchangeRateRepository);
        reset(currencyProviders);
    }


    @Test
    void historyUpdate() {
        // given
        doReturn(provider_1).when(currencyProviders).getProvider();

        // when
        exchangeRateHistoryUpdate.update();

        // then
        verify(exchangeRateRepository).findAll();
        verify(exchangeRateRepository).saveAll(anyCollection());
    }

    @Test
    void shouldFetchHistoricalRatesForAlternativeProvider_whenSomethingWentWrongInDefaultProvider() {
        // given
        doReturn(provider_2).when(currencyProviders).getProvider();

        // when
        exchangeRateHistoryUpdate.update();

        // then
        verify(currencyProviders).executeOnAllProviders(providerConsumerCaptor.capture());
        final var providerConsumer = providerConsumerCaptor.getValue();
        providerConsumer.accept(provider_1);
    }

}