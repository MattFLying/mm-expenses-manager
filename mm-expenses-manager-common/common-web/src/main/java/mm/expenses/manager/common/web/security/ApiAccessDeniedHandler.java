package mm.expenses.manager.common.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.config.ObjectMapperConfig;
import mm.expenses.manager.common.web.exception.ExceptionMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Access denied handler for application's APIs.
 * When user has no permission to access API it has to be handled specifically, and it's done here.
 */
@Slf4j
final class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private static final String ACCESS_DENIED_MESSAGE = "Access Denied";
    private static final String ACCESS_DENIED_CODE = "api-access-denied";
    private static final HttpStatus FORBIDDEN_STATUS = HttpStatus.FORBIDDEN;

    private final ObjectMapper objectMapper = ObjectMapperConfig.objectMapper();

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response, final AccessDeniedException accessDeniedException) throws IOException {
        var message = accessDeniedException.getMessage();
        try {
            final var loggedUser = extractLoggedUser(request);
            message = String.format("Forbidden access denied for: %s", objectMapper.writeValueAsString(loggedUser));
        } catch (final Exception exception) {
            message = "Forbidden access denied";
            log.error("Unknown error occurred during access denied handling", exception);
        } finally {
            log.error(message, accessDeniedException);
            response.setStatus(FORBIDDEN_STATUS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(ExceptionMessage.of(ACCESS_DENIED_CODE, ACCESS_DENIED_MESSAGE, FORBIDDEN_STATUS)));
        }
    }

    /**
     * Extract currently logged-in user directly from the request if it is possible otherwise just collect
     * the common details to be logged.
     */
    private LoggedUser extractLoggedUser(final HttpServletRequest request) {
        final var loggedUser = new LoggedUser();

        final var user = request.getUserPrincipal();
        if (Objects.nonNull(user)) {
            if (user instanceof SecurityContext securityContext) {
                loggedUser.setUserName(securityContext.getName());
                loggedUser.setUserId(securityContext.getUserId());
                loggedUser.setUserKeycloakId(securityContext.getUserIdFromKeycloak());
                loggedUser.setEmail(securityContext.getEmail());
                loggedUser.setParameters(securityContext.getParameters());
                loggedUser.setHeaders(securityContext.getHeaders());
            } else {
                loggedUser.setUserName(user.getName());
            }
        }

        loggedUser.setParameters(request.getParameterMap());
        loggedUser.setRequestUri(request.getRequestURI());

        return loggedUser;
    }

    /**
     * Simple representation of currently logged-in user to be logged with specific details.
     */
    @Setter
    @Getter
    @NoArgsConstructor
    private static class LoggedUser {
        private String userName;
        private String userId;
        private String userKeycloakId;
        private String email;
        private Map<String, String[]> parameters;
        private Map<String, Object> headers;
        private String requestUri;

        public void setUserId(final UUID userId) {
            assign(userId, value -> this.userId = value.toString());
        }

        public void setUserKeycloakId(final UUID userKeycloakId) {
            assign(userKeycloakId, value -> this.userKeycloakId = value.toString());
        }

        private void assign(final UUID expectedValue, final Consumer<UUID> consumer) {
            if (Objects.nonNull(expectedValue)) {
                consumer.accept(expectedValue);
            }
        }

    }

}
