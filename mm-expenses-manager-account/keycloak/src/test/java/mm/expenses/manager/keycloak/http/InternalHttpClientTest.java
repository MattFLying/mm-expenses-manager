package mm.expenses.manager.keycloak.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.keycloak.InternalProperties;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InternalHttpClientTest {

    public static final String TEST_HTTP_URL = "http://localhost";

    @Mock
    private ObjectMapper mapper;

    @Mock
    private InternalProperties internalProperties;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    private TestInternalHttpClient client;

    @BeforeEach
    public void setUp() {
        when(internalProperties.getHttpConnectionTimeoutInSeconds()).thenReturn(5);

        client = new TestInternalHttpClient(mapper, internalProperties);
    }

    @Test
    public void shouldBeAvailable_whenHttpClientIsNotNull() {
        // given
        client.setHttpClient(httpClient);

        // when & then
        assertThat(client.isAvailable()).isTrue();
    }

    @Test
    public void shouldNotBeAvailable_whenHttpClientIsNull() {
        // given
        client.setHttpClient(null);

        // when & then
        assertThat(client.isAvailable()).isFalse();
    }

    @Test
    public void shouldReturnNull_whenSyncSendAndNotAvailable() {
        // given
        client.setHttpClient(null);
        final var request = HttpRequest.newBuilder().uri(URI.create(TEST_HTTP_URL)).build();

        // when
        final var response = client.send(request);

        // then
        assertThat(response).isNull();
    }

    @Test
    public void shouldThrowException_whenSyncRequestIsNull() {
        // given
        client.setHttpClient(httpClient);

        // when & then
        assertThatThrownBy(() -> client.send(null))
                .isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldSendRequestSynchronously() throws Exception {
        // given
        client.setHttpClient(httpClient);
        final var request = HttpRequest.newBuilder().uri(URI.create(TEST_HTTP_URL)).build();

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(httpResponse);

        // when
        final var response = client.send(request);

        // then
        assertThat(response).isEqualTo(httpResponse);
    }

    @Test
    public void shouldThrowException_whenSyncSendFails() throws Exception {
        // given
        client.setHttpClient(httpClient);
        final var request = HttpRequest.newBuilder().uri(URI.create(TEST_HTTP_URL)).build();

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        // when & then
        assertThatThrownBy(() -> client.send(request))
                .isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldReturnNull_whenAsyncSendAndNotAvailable() {
        // given
        client.setHttpClient(null);
        final var request = HttpRequest.newBuilder().uri(URI.create(TEST_HTTP_URL)).build();

        // when
        final var future = client.sendAsync(request);

        // then
        assertThat(future).isNull();
    }

    @Test
    public void shouldThrowException_whenAsyncRequestIsNull() {
        // given
        client.setHttpClient(httpClient);

        // when & then
        assertThatThrownBy(() -> client.sendAsync(null))
                .isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldSendRequestAsynchronously() throws Exception {
        // given
        client.setHttpClient(httpClient);
        final var request = HttpRequest.newBuilder().uri(URI.create(TEST_HTTP_URL)).build();

        final var futureResponse = CompletableFuture.completedFuture(httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(futureResponse);

        // when
        final var resultFuture = client.sendAsync(request);

        // then
        assertThat(resultFuture).isNotNull();
        assertThat(resultFuture.get()).isEqualTo(httpResponse);
    }

    @Test
    public void shouldLogAndHandleErrorStatusInAsyncSend() throws Exception {
        // given
        client.setHttpClient(httpClient);
        final var request = HttpRequest.newBuilder().uri(URI.create(TEST_HTTP_URL)).build();

        final var futureResponse = CompletableFuture.completedFuture(httpResponse);
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(futureResponse);

        // when
        final var resultFuture = client.sendAsync(request);

        // then
        assertThat(resultFuture).isNotNull();
        assertThat(resultFuture.get()).isEqualTo(httpResponse);
    }

    @Test
    public void shouldBuildHttpRequestCorrectly() {
        // given
        final var path = String.format("%s:8080/api/test", TEST_HTTP_URL);
        when(internalProperties.getOriginHeader()).thenReturn("test-origin");

        // when
        final var builder = client.buildRequest(path);
        final var request = builder.GET().build();

        // then
        assertThat(request).isNotNull();
        assertThat(request.uri().toString()).isEqualTo(path);
        assertThat(request.headers().firstValue("Accept")).hasValue("*/*");
        assertThat(request.headers().firstValue("Origin")).hasValue("test-origin");
        assertThat(request.timeout()).hasValue(Duration.ofSeconds(5));
    }

    private static class TestInternalHttpClient extends InternalHttpClient {

        public TestInternalHttpClient(final ObjectMapper mapper, final InternalProperties internalProperties) {
            super(mapper, internalProperties);
        }

        public void setHttpClient(final HttpClient httpClient) {
            this.httpClient = httpClient;
        }

    }

}
