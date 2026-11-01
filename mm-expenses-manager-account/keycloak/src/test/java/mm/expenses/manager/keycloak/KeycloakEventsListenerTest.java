package mm.expenses.manager.keycloak;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KeycloakEventsListenerTest {

    public static final String TEST_HTTP_URL = "http://account-service";

    private static final String EVENT_ID = UUID.randomUUID().toString();
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private KeycloakSession keycloakSession;

    @Mock
    private KafkaProducer<String, String> kafkaProducer;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private InternalProperties internalProperties;

    @Mock
    private Event event;

    @Mock
    private AdminEvent adminEvent;

    private KeycloakEventsListener eventsListener;

    @BeforeEach
    public void setUp() {
        when(internalProperties.getHttpConnectionTimeoutInSeconds()).thenReturn(5);
        when(internalProperties.getAccountServiceUrl()).thenReturn(TEST_HTTP_URL);

        eventsListener = new KeycloakEventsListener(keycloakSession, kafkaProducer, objectMapper, internalProperties);
    }

    @Test
    public void shouldDoNothing_whenEventIsNull() {
        assertThatCode(() -> eventsListener.onEvent(null)).doesNotThrowAnyException();
    }

    @Test
    public void shouldHandleLoginEvent() throws Exception {
        // given
        when(event.getType()).thenReturn(EventType.LOGIN);
        when(event.getUserId()).thenReturn(USER_ID.toString());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"type\":\"LOGIN\"}");

        var userProvider = mock(UserProvider.class);
        var keycloakContext = mock(KeycloakContext.class);
        var realmModel = mock(RealmModel.class);
        var userModel = mock(UserModel.class);

        when(keycloakSession.users()).thenReturn(userProvider);
        when(keycloakSession.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getRealm()).thenReturn(realmModel);
        when(userProvider.getUserById(realmModel, USER_ID.toString())).thenReturn(userModel);

        // when & then
        assertThatCode(() -> eventsListener.onEvent(event)).doesNotThrowAnyException();
    }

    @Test
    public void shouldHandleLogoutEvent() throws Exception {
        // given
        when(event.getType()).thenReturn(EventType.LOGOUT);
        when(event.getUserId()).thenReturn(USER_ID.toString());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"type\":\"LOGOUT\"}");

        var userProvider = mock(UserProvider.class);
        var keycloakContext = mock(KeycloakContext.class);
        var realmModel = mock(RealmModel.class);
        var userModel = mock(UserModel.class);

        when(keycloakSession.users()).thenReturn(userProvider);
        when(keycloakSession.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getRealm()).thenReturn(realmModel);
        when(userProvider.getUserById(realmModel, USER_ID.toString())).thenReturn(userModel);

        // when & then
        assertThatCode(() -> eventsListener.onEvent(event)).doesNotThrowAnyException();
    }

    @Test
    public void shouldHandleRegisterEvent() throws Exception {
        // given
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(event.getUserId()).thenReturn(USER_ID.toString());
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"type\":\"REGISTER\"}");

        var userProvider = mock(UserProvider.class);
        var keycloakContext = mock(KeycloakContext.class);
        var realmModel = mock(RealmModel.class);
        var userModel = mock(UserModel.class);

        when(keycloakSession.users()).thenReturn(userProvider);
        when(keycloakSession.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getRealm()).thenReturn(realmModel);
        when(userProvider.getUserById(realmModel, USER_ID.toString())).thenReturn(userModel);

        // when & then
        assertThatCode(() -> eventsListener.onEvent(event)).doesNotThrowAnyException();
    }

    @Test
    public void shouldHandleResetPasswordEvent() throws Exception {
        // given
        when(event.getType()).thenReturn(EventType.RESET_PASSWORD);
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"type\":\"RESET_PASSWORD\"}");

        // when & then
        assertThatCode(() -> eventsListener.onEvent(event)).doesNotThrowAnyException();
    }

    @Test
    public void shouldHandleEventWithNullType() throws Exception {
        // given
        when(event.getType()).thenReturn(null);
        when(objectMapper.writeValueAsString(event)).thenReturn("{}");

        // when & then
        assertThatCode(() -> eventsListener.onEvent(event)).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowException_whenJsonMappingFails() throws Exception {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(objectMapper.writeValueAsString(event)).thenThrow(mock(JsonMappingException.class));

        // when & then
        assertThatThrownBy(() -> eventsListener.onEvent(event)).isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldRethrowKeycloakSpiException() throws Exception {
        // given
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(event.getUserId()).thenReturn(null);
        when(objectMapper.writeValueAsString(event)).thenReturn("{}");

        // when & then
        assertThatThrownBy(() -> eventsListener.onEvent(event)).isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldThrowException_whenGenericErrorOccurs() throws Exception {
        // given
        when(objectMapper.writeValueAsString(event)).thenThrow(new RuntimeException("Unexpected error"));

        // when & then
        assertThatThrownBy(() -> eventsListener.onEvent(event)).isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldExecuteEmptyMethods() {
        assertThatCode(() -> {
            eventsListener.onEvent(adminEvent, true);
            eventsListener.close();
        }).doesNotThrowAnyException();
    }

    @Test
    public void shouldIgnoreUnsupportedEventType() throws Exception {
        // given
        when(event.getType()).thenReturn(EventType.IMPERSONATE);
        when(objectMapper.writeValueAsString(event)).thenReturn("{\"type\":\"IMPERSONATE\"}");

        // when & then
        assertThatCode(() -> eventsListener.onEvent(event)).doesNotThrowAnyException();

        verify(keycloakSession, never()).users();
    }

}
