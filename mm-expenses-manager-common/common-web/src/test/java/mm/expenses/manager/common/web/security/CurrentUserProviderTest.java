package mm.expenses.manager.common.web.security;

import mm.expenses.manager.common.web.exception.security.ApiUnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class CurrentUserProviderTest {

    private final CurrentUserProvider currentUserProvider = new CurrentUserProvider();

    @Mock
    private SecurityContext securityContextMock;

    @Mock
    private Authentication genericAuthenticationMock;

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldSetAndGetCustomSecurityContext() {
        // when
        CurrentUserProvider.setSecurityContext(securityContextMock);
        final var resultContext = currentUserProvider.getSecurityContext();
        final var resultAuth = currentUserProvider.getCurrentAuthentication();

        // then
        assertThat(resultContext).isEqualTo(securityContextMock);
        assertThat(resultAuth).isEqualTo(securityContextMock);
    }

    @Test
    public void shouldSetSystemSecurityContext() {
        // when
        CurrentUserProvider.setSystemSecurityContext();
        final var auth = currentUserProvider.getCurrentAuthentication();

        // then
        assertThat(auth).isNotNull();
        assertThat(auth).isInstanceOf(SecurityContext.class);
    }

    @Test
    public void shouldSetSystemSPISecurityContext() {
        // when
        CurrentUserProvider.setSystemSPISecurityContext();
        final var auth = currentUserProvider.getCurrentAuthentication();

        // then
        assertThat(auth).isNotNull();
        assertThat(auth).isInstanceOf(SecurityContext.class);
    }

    @Test
    public void shouldSetUnsecuredContext() {
        // when
        CurrentUserProvider.setUnsecuredContext();
        final var auth = currentUserProvider.getCurrentAuthentication();

        // then
        assertThat(auth).isNotNull();
        assertThat(auth).isInstanceOf(SecurityContext.class);
    }

    @Test
    public void shouldThrowException_whenAuthenticationIsNull() {
        // when & then
        assertThatThrownBy(() -> currentUserProvider.getSecurityContext())
                .isInstanceOf(ApiUnauthorizedException.class);
    }

    @Test
    public void shouldThrowException_whenAuthenticationIsNotSecurityContextType() {
        // given
        SecurityContextHolder.getContext().setAuthentication(genericAuthenticationMock);

        // when & then
        assertThatThrownBy(() -> currentUserProvider.getSecurityContext())
                .isInstanceOf(ApiUnauthorizedException.class);
    }

}
