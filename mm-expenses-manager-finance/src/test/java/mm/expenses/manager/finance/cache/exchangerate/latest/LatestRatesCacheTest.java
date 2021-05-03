package mm.expenses.manager.finance.cache.exchangerate.latest;

import mm.expenses.manager.finance.FinanceApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

class LatestRatesCacheTest extends FinanceApplicationTest {

    @MockBean
    private LatestRatesCache latestRatesCache;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldCallSaveInMemoryAfterContextRefreshedEventAndUpdateLatestInMemoryEvent() {
        verify(latestRatesCache).saveInMemory(); // verify after ContextRefreshedEvent
        reset(latestRatesCache);

        eventPublisher.publishEvent(new UpdateLatestInMemoryEvent(this));
        verify(latestRatesCache).saveInMemory(); // verify after UpdateLatestInMemoryEvent
    }

}
