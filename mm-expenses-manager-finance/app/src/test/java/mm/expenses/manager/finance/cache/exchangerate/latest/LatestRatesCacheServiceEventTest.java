package mm.expenses.manager.finance.cache.exchangerate.latest;

import mm.expenses.manager.finance.FinanceApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

class LatestRatesCacheServiceEventTest extends FinanceApplicationTest {

    @MockBean
    private LatestRatesCacheService latestRatesCacheService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldCallSaveInMemoryAfterContextRefreshedEventAndUpdateLatestInMemoryEvent() {
        verify(latestRatesCacheService).saveInMemory(); // verify after ContextRefreshedEvent
        reset(latestRatesCacheService);

        eventPublisher.publishEvent(new UpdateLatestInMemoryEvent(this));
        verify(latestRatesCacheService).saveInMemory(); // verify after UpdateLatestInMemoryEvent
    }

}
