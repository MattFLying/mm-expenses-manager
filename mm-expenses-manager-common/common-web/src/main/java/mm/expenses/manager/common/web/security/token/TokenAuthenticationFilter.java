package mm.expenses.manager.common.web.security.token;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * REST API has two possible options to be used: with classic authentication header, or with custom token
 * used in the request. If the custom token has been used special filtering is executed to
 * process the custom token. Otherwise, classic processing of the authentication header will be processed.
 * <p>
 * This filter must be used first in whole application to detect if there is a custom token used and to
 * adapt the request to be properly used, otherwise just work in standard manner.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, havingValue = "true")
public final class TokenAuthenticationFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final var httpRequest = (HttpServletRequest) request;

        final var customToken = getCustomToken(httpRequest);
        if (StringUtils.isNotBlank(customToken)) {
            final var wrappedRequest = new CustomTokenRequestWrapper(httpRequest, customToken);

            chain.doFilter(wrappedRequest, response);
            return;
        }
        chain.doFilter(httpRequest, response);
    }

    /**
     * Gets custom token header safe for swagger API which can be used without log in to the application
     * if the token is already taken and available.
     */
    private String getCustomToken(final HttpServletRequest httpRequest) {
        if (Objects.isNull(httpRequest)) {
            return null;
        }
        return httpRequest.getHeader(SecurityRequestConfig.TOKEN);
    }

}
