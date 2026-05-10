package mm.expenses.manager.finance.cache.exchangerate.latest;

import mm.expenses.manager.finance.FinanceApplicationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.*;

class LatestRatesCacheServiceEventTest extends FinanceApplicationTest {

    @MockitoBean
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
