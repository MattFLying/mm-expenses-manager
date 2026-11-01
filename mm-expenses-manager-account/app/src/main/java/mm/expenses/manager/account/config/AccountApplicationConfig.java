package mm.expenses.manager.account.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import mm.expenses.manager.common.kafka.producer.AsyncKafkaProducer;
import mm.expenses.manager.common.postgresql.pagination.PaginationHelper;
import mm.expenses.manager.common.utils.config.ObjectMapperConfig;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.web.config.ApplicationConfig;
import mm.expenses.manager.common.web.config.ErrorHandlingConfig;
import mm.expenses.manager.common.web.config.OpenApiConfig;
import mm.expenses.manager.common.web.config.WebMvcConfig;
import mm.expenses.manager.common.web.security.SecurityImportSelector;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Generated
@Configuration
@Import({
        ErrorHandlingConfig.class, PaginationConfig.class, WebMvcConfig.class, OpenApiConfig.class,
        ApplicationConfig.class, AsyncKafkaProducer.class, SecurityImportSelector.class
})
class AccountApplicationConfig {

    @Bean
    ObjectMapper objectMapper() {
        return ObjectMapperConfig.objectMapper();
    }

    @Bean
    PaginationHelper paginationHelper(final PaginationConfig paginationConfig) {
        return new PaginationHelper(paginationConfig);
    }

    @Bean
    OpenApiConfig openApiConfig(final ApplicationConfig applicationConfig) {
        return new OpenApiConfig(applicationConfig);
    }

    @Bean
    Keycloak keycloak(final KeycloakConfig keycloakConfig) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakConfig.getUrl())
                .realm(keycloakConfig.getAdmin().getRealm())
                .clientId(keycloakConfig.getAdmin().getClientId())
                .username(keycloakConfig.getAdmin().getUsername())
                .password(keycloakConfig.getAdmin().getSecret())
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }

}
