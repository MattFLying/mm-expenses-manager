package mm.expenses.manager.common.web.security.system;

import jakarta.servlet.FilterChain;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SystemAuthenticationFilterTest {

    private final SystemAuthenticationFilter filter = new SystemAuthenticationFilter();

    @Mock
    private FilterChain filterChain;

    @Test
    void shouldSetSystemSecurityContext_whenSystemHeaderIsPresent() throws Exception {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE);

        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(any(SystemRequestWrapper.class), eq(response));
    }

    @Test
    void shouldSetSpiSecurityContext_whenSpiHeaderIsPresent() throws Exception {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE);

        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(any(SystemRequestWrapper.class), eq(response));
    }

    @Test
    void shouldNotSetContext_whenHeaderIsInvalidOrMissing() throws Exception {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME, "INVALID_ORIGIN");

        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(any(SystemRequestWrapper.class), eq(response));
    }

    @Test
    void shouldHandleMissingHeader() throws Exception {
        // given
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(any(SystemRequestWrapper.class), eq(response));
    }

}