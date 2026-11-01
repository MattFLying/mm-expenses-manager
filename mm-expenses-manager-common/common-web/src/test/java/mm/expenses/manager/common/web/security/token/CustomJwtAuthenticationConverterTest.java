package mm.expenses.manager.common.web.security.token;

import jakarta.servlet.http.HttpServletRequest;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.utils.security.UserPermissions;
import mm.expenses.manager.common.utils.security.UserSimpleInterface;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomJwtAuthenticationConverterTest {

    @Mock
    private UserSimpleInterface userRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private UserSimpleInterface.SimpleUser simpleUser;

    private CustomJwtAuthenticationConverter converter;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CustomJwtAuthenticationConverter.class)
            .withBean(UserSimpleInterface.class, () -> userRepository)
            .withBean(HttpServletRequest.class, () -> httpServletRequest);

    @BeforeEach
    void setUp() {
        converter = new CustomJwtAuthenticationConverter(userRepository, httpServletRequest);
    }

    @EnumSource
    @ParameterizedTest
    void shouldConvertJwt_whenUserExists(final Permission permission) {
        // given
        final var keycloakUserId = UUID.randomUUID();
        final var internalUserId = UUID.randomUUID();

        final var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(keycloakUserId.toString())
                .build();

        when(userRepository.findByKeycloakId(keycloakUserId)).thenReturn(Optional.of(simpleUser));
        when(simpleUser.getId()).thenReturn(internalUserId);

        final var permissionsMap = new UserPermissions();
        permissionsMap.put(permission.getGroup().name(), Set.of(permission));
        when(simpleUser.getPermissions()).thenReturn(permissionsMap);

        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.enumeration(List.of("X-Test-Header")));
        when(httpServletRequest.getHeader("X-Test-Header")).thenReturn("HeaderValue");

        final var paramMap = Map.of("param1", new String[]{"val1"});
        when(httpServletRequest.getParameterMap()).thenReturn(paramMap);

        // when
        final var securityContext = converter.convert(jwt);

        // then
        assertThat(securityContext).isNotNull();
    }

    @Test
    void shouldConvertJwt_whenUserNotFound() {
        // given
        final var keycloakUserId = UUID.randomUUID();
        final var jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(keycloakUserId.toString())
                .build();

        when(userRepository.findByKeycloakId(keycloakUserId)).thenReturn(Optional.empty());
        when(httpServletRequest.getHeaderNames()).thenReturn(null);
        when(httpServletRequest.getParameterMap()).thenReturn(null);

        // when
        final var securityContext = converter.convert(jwt);

        // then
        assertThat(securityContext).isNotNull();
    }

    @Test
    void shouldLoadBean_whenSecurityEnabledIsTrue() {
        contextRunner.withPropertyValues(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH + "=true")
                .run(context -> assertThat(context).hasSingleBean(CustomJwtAuthenticationConverter.class));
    }

    @Test
    void shouldNotLoadBean_whenPropertyIsMissing() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(CustomJwtAuthenticationConverter.class));
    }

    @Test
    void shouldNotLoadBean_whenPropertyIsFalse() {
        contextRunner.withPropertyValues(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH + "=false")
                .run(context -> assertThat(context).doesNotHaveBean(CustomJwtAuthenticationConverter.class));
    }

}
