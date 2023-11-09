package mm.expenses.manager.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.configuration")
class AppConfig {

    private String name;
    private String description;
    private String version;

    private Contact contact;

    String getDeveloperNameWithRole() {
        return String.format("%s - %s", contact.getName(), contact.getRole());
    }

    String getDeveloperEmail() {
        return contact.getEmail();
    }

    @Data
    static class Contact {

        private String name;
        private String email;
        private String role;

    }

}
