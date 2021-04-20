package mm.expenses.manager.finance.exchangerate.latest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import mm.expenses.manager.finance.exchangerate.ExchangeRate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

@Generated
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.configuration", name = "cache", havingValue = "redis")
class RedisConfig {

    private final ObjectMapper objectMapper;
    private final SpringRedisConfig config;

    @Bean
    RedisTemplate<CurrencyCode, ExchangeRate> redisTemplate() {
        final var jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        final var currencyCodeSerializer = new CurrencyCodeRedisSerializer(objectMapper);

        final var template = new RedisTemplate<CurrencyCode, ExchangeRate>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(currencyCodeSerializer);
        template.setHashKeySerializer(currencyCodeSerializer);
        template.setValueSerializer(jdkSerializationRedisSerializer);
        template.setHashValueSerializer(jdkSerializationRedisSerializer);
        template.setEnableTransactionSupport(true);
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
