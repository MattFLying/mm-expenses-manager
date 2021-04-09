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

}