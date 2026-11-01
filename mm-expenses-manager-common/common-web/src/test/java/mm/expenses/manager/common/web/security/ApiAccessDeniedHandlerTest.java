package mm.expenses.manager.common.web.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiAccessDeniedHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private SecurityContext securityContext;

    private ApiAccessDeniedHandler accessDeniedHandler;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        accessDeniedHandler = new ApiAccessDeniedHandler();
        responseWriter = new StringWriter();

        final var printWriter = new PrintWriter(responseWriter);

        when(response.getWriter()).thenReturn(printWriter);
    }

    @Test
    void shouldHandleAccessDeniedWithSecurityContext() throws Exception {
        // given
        final var exception = new AccessDeniedException("Access denied message");
        final var userId = UUID.randomUUID();
        final var keycloakId = UUID.randomUUID();

        when(request.getUserPrincipal()).thenReturn(securityContext);
        when(securityContext.getName()).thenReturn("test-user");
        when(securityContext.getUserId()).thenReturn(userId);
        when(securityContext.getUserIdFromKeycloak()).thenReturn(keycloakId);
        when(securityContext.getEmail()).thenReturn("user@example.com");
        when(securityContext.getParameters()).thenReturn(Map.of("paramKey", new String[]{"paramVal"}));
        when(securityContext.getHeaders()).thenReturn(Map.of("headerKey", "headerVal"));

        when(request.getParameterMap()).thenReturn(Map.of("reqParam", new String[]{"val"}));
        when(request.getRequestURI()).thenReturn("/api/test-uri");

        // when
        accessDeniedHandler.handle(request, response, exception);

        // then
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

        final var responseBody = responseWriter.toString();
        assertThat(responseBody).isNotBlank();
        assertThat(responseBody).contains("api-access-denied");
        assertThat(responseBody).contains("Access Denied");
    }

    @Test
    void shouldHandleAccessDeniedWithStandardPrincipal() throws Exception {
        // given
        final var exception = new AccessDeniedException("Access denied message");
        java.security.Principal standardPrincipal = () -> "standard-user-name";

        when(request.getUserPrincipal()).thenReturn(standardPrincipal);
        when(request.getParameterMap()).thenReturn(Map.of());
        when(request.getRequestURI()).thenReturn("/api/secure");

        // when
        accessDeniedHandler.handle(request, response, exception);

        // then
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

        final var responseBody = responseWriter.toString();
        assertThat(responseBody).contains("api-access-denied");
    }

    @Test
    void shouldHandleAccessDenied_whenPrincipalIsNull() throws Exception {
        // given
        final var exception = new AccessDeniedException("Unauthorized access");

        when(request.getUserPrincipal()).thenReturn(null);
        when(request.getParameterMap()).thenReturn(Map.of());
        when(request.getRequestURI()).thenReturn("/api/public");

        // when
        accessDeniedHandler.handle(request, response, exception);

        // then
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

        final var responseBody = responseWriter.toString();
        assertThat(responseBody).contains("api-access-denied");
        assertThat(responseBody).contains("Access Denied");
    }

}
