package mm.expenses.manager.finance.cache.config;

import lombok.Data;
import lombok.Generated;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Data
@Generated
@Configuration
@ConfigurationProperties(prefix = "app.configuration.cache.redis")
@ConditionalOnProperty(prefix = "app.configuration.cache", name = "type", havingValue = "redis")
class SpringRedisConfig {

    private static final int DEFAULT_PORT = 6379;

    private String host;
    private Integer port;

    Integer getPort() {
        return Objects.nonNull(port) ? port : DEFAULT_PORT;
    }

}
