package mm.expenses.manager.common.web.security.token;

import jakarta.servlet.http.HttpServletRequest;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.utils.security.UserSimpleInterface;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.SecurityContext;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Converter of JWT token to {@link SecurityContext} with expected additional information used
 * in context of HTTP request.
 * Besides basic information about the user like user id from the Keycloak also fetch
 * user id from the internal database with available permissions and other settings that can be
 * used later in specific use cases.
 */
@Component
@ConditionalOnProperty(name = SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, havingValue = "true")
public final class CustomJwtAuthenticationConverter implements Converter<Jwt, SecurityContext> {

    private final JwtGrantedAuthoritiesConverter defaultAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final UserSimpleInterface<?> userRepository;
    private final HttpServletRequest httpServletRequest;

    public CustomJwtAuthenticationConverter(final UserSimpleInterface<?> userRepository, final HttpServletRequest httpServletRequest) {
        this.userRepository = userRepository;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public SecurityContext convert(final Jwt jwt) {
        // get default authorities from JWT
        final var authorities = new HashSet<>(defaultAuthoritiesConverter.convert(jwt));

        final var headers = extractRequestHeaders();
        final var parameters = extractRequestParameters();

        // get user ID from Keycloak and map it to UUID
        final var userIdFromKeycloak = jwt.getSubject();
        final var userIdFromKeycloakUUID = UUID.fromString(userIdFromKeycloak);

        // find specific user in internal database by the Keycloak user id and map the internal user ID
        final var userOpt = userRepository.findByKeycloakId(userIdFromKeycloakUUID);
        final var userId = userOpt.map(UserSimpleInterface.SimpleUser::getId).orElse(null);

        final var permissions = new HashSet<Permission>();
        userOpt.ifPresent(user -> mapAuthorities(user, authorities, permissions));

        return SecurityContext.context(authorities, jwt, userIdFromKeycloakUUID, userId, headers, parameters, permissions);
    }

    /**
     * Updates the authorities list with additional permissions owned by the current user.
     */
    private void mapAuthorities(final UserSimpleInterface.SimpleUser user, final Set<GrantedAuthority> authorities, final Set<Permission> permissions) {
        user.getPermissions()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(permission -> {
                    if (Objects.nonNull(permission)) {
                        final var authority = new CustomGrantedAuthority(permission);

                        authorities.add(authority);
                        permissions.add(authority.getPermission());
                    }
                });
    }

    /**
     * Extract all available headers in the current request otherwise return empty map if there are no headers.
     */
    private HashMap<String, Object> extractRequestHeaders() {
        final var headers = new HashMap<String, Object>();

        final var headerNames = httpServletRequest.getHeaderNames();
        if (Objects.nonNull(headerNames)) {
            while (headerNames.hasMoreElements()) {
                final var name = headerNames.nextElement();
                final var value = httpServletRequest.getHeader(name);

                headers.put(name, value);
            }
        }
        return headers;
    }

    /**
     * Extract all available parameters in the current request
     */
    private Map<String, String[]> extractRequestParameters() {
        final var parameters = new HashMap<String, String[]>();

        final var requestParameters = httpServletRequest.getParameterMap();
        if (MapUtils.isNotEmpty(requestParameters)) {
            parameters.putAll(requestParameters);
        }
        return parameters;
    }

}
