package mm.expenses.manager.common.web.security.unsecured;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import mm.expenses.manager.common.web.security.SecurityContext;

import java.security.Principal;

/**
 * Wrapper for {@link HttpServletRequest} of unsecured request.
 */
final class UnsecuredRequestWrapper extends HttpServletRequestWrapper {

    UnsecuredRequestWrapper(final HttpServletRequest request) {
        super(request);
    }

    @Override
    public Principal getUserPrincipal() {
        return SecurityContext.unsecuredContext();
    }

}
