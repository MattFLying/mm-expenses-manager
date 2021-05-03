package mm.expenses.manager.finance.cache.config;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.finance.cache.exchangerate.ExchangeRateCache;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Generated
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "redis")
class RedisConfig {

    private final SpringRedisConfig config;

    @Bean
    RedisTemplate<String, ExchangeRateCache> redisTemplate() {
        final var template = new RedisTemplate<String, ExchangeRateCache>();

        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        final var configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(config.getHost());
        configuration.setPort(config.getPort());
        return new JedisConnectionFactory(configuration);
    }

}
