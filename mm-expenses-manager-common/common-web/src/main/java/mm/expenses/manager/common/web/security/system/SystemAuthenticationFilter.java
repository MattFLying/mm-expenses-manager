package mm.expenses.manager.common.web.security.system;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.CurrentUserProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom implementation to filter request if it's a system and spi request to configure security context
 * if specific conditions are correct.
 */
public final class SystemAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final var wrappedRequest = new SystemRequestWrapper(request);
        final var originHeader = wrappedRequest.getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME);

        if (wrappedRequest.hasSystemOrigin(originHeader)) {
            // initialize system security context if the expected header is present in the spring security context
            CurrentUserProvider.setSystemSecurityContext();
        }

        if (wrappedRequest.hasSpiOrigin(originHeader)) {
            // initialize system-spi security context if the expected header is present in the spring security context
            CurrentUserProvider.setSystemSPISecurityContext();
        }

        filterChain.doFilter(wrappedRequest, response);
    }

}
