package mm.expenses.manager.common.web.security.token;

import jakarta.servlet.http.HttpServletRequest;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.util.Objects;

/**
 * Custom bearer token resolver to get the token even if the 'Bearer ' is missing.
 */
public final class CustomBearerTokenResolver implements BearerTokenResolver {

    private static final String BEARER_TOKEN_STARTS_WITH = String.format("%s ", SecurityRequestConfig.BEARER).toLowerCase();
    private static final int BEARER_TOKEN_STARTS_WITH_LENGTH = BEARER_TOKEN_STARTS_WITH.length();

    @Override
    public String resolve(final HttpServletRequest request) {
        if (Objects.isNull(request)) {
            return null;
        }

        final var authorizationHeader = request.getHeader(SecurityRequestConfig.AUTHORIZATION);
        if (StringUtils.isBlank(authorizationHeader)) {
            return null;
        }

        // if header starts with 'Bearer ' just cut it off (case-sensitive does not matter)
        if (authorizationHeader.toLowerCase().startsWith(BEARER_TOKEN_STARTS_WITH)) {
            return authorizationHeader.substring(BEARER_TOKEN_STARTS_WITH_LENGTH).trim();
        }

        // if only token has been passed just return it
        return authorizationHeader.trim();
    }

}
