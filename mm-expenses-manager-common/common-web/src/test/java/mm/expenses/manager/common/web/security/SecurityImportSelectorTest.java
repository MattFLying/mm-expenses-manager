package mm.expenses.manager.common.web.security;

import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.token.CustomJwtAuthenticationConverter;
import mm.expenses.manager.common.web.security.token.TokenAuthenticationFilter;
import mm.expenses.manager.common.web.security.unsecured.UnsecuredAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityImportSelectorTest {

    @Mock
    private Environment environment;

    @Mock
    private AnnotationMetadata importingClassMetadata;

    private SecurityImportSelector importSelector;

    @BeforeEach
    void setUp() {
        importSelector = new SecurityImportSelector();
        importSelector.setEnvironment(environment);
    }

    @Test
    void shouldSelectSecuredImports_whenSecurityIsEnabled() {
        // given
        when(environment.getProperty(eq(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH), eq(Boolean.class), eq(false))).thenReturn(true);

        // when
        final var imports = importSelector.selectImports(importingClassMetadata);

        // then
        assertThat(imports).isNotNull();
        assertThat(imports).containsExactlyInAnyOrder(
                CurrentUserProvider.class.getName(),
                ResourceServerSecuredConfig.class.getName(),
                TokenAuthenticationFilter.class.getName(),
                CustomJwtAuthenticationConverter.class.getName()
        );
    }

    @Test
    void shouldSelectUnsecuredImports_whenSecurityIsDisabled() {
        // given
        when(environment.getProperty(eq(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH), eq(Boolean.class), eq(false))).thenReturn(false);

        // when
        final var imports = importSelector.selectImports(importingClassMetadata);

        // then
        assertThat(imports).isNotNull();
        assertThat(imports).containsExactlyInAnyOrder(
                CurrentUserProvider.class.getName(),
                ResourceServerUnsecuredConfig.class.getName(),
                UnsecuredAuthenticationFilter.class.getName()
        );
    }

    @Test
    void shouldSelectUnsecuredImportsByDefault_whenPropertyIsMissing() {
        // given
        when(environment.getProperty(eq(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH), eq(Boolean.class), eq(false))).thenReturn(false);

        // when
        final var imports = importSelector.selectImports(importingClassMetadata);

        // then
        assertThat(imports).isNotNull();
        assertThat(imports).containsExactlyInAnyOrder(
                CurrentUserProvider.class.getName(),
                ResourceServerUnsecuredConfig.class.getName(),
                UnsecuredAuthenticationFilter.class.getName()
        );
    }

}
