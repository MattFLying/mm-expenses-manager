package mm.expenses.manager.finance.cache.exchangerate;

import mm.expenses.manager.finance.FinanceApplicationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCacheRepository.CacheType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

class ExchangeRateCacheClearTest extends FinanceApplicationTest {

    @MockBean
    private ExchangeRateCacheRepository exchangeRateCacheRepository;

    @Autowired
    private ExchangeRateCacheService exchangeRateCacheService;

    @Override
    protected void setupAfterEachTest() {
        reset(exchangeRateCacheRepository);
    }


    @Nested
    class CacheType {

        @Test
        void shouldRetrieveCorrectCacheTypeMap() {
            when(exchangeRateCacheRepository.getCacheType()).thenReturn(MAP);
            assertThat(exchangeRateCacheService.getCacheType()).isEqualTo(MAP);
        }

        @Test
        void shouldRetrieveCorrectCacheTypeRedis() {
            when(exchangeRateCacheRepository.getCacheType()).thenReturn(REDIS);
            assertThat(exchangeRateCacheService.getCacheType()).isEqualTo(REDIS);
        }

    }

    @Nested
    class ClearCache {

        @Test
        void clearNotLatestCache() {
            // given && when
            exchangeRateCacheService.clearNotLatestCache();

            // then
            await().untilAsserted(() -> {
                verify(exchangeRateCacheRepository, atLeastOnce()).deleteAll(any());
                verify(exchangeRateCacheRepository, atLeastOnce()).findByIsLatestFalse();
            });
        }

        @Test
        void clearCache() {
            // given && when
            exchangeRateCacheService.clearCache();

            // then
            await().untilAsserted(() -> verify(exchangeRateCacheRepository, atLeastOnce()).deleteAll());
        }

    }

}