package mm.expenses.manager.product.config;

import lombok.Data;
import mm.expenses.manager.common.web.config.AppConfigProperties;
import mm.expenses.manager.common.web.config.Contact;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.configuration")
class AppConfig implements AppConfigProperties {

    private String name;
    private String description;
    private String version;
    private Contact contact;

}
