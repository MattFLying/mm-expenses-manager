package mm.expenses.manager.finance.exchangerate.provider;

import mm.expenses.manager.exception.api.ApiInternalErrorException;
import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exception.FinanceExceptionMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.reset;

class CurrencyProvidersTest extends FinanceApplicationTest {

    @MockBean
    private CurrencyRatesConfig currencyRatesConfig;

    @Override
    protected void setupAfterEachTest() {
        reset(currencyRatesConfig);
    }

    @Test
    void shouldThrowException_whenNoProvidersFound() {
        // given
        final var currencyProviders = new CurrencyProviders(List.of(), currencyRatesConfig);
        assertThat(currencyProviders.getProviders()).isEmpty();

        // when && then
        assertThatThrownBy(currencyProviders::findDefaultProviderOrAny)
                .isInstanceOf(ApiInternalErrorException.class)
                .hasMessage(FinanceExceptionMessage.CURRENCY_PROVIDER_NOT_FOUND.getMessage());
    }

}