package mm.expenses.manager.common.web.security;

import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SecurityContextTest {

    @Mock
    private Jwt jwtToken;

    @Mock
    private GrantedAuthority authority;

    @Test
    public void shouldCreateStandardUserContext() {
        // given
        final var keycloakId = UUID.randomUUID();
        final var userId = UUID.randomUUID();
        final var authorities = List.of(authority);
        Map<String, Object> headers = Map.of("header-key", "header-value");
        final var parameters = Map.of("param-key", new String[]{"param-val"});
        final var permissions = new HashSet<Permission>();

        when(jwtToken.getClaimAsString("email")).thenReturn("user@example.com");
        when(jwtToken.getClaimAsString("preferred_username")).thenReturn("john_doe");
        when(jwtToken.getTokenValue()).thenReturn("token-value-123");

        // when
        final var context = SecurityContext.context(
                authorities, jwtToken, keycloakId, userId, headers, parameters, permissions
        );

        // then
        assertThat(context).isNotNull();
        assertThat(context.isAuthenticated()).isTrue();
        assertThat(context.getJwt()).isEqualTo(jwtToken);
        assertThat(context.getUserIdFromKeycloak()).isEqualTo(keycloakId);
        assertThat(context.getUserId()).isEqualTo(userId);
        assertThat(context.getEmail()).isEqualTo("user@example.com");
        assertThat(context.getName()).isEqualTo("john_doe");
        assertThat(context.getHeaders()).isEqualTo(headers);
        assertThat(context.getParameters()).isEqualTo(parameters);
        assertThat(context.getPermissions()).isEqualTo(permissions);
        assertThat(context.isSystemRequest()).isFalse();
        assertThat(context.isSpiRequest()).isFalse();
        assertThat(context.getSecurityMode()).isEqualTo(SecurityMode.SECURED);
        assertThat(context.getCredentials()).isEqualTo("token-value-123");
        assertThat(context.getPrincipal()).isEqualTo(keycloakId);
    }

    @Test
    public void shouldFallbackToDefaults_whenJwtClaimsAreMissing() {
        // given
        when(jwtToken.getClaimAsString("email")).thenReturn(null);
        when(jwtToken.getClaimAsString("preferred_username")).thenReturn(null);
        when(jwtToken.getTokenValue()).thenReturn(null);

        // when
        final var context = SecurityContext.context(
                List.of(), jwtToken, null, null, Map.of(), Map.of(), Set.of()
        );

        // then
        assertThat(context.getEmail()).isNull();
        assertThat(context.getName()).isEqualTo("name");
        assertThat(context.getCredentials()).isNull();
    }

    @Test
    public void shouldCreateSystemContext() {
        // when
        final var context = SecurityContext.systemContext();

        // then
        assertThat(context).isNotNull();
        assertThat(context.isAuthenticated()).isTrue();
        assertThat(context.getEmail()).isEqualTo("system@internal");
        assertThat(context.getName()).startsWith(SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE);
        assertThat(context.isSystemRequest()).isTrue();
        assertThat(context.isSpiRequest()).isFalse();
        assertThat(context.getSecurityMode()).isEqualTo(SecurityMode.SECURED);
        assertThat(context.getJwt()).isNull();
        assertThat(context.getCredentials()).isNull();
        assertThat(context.getPrincipal()).isNull();
    }

    @Test
    public void shouldCreateSpiContext() {
        // when
        final var context = SecurityContext.spiContext();

        // then
        assertThat(context).isNotNull();
        assertThat(context.isAuthenticated()).isTrue();
        assertThat(context.getEmail()).isEqualTo("system-spi@internal");
        assertThat(context.getName()).startsWith(SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE);
        assertThat(context.isSystemRequest()).isTrue();
        assertThat(context.isSpiRequest()).isTrue();
        assertThat(context.getSecurityMode()).isEqualTo(SecurityMode.SECURED);
        assertThat(context.getJwt()).isNull();
        assertThat(context.getCredentials()).isNull();
        assertThat(context.getPrincipal()).isNull();
    }

    @Test
    public void shouldCreateUnsecuredContext() {
        // when
        final var context = SecurityContext.unsecuredContext();

        // then
        assertThat(context).isNotNull();
        assertThat(context.isAuthenticated()).isTrue();
        assertThat(context.getEmail()).isEqualTo("anon@internal");
        assertThat(context.getName()).startsWith("anon");
        assertThat(context.isSystemRequest()).isFalse();
        assertThat(context.isSpiRequest()).isFalse();
        assertThat(context.getSecurityMode()).isEqualTo(SecurityMode.UNSECURED);
        assertThat(context.getJwt()).isNull();
        assertThat(context.getCredentials()).isNull();
        assertThat(context.getPrincipal()).isNull();
    }

}
