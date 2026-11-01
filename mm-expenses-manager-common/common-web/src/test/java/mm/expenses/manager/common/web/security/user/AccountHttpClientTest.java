package mm.expenses.manager.common.web.security.user;

import mm.expenses.manager.common.utils.security.UserSimpleInterface;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountHttpClientTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private UserSimpleInterface.SimpleUser simpleUser;

    private AccountHttpClient accountHttpClient;

    @BeforeEach
    void setUp() {
        when(restClientBuilder.baseUrl(anyString())).thenReturn(restClientBuilder);
        when(restClientBuilder.defaultHeaders(any())).thenReturn(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);

        accountHttpClient = new AccountHttpClient(restClientBuilder, "http://localhost:8187");
    }

    @Test
    void shouldReturnUser_whenFound() {
        // given
        final var keycloakId = UUID.randomUUID();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(SecurityRequestConfig.API_INTERNAL_SYSTEM_USER_KEYCLOAK_PATH, keycloakId)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(UserSimpleInterface.SimpleUser.class)).thenReturn(simpleUser);

        // when
        final var result = accountHttpClient.findByKeycloakId(keycloakId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(simpleUser);
    }

    @Test
    void shouldReturnEmptyOptional_whenExceptionOccurs() {
        // given
        final var keycloakId = UUID.randomUUID();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(SecurityRequestConfig.API_INTERNAL_SYSTEM_USER_KEYCLOAK_PATH, keycloakId)).thenThrow(new RuntimeException("Connection error"));

        // when
        final var result = accountHttpClient.findByKeycloakId(keycloakId);

        // then
        assertThat(result).isEmpty();
    }

}
