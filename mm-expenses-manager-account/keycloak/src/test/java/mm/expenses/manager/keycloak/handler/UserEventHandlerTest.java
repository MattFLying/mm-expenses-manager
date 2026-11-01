package mm.expenses.manager.keycloak.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.keycloak.InternalProperties;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.*;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserEventHandlerTest {

    public static final String TEST_HTTP_URL = "http://account-service";

    private static final String EVENT_ID = UUID.randomUUID().toString();
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private KeycloakSession session;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaProducer<String, String> kafkaProducer;

    @Mock
    private InternalProperties internalProperties;

    @Mock
    private UserProvider userProvider;

    @Mock
    private KeycloakContext keycloakContext;

    @Mock
    private RealmModel realmModel;

    @Mock
    private Event event;

    @Mock
    private UserModel userModel;

    private UserEventHandler userEventHandler;

    @BeforeEach
    public void setUp() {
        when(internalProperties.getHttpConnectionTimeoutInSeconds()).thenReturn(5);
        when(internalProperties.getAccountServiceUrl()).thenReturn(TEST_HTTP_URL);

        userEventHandler = new UserEventHandler(session, objectMapper, kafkaProducer, internalProperties);
    }

    @Test
    public void login_shouldHandleLoginEventSuccessfully() {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getUserId()).thenReturn(USER_ID.toString());

        // when & then
        assertThatCode(() -> userEventHandler.login(event, EventType.LOGIN)).doesNotThrowAnyException();
    }

    @Test
    public void login_shouldHandleLoginErrorEventSuccessfully() {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getUserId()).thenReturn(USER_ID.toString());
        when(event.getError()).thenReturn("invalid_credentials");
        when(event.getDetails()).thenReturn(null);

        // when & then
        assertThatCode(() -> userEventHandler.login(event, EventType.LOGIN_ERROR)).doesNotThrowAnyException();
    }

    @Test
    public void logout_shouldHandleLogoutEventSuccessfully() {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getUserId()).thenReturn(USER_ID.toString());

        // when & then
        assertThatCode(() -> userEventHandler.logout(event, EventType.LOGOUT)).doesNotThrowAnyException();
    }

    @Test
    public void logout_shouldHandleLogoutErrorEventSuccessfully() {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getUserId()).thenReturn(USER_ID.toString());
        when(event.getError()).thenReturn("session_expired");

        // when & then
        assertThatCode(() -> userEventHandler.logout(event, EventType.LOGOUT_ERROR)).doesNotThrowAnyException();
    }

    @Test
    public void register_shouldHandleRegisterEventSuccessfully() {
        // given
        when(event.getUserId()).thenReturn(USER_ID.toString());
        when(session.users()).thenReturn(userProvider);
        when(session.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getRealm()).thenReturn(realmModel);
        when(userProvider.getUserById(realmModel, USER_ID.toString())).thenReturn(userModel);

        // when & then
        assertThatCode(() -> userEventHandler.register(event, EventType.REGISTER)).doesNotThrowAnyException();
    }

    @Test
    public void register_shouldThrowException_whenUserIdIsNullDuringRegistration() {
        // given
        when(event.getUserId()).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> userEventHandler.register(event, EventType.REGISTER)).isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void register_shouldHandleRegisterErrorEventSuccessfully() {
        // given
        when(event.getUserId()).thenReturn(USER_ID.toString());
        when(event.getError()).thenReturn("user_already_exists");

        when(session.users()).thenReturn(userProvider);
        when(session.getContext()).thenReturn(keycloakContext);
        when(keycloakContext.getRealm()).thenReturn(realmModel);
        when(userProvider.getUserById(realmModel, USER_ID.toString())).thenReturn(userModel);

        // when & then
        assertThatCode(() -> userEventHandler.register(event, EventType.REGISTER_ERROR)).doesNotThrowAnyException();
    }

    @Test
    public void resetPassword_shouldHandleResetPasswordEventSuccessfully() {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getUserId()).thenReturn(USER_ID.toString());

        // when & then
        assertThatCode(() -> userEventHandler.resetPassword(event, EventType.RESET_PASSWORD)).doesNotThrowAnyException();
    }

    @Test
    public void resetPassword_shouldHandleResetPasswordErrorEventSuccessfully() {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getUserId()).thenReturn(USER_ID.toString());
        when(event.getError()).thenReturn("password_too_weak");

        // when & then
        assertThatCode(() -> userEventHandler.resetPassword(event, EventType.RESET_PASSWORD_ERROR)).doesNotThrowAnyException();
        assertThatCode(() -> userEventHandler.resetPassword(event, EventType.RESET_PASSWORD_ERROR)).doesNotThrowAnyException();
    }

}
