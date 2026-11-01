package mm.expenses.manager.common.web.security.system;

import jakarta.servlet.http.HttpServletRequest;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemAuthorizationDecisionTest {

    @Mock
    private Supplier<Authentication> authenticationSupplier;

    @Mock
    private RequestAuthorizationContext context;

    @Mock
    private HttpServletRequest request;

    @Test
    void shouldDeny_whenAuthenticationIsNull() {
        // given / when
        final var decision = new SystemAuthorizationDecision(null, context);

        // then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDeny_whenContextIsNull() {
        // given / when
        final var decision = new SystemAuthorizationDecision(authenticationSupplier, null);

        // then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDeny_whenAuthenticationAndContextAreNull() {
        // given / when
        final var decision = new SystemAuthorizationDecision(null, null);

        // then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldGrant_whenSpiHeaderMatches() {
        // given
        when(context.getRequest()).thenReturn(request);
        when(request.getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME)).thenReturn(SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE);

        // when
        final var decision = new SystemAuthorizationDecision(authenticationSupplier, context);

        // then
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void shouldGrant_whenSystemHeaderMatchesCaseInsensitively() {
        // given
        when(context.getRequest()).thenReturn(request);
        when(request.getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME)).thenReturn(SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE.toUpperCase());

        // when
        final var decision = new SystemAuthorizationDecision(authenticationSupplier, context);

        // then
        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void shouldDeny_whenHeaderIsInvalid() {
        // given
        when(context.getRequest()).thenReturn(request);
        when(request.getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME)).thenReturn("INVALID_ORIGIN");

        // when
        final var decision = new SystemAuthorizationDecision(authenticationSupplier, context);

        // then
        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void shouldDeny_whenHeaderIsNull() {
        // given
        when(context.getRequest()).thenReturn(request);
        when(request.getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME)).thenReturn(null);

        // when
        final var decision = new SystemAuthorizationDecision(authenticationSupplier, context);

        // then
        assertThat(decision.isGranted()).isFalse();
    }

}