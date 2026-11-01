package mm.expenses.manager.common.web.security;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.web.exception.security.ApiUnauthorizedException;
import mm.expenses.manager.common.web.exception.security.SecurityExceptionMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Current user provider if any is currently logged in.
 */
@Slf4j
@Component
public class CurrentUserProvider {

    public static void setSecurityContext(final SecurityContext securityContext) {
        SecurityContextHolder.getContext().setAuthentication(securityContext);
    }

    public static void setSystemSecurityContext() {
        setSecurityContext(SecurityContext.systemContext());
    }

    public static void setSystemSPISecurityContext() {
        setSecurityContext(SecurityContext.spiContext());
    }

    public static void setUnsecuredContext() {
        setSecurityContext(SecurityContext.unsecuredContext());
    }

    /**
     * Gets current authenticated user which may not be of the {@link SecurityContext} type.
     */
    public Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Gets current security context.
     */
    public SecurityContext getSecurityContext() {
        final var authentication = getCurrentAuthentication();

        if (Objects.nonNull(authentication) && authentication instanceof SecurityContext securityContext) {
            return securityContext;
        }

        log.error("User is not logged in or security context is of unknown type: {}", authentication);
        throw new ApiUnauthorizedException(SecurityExceptionMessage.UNAUTHORIZED_IN_CONTEXT);
    }

}
