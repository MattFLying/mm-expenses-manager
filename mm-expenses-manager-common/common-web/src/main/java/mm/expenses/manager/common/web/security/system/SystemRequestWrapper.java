package mm.expenses.manager.common.web.security.system;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.SecurityContext;

import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Wrapper for {@link HttpServletRequest} of system and spi request.
 */
final class SystemRequestWrapper extends HttpServletRequestWrapper {

    SystemRequestWrapper(final HttpServletRequest request) {
        super(request);
    }

    @Override
    public Principal getUserPrincipal() {
        return SecurityContext.systemContext();
    }

    @Override
    public String getHeader(final String name) {
        final var headerValue = super.getHeader(name);

        if (hasOriginHeader(name)) {
            if (hasSpiOrigin(headerValue)) {
                return SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE;
            }
            if (hasSystemOrigin(headerValue)) {
                return SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE;
            }
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(final String name) {
        if (hasOriginHeader(name)) {
            return Collections.enumeration(List.of(getHeader(name)));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        final var names = Collections.list(super.getHeaderNames());
        if (names.stream().noneMatch(this::hasOriginHeader)) {
            names.add(SecurityRequestConfig.HEADER_ORIGIN_NAME);
        }
        return Collections.enumeration(names);
    }

    boolean hasOriginHeader(final String name) {
        return SecurityRequestConfig.HEADER_ORIGIN_NAME.equalsIgnoreCase(name);
    }

    boolean hasSpiOrigin(final String headerValue) {
        return SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE.equalsIgnoreCase(headerValue);
    }

    boolean hasSystemOrigin(final String headerValue) {
        return SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE.equalsIgnoreCase(headerValue);
    }

}
