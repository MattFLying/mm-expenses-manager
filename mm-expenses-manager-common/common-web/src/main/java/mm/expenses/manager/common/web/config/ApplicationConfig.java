package mm.expenses.manager.common.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.configuration")
public class ApplicationConfig implements ApplicationConfigProperties {

    private String name;
    private String description;
    private String version;
    private Contact contact;

}