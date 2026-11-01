package mm.expenses.manager.keycloak.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.keycloak.InternalProperties;
import mm.expenses.manager.keycloak.request.RequestSender;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import mm.expenses.manager.keycloak.exception.KeycloakSpiExceptionMessage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Request sender responsible for sending via HTTP
 */
@Slf4j
abstract class InternalHttpClient implements RequestSender {

    private static final int ERROR_STATUS_CODE = 400;

    protected ObjectMapper mapper;
    protected HttpClient httpClient;
    protected InternalProperties internalProperties;

    @Override
    public boolean isAvailable() {
        return Objects.nonNull(httpClient);
    }

    protected InternalHttpClient(final ObjectMapper mapper, final InternalProperties internalProperties) {
        this.mapper = mapper;
        this.internalProperties = internalProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(this.internalProperties.getHttpConnectionTimeoutInSeconds()))
                .build();
    }

    /**
     * Sends the given request asynchronously.
     *
     * @param request the request object with details about URL path, headers etc.
     */
    protected CompletableFuture<HttpResponse<String>> sendAsync(final HttpRequest request) {
        try {
            if (!isAvailable()) {
                return null;
            }

            if (Objects.isNull(request)) {
                log.error("[KEYCLOAK-SPI] Http request cannot be sent because is null");
                throw new KeycloakSpiException(KeycloakSpiExceptionMessage.HTTP_REQUEST_MISSING);
            }

            log.info("[KEYCLOAK-SPI] Sending HTTP request to URI: {} {}, HEADERS: {}", request.method(), request.uri(), request.headers());
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .whenCompleteAsync((response, throwable) -> {
                        // every response with HTTP status higher than 400 is threatened as error and to avoid
                        // issues it has to be skipped any other processing here
                        if (response.statusCode() >= ERROR_STATUS_CODE) {
                            if (Objects.nonNull(throwable)) {
                                log.error("[KEYCLOAK-SPI] Error sending HTTP request: {}", throwable.getMessage(), throwable);
                            } else {
                                log.error("[KEYCLOAK-SPI] Error sending HTTP request: {}", request);
                            }
                        }
                    });
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Unknown error occurred during sending HTTP request: {}, Message: {}", request, exception.getMessage(), exception);
            throw new KeycloakSpiException(KeycloakSpiExceptionMessage.UNKNOWN_ERROR, exception);
        }
    }

    /**
     * Sends the given request synchronously.
     *
     * @param request the request object with details about URL path, headers etc.
     */
    protected HttpResponse<String> send(final HttpRequest request) {
        try {
            if (!isAvailable()) {
                return null;
            }

            if (Objects.isNull(request)) {
                log.error("[KEYCLOAK-SPI] Http request cannot be sent because is null");
                throw new KeycloakSpiException(KeycloakSpiExceptionMessage.HTTP_REQUEST_MISSING);
            }

            log.info("[KEYCLOAK-SPI] Sending HTTP request to URI: {} {}, HEADERS: {}", request.method(), request.uri(), request.headers());
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Unknown error occurred during sending HTTP request: {}, Message: {}", request, exception.getMessage(), exception);
            throw new KeycloakSpiException(KeycloakSpiExceptionMessage.UNKNOWN_ERROR, exception);
        }
    }

    /**
     * Builds Http request to be sent via {@link HttpClient}.
     *
     * @param path - url path
     * @return http request builder with commonly prepared object to be extended with expected details
     */
    protected HttpRequest.Builder buildRequest(final String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(path))
                .header("Accept", "*/*")
                .header("Origin", this.internalProperties.getOriginHeader())
                .timeout(Duration.ofSeconds(this.internalProperties.getHttpConnectionTimeoutInSeconds()));
    }

}
