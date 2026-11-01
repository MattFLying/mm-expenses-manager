package mm.expenses.manager.common.web.security;

import lombok.Getter;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

/**
 * Security context built during conversion from the JWT directly and with available additional details.
 */
@Getter
public final class SecurityContext extends AbstractAuthenticationToken {

    private static final String EMAIL = "email";
    private static final String USERNAME = "preferred_username";
    private static final String SYSTEM_EMAIL = "system@internal";
    private static final String SYSTEM_SPI_EMAIL = "system-spi@internal";

    // For unsecured mode anonymous user details are required
    private static final String ANON_EMAIL = "anon@internal";
    private static final String ANON_NAME = "anon";

    private final Jwt jwt;
    private final UUID userIdFromKeycloak;
    private final UUID userId;
    private final String email;
    private final String name;
    private final Map<String, Object> headers;
    private final Map<String, String[]> parameters;
    private final Set<Permission> permissions;
    private final boolean isSystemRequest;
    private final boolean isSpiRequest;
    private final SecurityMode securityMode;

    private SecurityContext(final Collection<? extends GrantedAuthority> authorities, final Jwt jwt,
                            final UUID userIdFromKeycloak, final UUID userId, final String email, final String name,
                            final Map<String, Object> headers, final Map<String, String[]> parameters, final Set<Permission> permissions,
                            final boolean isSystemRequest, final boolean isSpiRequest, final boolean shouldBeInSecuredMode) {
        super(authorities);

        this.jwt = jwt;
        this.userIdFromKeycloak = userIdFromKeycloak;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.headers = headers;
        this.parameters = parameters;
        this.permissions = permissions;
        this.isSystemRequest = isSystemRequest;
        this.isSpiRequest = isSpiRequest;
        this.securityMode = shouldBeInSecuredMode ? SecurityMode.SECURED : SecurityMode.UNSECURED;
    }

    /**
     * Builds security context for authenticated user based on the token.
     */
    public static SecurityContext context(final Collection<? extends GrantedAuthority> authorities, final Jwt token,
                                          final UUID userIdFromKeycloak, final UUID userId,
                                          final Map<String, Object> headers, final Map<String, String[]> parameters, final Set<Permission> permissions) {
        final var email = Optional.ofNullable(token)
                .map(jwt -> jwt.getClaimAsString(EMAIL))
                .orElse(null);
        final var name = Optional.ofNullable(token)
                .map(jwt -> jwt.getClaimAsString(USERNAME))
                .orElse("name");

        final var context = new SecurityContext(
                authorities,
                token, userIdFromKeycloak,
                userId,
                email, name,
                headers, parameters, permissions,
                false, false, true
        );
        context.setAuthenticated(true);

        return context;
    }

    /**
     * Builds security context for system usage.
     */
    public static SecurityContext systemContext() {
        final var uniqueRequestId = UUID.randomUUID();
        final var systemName = String.format("%s.%s", SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE, uniqueRequestId);

        final var context = new SecurityContext(
                List.of(),
                null, null,
                uniqueRequestId,
                SYSTEM_EMAIL, systemName,
                Map.of(), Map.of(), Set.of(),
                true, false, true
        );
        context.setAuthenticated(true);

        return context;
    }

    /**
     * Builds security context for system-spi usage.
     */
    public static SecurityContext spiContext() {
        final var uniqueRequestId = UUID.randomUUID();
        final var systemName = String.format("%s.%s", SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE, uniqueRequestId);

        final var context = new SecurityContext(
                List.of(),
                null, null,
                uniqueRequestId,
                SYSTEM_SPI_EMAIL, systemName,
                Map.of(), Map.of(), Set.of(),
                true, true, true
        );
        context.setAuthenticated(true);

        return context;
    }

    /**
     * Builds security context for unsecured mode with anonymous user.
     */
    public static SecurityContext unsecuredContext() {
        final var uniqueRequestId = UUID.randomUUID();
        final var systemName = String.format("%s.%s", ANON_NAME, uniqueRequestId);

        final var context = new SecurityContext(
                List.of(),
                null, null,
                uniqueRequestId,
                ANON_EMAIL, systemName,
                Map.of(), Map.of(), Set.of(),
                false, false, false
        );
        context.setAuthenticated(true);

        return context;
    }

    @Override
    public Object getCredentials() {
        return Optional.ofNullable(this.jwt)
                .map(AbstractOAuth2Token::getTokenValue)
                .orElse(null);
    }

    @Override
    public Object getPrincipal() {
        return this.userIdFromKeycloak;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
