package mm.expenses.manager.finance.cache.config;

import lombok.Generated;
import mm.expenses.manager.common.redis.config.DefaultRedisConfig;
import mm.expenses.manager.common.redis.config.SpringRedisConfig;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Generated
@Configuration
@Import({SpringRedisConfig.class})
@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "redis")
class FinanceRedisConfig extends DefaultRedisConfig {

    FinanceRedisConfig(final SpringRedisConfig config) {
        super(config);
    }

    @Bean
    RedisTemplate<String, ExchangeRateCache> redisTemplate() {
        final var template = new RedisTemplate<String, ExchangeRateCache>();

        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

}
