package mm.expenses.manager.finance.management;

import mm.expenses.manager.finance.FinanceApplicationTest;
import mm.expenses.manager.finance.exchangerate.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ManagementControllerTest extends FinanceApplicationTest {

    private static final String BASE_URL = "/management";

    @MockBean
    private ExchangeRateService service;

    @Override
    protected void setupAfterEachTest() {
        reset(service);
    }

    @Test
    void shouldCallExchangeRateHistoryUpdate() throws Exception {
        mockMvc.perform(post(historyUpdateUrl())).andExpect(status().isNoContent());
        verify(service).historyUpdate();
    }

    private String historyUpdateUrl() {
        return BASE_URL + "/exchange-rates/history-update";
    }

}