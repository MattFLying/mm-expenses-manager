package mm.expenses.manager.common.redis.config;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

@Generated
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "redis")
public class DefaultRedisConfig {

    private final SpringRedisConfig config;

    @Bean
    protected JedisConnectionFactory jedisConnectionFactory() {
        final var configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(config.getHost());
        configuration.setPort(config.getPort());
        return new JedisConnectionFactory(configuration);
    }

}
