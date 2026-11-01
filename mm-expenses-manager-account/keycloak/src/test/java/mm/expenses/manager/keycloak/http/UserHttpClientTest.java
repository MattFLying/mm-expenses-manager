package mm.expenses.manager.keycloak.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.keycloak.InternalProperties;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import mm.expenses.manager.keycloak.model.UserRegisteredModel;
import mm.expenses.manager.keycloak.model.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.UserModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserHttpClientTest {

    public static final String TEST_HTTP_URL = "http://account-service";

    private static final String EVENT_ID = UUID.randomUUID().toString();
    private static final UUID USER_ID = UUID.randomUUID();

    @Mock
    private ObjectMapper mapper;

    @Mock
    private InternalProperties internalProperties;

    @Mock
    private HttpClient httpClient;

    @Mock
    private Event event;

    @Mock
    private UserModel user;

    @Mock
    private HttpResponse<String> httpResponse;

    private UserHttpClient userHttpClient;

    @BeforeEach
    public void setUp() {
        when(internalProperties.getHttpConnectionTimeoutInSeconds()).thenReturn(5);
        when(internalProperties.getAccountServiceUrl()).thenReturn(TEST_HTTP_URL);

        userHttpClient = new UserHttpClient(mapper, internalProperties);
        userHttpClient.httpClient = httpClient;
    }

    @Test
    public void registerUser_shouldRegisterUserSuccessfully() throws Exception {
        // given
        final var jsonPayload = "{\"userId\":\"" + USER_ID + "\"}";
        when(internalProperties.getOriginHeader()).thenReturn("test-origin");
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(mapper.writeValueAsString(any(UserRegisteredModel.class))).thenReturn(jsonPayload);

        final var futureResponse = CompletableFuture.completedFuture(httpResponse);
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(futureResponse);

        // when
        userHttpClient.registerUser(event, user);

        // then
        verify(httpClient).sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void registerUser_shouldHandleJsonProcessingExceptionDuringRegistration() throws Exception {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(mapper.writeValueAsString(any(UserRegisteredModel.class))).thenThrow(new JsonProcessingException("Mapping error") {
        });

        // when
        userHttpClient.registerUser(event, user);

        // then
        verify(httpClient, never()).sendAsync(any(), any());
    }

    @Test
    public void registerUser_shouldHandleGenericExceptionDuringRegistration() throws Exception {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(mapper.writeValueAsString(any(UserRegisteredModel.class))).thenThrow(new RuntimeException("Unexpected error"));

        // when
        userHttpClient.registerUser(event, user);

        // then
        verify(httpClient, never()).sendAsync(any(), any());
    }

    @Test
    public void getUser_shouldFetchUserSuccessfullyOnFirstAttempt() throws Exception {
        // given
        final var jsonResponseBody = "{\"id\":\"" + USER_ID + "\"}";
        final var expectedResponse = new UserResponse();

        when(internalProperties.getOriginHeader()).thenReturn("test-origin");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponseBody);
        when(mapper.readValue(jsonResponseBody, UserResponse.class)).thenReturn(expectedResponse);

        // when
        final var result = userHttpClient.getUser(USER_ID);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void getUser_shouldRetryAndFetchUserSuccessfully() throws Exception {
        // given
        final var jsonResponseBody = "{\"id\":\"" + USER_ID + "\"}";
        final var expectedResponse = new UserResponse();
        final var errorResponse = mock(HttpResponse.class);

        when(internalProperties.getOriginHeader()).thenReturn("test-origin");
        when(errorResponse.statusCode()).thenReturn(500);

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(errorResponse) // first attempt fail
                .thenReturn(httpResponse);  // second attempt success

        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn(jsonResponseBody);
        when(mapper.readValue(jsonResponseBody, UserResponse.class)).thenReturn(expectedResponse);

        // when
        final var result = userHttpClient.getUser(USER_ID);

        // then
        assertThat(result).isEqualTo(expectedResponse);
        verify(httpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void getUser_shouldThrowException_whenAllFetchAttemptsFail() throws Exception {
        // given
        final var errorResponse = mock(HttpResponse.class);

        when(internalProperties.getOriginHeader()).thenReturn("test-origin");
        when(errorResponse.statusCode()).thenReturn(404);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(errorResponse);

        // when & then
        assertThatThrownBy(() -> userHttpClient.getUser(USER_ID))
                .isInstanceOf(KeycloakSpiException.class);

        verify(httpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    public void shouldReturnPriority() {
        // when
        final var priority = userHttpClient.priority();

        // then
        assertThat(priority).isEqualTo(1);
    }

}
