package mm.expenses.manager.account.config;

import lombok.Data;
import lombok.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Generated
@Configuration
@ConfigurationProperties(prefix = "app.configuration.keycloak")
class KeycloakConfig {

    private String hostname;

    private String port;

    private String url;

    private String realm;

    private Oauth oauth;

    private Admin admin;


    @Data
    final static class Oauth {

        private String clientId;

    }


    @Data
    final static class Admin {

        private String realm;

        private String clientId;

        private String username;

        private String secret;

    }

}
