package mm.expenses.manager.common.web.security.unsecured;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.CurrentUserProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom implementation to filter request when the security is disabled and application should work
 * with unsecured mode.
 * Configures security context as unsecured to be accessible through the code.
 */
@Component
@ConditionalOnProperty(name = SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, havingValue = "false", matchIfMissing = true)
public final class UnsecuredAuthenticationFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
        final var httpRequest = (HttpServletRequest) request;
        final var wrappedRequest = new UnsecuredRequestWrapper(httpRequest);

        // configure unsecured security context as anonymous user
        CurrentUserProvider.setUnsecuredContext();

        filterChain.doFilter(wrappedRequest, response);
    }

}
