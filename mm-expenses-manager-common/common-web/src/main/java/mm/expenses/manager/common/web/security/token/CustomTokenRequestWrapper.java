package mm.expenses.manager.common.web.security.token;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.Getter;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.apache.commons.lang3.StringUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;

/**
 * Wrapper for {@link HttpServletRequest} of user request with specific custom token header.
 * Overrides current request to simulate the Authorization header is present and the custom token is accessible.
 */
final class CustomTokenRequestWrapper extends HttpServletRequestWrapper {

    private final String formattedBearerToken;

    CustomTokenRequestWrapper(final HttpServletRequest request, final String customToken) {
        super(request);
        this.formattedBearerToken = formatBearerToken(customToken);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        final var names = Collections.list(super.getHeaderNames());

        // Authorization header is missing when custom token is available
        // so we need to add it to fake its presence
        if (!names.contains(SecurityRequestConfig.AUTHORIZATION)) {
            names.add(SecurityRequestConfig.AUTHORIZATION);
        }
        return Collections.enumeration(names);
    }

    @Override
    public String getHeader(final String name) {
        // if the expected header is Authorization
        // we need to return previously formatted bearer token
        if (hasAuthorizationHeader(name)) {
            return formattedBearerToken;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(final String name) {
        if (hasAuthorizationHeader(name)) {
            return Collections.enumeration(Collections.singletonList(formattedBearerToken));
        }
        return super.getHeaders(name);
    }

    /**
     * In case of custom token there is a special type of user principal.
     * Here we have access to the super principal and also to the formatted bearer token.
     */
    @Override
    public Principal getUserPrincipal() {
        return new UserPrincipalFromCustomToken(super.getUserPrincipal(), formattedBearerToken);
    }

    /**
     * Format custom token to bearer headers acceptable by spring-security.
     * If token is null or blank just return null value, otherwise
     * validate if token starts with 'bearer' word and trim it, but if 'bearer' is missing just add it at the beginning.
     */
    private String formatBearerToken(final String customToken) {
        if (StringUtils.isBlank(customToken)) {
            return null;
        }
        return customToken.toLowerCase().startsWith(SecurityRequestConfig.BEARER.toLowerCase())
                ? customToken.trim()
                : String.format("%s %s", SecurityRequestConfig.BEARER, customToken.trim());
    }

    private boolean hasAuthorizationHeader(final String name) {
        return SecurityRequestConfig.AUTHORIZATION.equalsIgnoreCase(name);
    }

    @Getter
    public static class UserPrincipalFromCustomToken implements Principal {

        private final Principal principal;
        private final String token;

        public UserPrincipalFromCustomToken(final Principal principal, final String token) {
            this.principal = principal;
            this.token = token;
        }

        @Override
        public String getName() {
            if (Objects.nonNull(principal)) {
                return principal.getName();
            }
            return null;
        }

    }

}
