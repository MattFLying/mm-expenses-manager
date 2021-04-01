package mm.expenses.manager.finance.config;

import lombok.Data;
import lombok.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Generated
@Configuration
@ConfigurationProperties(prefix = "app.configuration")
class AppConfig {

    private String name;
    private String version;
    private Integer scheduledThreadPoolSize;

}
