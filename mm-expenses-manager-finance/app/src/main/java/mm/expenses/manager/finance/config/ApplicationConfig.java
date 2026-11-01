package mm.expenses.manager.finance.config;

import lombok.Data;
import mm.expenses.manager.common.web.config.ApplicationConfigProperties;
import mm.expenses.manager.common.web.config.Contact;
import mm.expenses.manager.common.web.config.Keycloak;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.configuration")
class ApplicationConfig implements ApplicationConfigProperties {

    private String name;
    private String description;
    private String version;
    private Contact contact;
    private Keycloak keycloak;

    private Integer scheduledThreadPoolSize;

}