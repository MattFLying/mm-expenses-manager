package mm.expenses.manager.common.web.security.system;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.apache.commons.lang3.Strings;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Custom implementation to make the authorization decision for internal system or spi API calls.
 */
@Slf4j
public final class SystemAuthorizationDecision extends AuthorizationDecision {

    public SystemAuthorizationDecision(final Supplier<? extends Authentication> authentication, final RequestAuthorizationContext context) {
        super(makeSystemAuthorizationDecision(authentication, context));
    }

    private static boolean makeSystemAuthorizationDecision(final Supplier<? extends Authentication> authentication, final RequestAuthorizationContext context) {
        var isAllowed = false;
        if (Objects.isNull(authentication) || Objects.isNull(context)) {
            log.error("Cannot process system API call because of any of required objects is null: authentication=[{}], context=[{}]", authentication, context);
            return isAllowed;
        }

        if (Objects.nonNull(context.getRequest())) {
            final var originHeaderValue = context.getRequest().getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME);
            final var hasSpiHeader = hasOriginHeader(originHeaderValue, SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE);
            final var hasSystemHeader = hasOriginHeader(originHeaderValue, SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE);

            isAllowed = hasSpiHeader || hasSystemHeader;
        }
        return isAllowed;
    }

    private static boolean hasOriginHeader(final String originHeaderValue, final String expected) {
        return Strings.CI.equals(expected, originHeaderValue);
    }

}
