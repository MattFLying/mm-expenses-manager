package mm.expenses.manager.common.web.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpenApiConfigTest {

    @Mock
    private ApplicationConfigProperties applicationConfig;

    @Mock
    private HandlerMethod handlerMethod;

    private OpenApiConfig openApiConfig;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @BeforeEach
    public void setUp() {
        openApiConfig = new OpenApiConfig(applicationConfig);
    }

    @Test
    public void shouldAddGlobalHeaderParam() {
        // given
        final var customizer = openApiConfig.addGlobalHeaderParam();
        assertThat(customizer).isNotNull();

        final var operation = new Operation();

        // when
        final var resultOperation = customizer.customize(operation, handlerMethod);

        // then
        assertThat(resultOperation).isNotNull();
        assertThat(resultOperation.getParameters()).hasSize(1);

        final var param = resultOperation.getParameters().get(0);
        assertThat(param.getIn()).isEqualTo("header");
        assertThat(param.getName()).isEqualTo(SecurityRequestConfig.TOKEN);
        assertThat(param.getRequired()).isFalse();
        assertThat(param.getSchema()).isInstanceOf(StringSchema.class);
    }

    @Test
    public void shouldCreateSecuredOpenApi() {
        // given
        when(applicationConfig.getName()).thenReturn("Test App");
        when(applicationConfig.getDescription()).thenReturn("Test Description");
        when(applicationConfig.getVersion()).thenReturn("1.0.0");
        when(applicationConfig.getDeveloperNameWithRole()).thenReturn("John Doe");
        when(applicationConfig.getDeveloperEmail()).thenReturn("john@example.com");
        when(applicationConfig.getKeycloakUrl()).thenReturn("http://keycloak:8080");
        when(applicationConfig.getKeycloakRealm()).thenReturn("test-realm");

        // when
        final var openAPI = openApiConfig.openAPISecured();

        // then
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Test App");
        assertThat(openAPI.getInfo().getDescription()).isEqualTo("Test Description");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("John Doe");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("john@example.com");

        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey(SecurityRequestConfig.KEYCLOAK_OAUTH_SCHEME_NAME);

        final var scheme = openAPI.getComponents().getSecuritySchemes().get(SecurityRequestConfig.KEYCLOAK_OAUTH_SCHEME_NAME);
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.OAUTH2);
        assertThat(scheme.getFlows().getAuthorizationCode().getAuthorizationUrl()).isEqualTo("http://keycloak:8080/realms/test-realm/protocol/openid-connect/auth");
        assertThat(scheme.getFlows().getAuthorizationCode().getTokenUrl()).isEqualTo("http://keycloak:8080/realms/test-realm/protocol/openid-connect/token");
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }

    @Test
    public void shouldCreateUnsecuredOpenApi() {
        // given
        when(applicationConfig.getName()).thenReturn("Test App");
        when(applicationConfig.getDescription()).thenReturn("Test Description");
        when(applicationConfig.getVersion()).thenReturn("1.0.0");
        when(applicationConfig.getDeveloperNameWithRole()).thenReturn("John Doe");
        when(applicationConfig.getDeveloperEmail()).thenReturn("john@example.com");

        // when
        final var openAPI = openApiConfig.openAPIUnsecured();

        // then
        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Test App");
        assertThat(openAPI.getComponents()).isNull();
        assertThat(openAPI.getSecurity()).isNull();
    }

    @Test
    public void shouldLoadSecuredBeans_whenSecurityIsEnabled() {
        contextRunner.withBean(ApplicationConfigProperties.class, () -> applicationConfig)
                .withUserConfiguration(OpenApiConfig.class)
                .withPropertyValues(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH + "=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenApiConfig.class);
                    assertThat(context).hasBean("openAPISecured");
                    assertThat(context).hasBean("addGlobalHeaderParam");
                    assertThat(context).doesNotHaveBean("openAPIUnsecured");
                });
    }

    @Test
    public void shouldLoadUnsecuredBeanByDefault() {
        contextRunner.withBean(ApplicationConfigProperties.class, () -> applicationConfig)
                .withUserConfiguration(OpenApiConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenApiConfig.class);
                    assertThat(context).hasBean("openAPIUnsecured");
                    assertThat(context).doesNotHaveBean("openAPISecured");
                    assertThat(context).doesNotHaveBean("addGlobalHeaderParam");
                });
    }

    @Test
    public void shouldLoadUnsecuredBean_whenSecurityIsDisabled() {
        contextRunner.withBean(ApplicationConfigProperties.class, () -> applicationConfig)
                .withUserConfiguration(OpenApiConfig.class)
                .withPropertyValues(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH + "=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenApiConfig.class);
                    assertThat(context).hasBean("openAPIUnsecured");
                    assertThat(context).doesNotHaveBean("openAPISecured");
                    assertThat(context).doesNotHaveBean("addGlobalHeaderParam");
                });
    }

}
