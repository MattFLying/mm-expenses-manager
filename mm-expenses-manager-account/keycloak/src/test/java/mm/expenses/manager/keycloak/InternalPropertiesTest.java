package mm.expenses.manager.keycloak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class InternalPropertiesTest {

    private InternalProperties internalProperties;

    @BeforeEach
    public void setUp() {
        internalProperties = new InternalProperties();
    }

    @Test
    public void shouldCleanProperties() {
        // given
        internalProperties.setAccountServiceUrl("http://localhost");
        internalProperties.setOriginHeader("test");
        internalProperties.setKafkaBootstrapServer("kafka:9092");
        internalProperties.setHttpConnectionTimeoutInSeconds(10);

        // when
        internalProperties.clean();

        // then
        assertThat(internalProperties.getAccountServiceUrl()).isNull();
        assertThat(internalProperties.getOriginHeader()).isNull();
        assertThat(internalProperties.getKafkaBootstrapServer()).isNull();
        assertThat(internalProperties.getHttpConnectionTimeoutInSeconds()).isNull();
    }

    @Test
    public void shouldReturnDefaultValue_whenEnvironmentVariableIsMissing() {
        // given
        final var nonExistingVariable = "NON_EXISTING_ENV_VAR_12345";
        final var defaultValue = "default-value";

        // when
        final var result = internalProperties.getEnvironmentVariable(nonExistingVariable, defaultValue);

        // then
        assertThat(result).isEqualTo(defaultValue);
    }

}
