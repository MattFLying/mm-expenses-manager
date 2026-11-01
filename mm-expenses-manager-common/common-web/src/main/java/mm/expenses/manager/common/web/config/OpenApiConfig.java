package mm.expenses.manager.common.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.*;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final ApplicationConfigProperties applicationConfig;

    /**
     * Responsible for adding 'Token' parameter into headers in every Swagger API endpoint
     * when such token will be given.
     */
    @Bean
    @ConditionalOnProperty(name = SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, havingValue = "true")
    OperationCustomizer addGlobalHeaderParam() {
        return (operation, handlerMethod) -> {
            operation.addParametersItem(
                    new Parameter().in("header")
                            .name(SecurityRequestConfig.TOKEN)
                            .description("To be used when user has already active token but can be skipped.")
                            .required(false)
                            .schema(new StringSchema())
            );
            return operation;
        };
    }

    @Bean
    @ConditionalOnProperty(name = SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, havingValue = "true")
    OpenAPI openAPISecured() {
        return new OpenAPI().info(openApiInfo())
                .components(new Components().addSecuritySchemes(SecurityRequestConfig.KEYCLOAK_OAUTH_SCHEME_NAME, oauthSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SecurityRequestConfig.KEYCLOAK_OAUTH_SCHEME_NAME));
    }

    @Bean
    @ConditionalOnProperty(name = SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, havingValue = "false", matchIfMissing = true)
    OpenAPI openAPIUnsecured() {
        return new OpenAPI().info(openApiInfo());
    }

    private Info openApiInfo() {
        return new Info().title(applicationConfig.getName())
                .description(applicationConfig.getDescription())
                .version(applicationConfig.getVersion())
                .contact(buildContact());
    }

    private Contact buildContact() {
        return new Contact().name(applicationConfig.getDeveloperNameWithRole())
                .email(applicationConfig.getDeveloperEmail());
    }

    /**
     * Configures oauth security scheme when the secured mode is enabled.
     * Thanks to that there is no need to apply security in the Swagger schemas - it will be
     * added automatically here to secure every possible API endpoint.
     */
    private SecurityScheme oauthSecurityScheme() {
        final var keycloakUrl = applicationConfig.getKeycloakUrl();
        final var keycloakRealm = applicationConfig.getKeycloakRealm();

        final var authUrl = String.format("%s/realms/%s/protocol/openid-connect/auth", keycloakUrl, keycloakRealm);
        final var tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakUrl, keycloakRealm);

        return new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                .description("Log in into Expenses Manager application")
                .flows(new OAuthFlows().authorizationCode(
                                new OAuthFlow()
                                        .authorizationUrl(authUrl)
                                        .tokenUrl(tokenUrl)
                        )
                );
    }

}
