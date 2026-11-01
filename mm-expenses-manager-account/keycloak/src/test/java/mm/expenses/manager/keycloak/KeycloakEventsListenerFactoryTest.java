package mm.expenses.manager.keycloak;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSessionFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
public class KeycloakEventsListenerFactoryTest {

    @Mock
    private KeycloakSessionFactory keycloakSessionFactory;

    @Mock
    private Config.Scope configScope;

    private KeycloakEventsListenerFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new KeycloakEventsListenerFactory();
    }

    @Test
    public void shouldReturnCorrectId() {
        // when
        final var providerId = factory.getId();

        // then
        assertThat(providerId).isEqualTo(KeycloakEventsListenerFactory.PROVIDER_ID);
    }

    @Test
    public void shouldInitializeFactorySuccessfully() {
        assertThatCode(() -> factory.init(configScope)).doesNotThrowAnyException();
    }

    @Test
    public void shouldExecuteLifecycleMethods() {
        // given
        factory.init(configScope);

        // when & then
        assertThatCode(() -> {
            factory.postInit(keycloakSessionFactory);
            factory.close();
        }).doesNotThrowAnyException();
    }

    @Test
    public void shouldCloseSafely_whenNotInitialized() {
        // given
        final var uninitializedFactory = new KeycloakEventsListenerFactory();

        // when & then
        assertThatCode(uninitializedFactory::close).doesNotThrowAnyException();
    }

}
