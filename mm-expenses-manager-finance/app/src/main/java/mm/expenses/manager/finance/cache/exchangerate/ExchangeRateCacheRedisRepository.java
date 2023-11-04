package mm.expenses.manager.finance.cache.exchangerate;

import lombok.Generated;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.repository.CrudRepository;

@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "redis")
interface ExchangeRateCacheRedisRepository extends CrudRepository<ExchangeRateCache, String>, ExchangeRateCacheRepository {

    @Override
    <S extends ExchangeRateCache> Iterable<S> saveAll(final Iterable<S> toSave);

    @Override
    void deleteAll(final Iterable<? extends ExchangeRateCache> toRemove);

    @Override
    void deleteAll();

    @Generated
    @Override
    default CacheType getCacheType() {
        return CacheType.REDIS;
    }

}
