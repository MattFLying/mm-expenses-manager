package mm.expenses.manager.common.web.security;

import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.token.CustomJwtAuthenticationConverter;
import mm.expenses.manager.common.web.security.token.TokenAuthenticationFilter;
import mm.expenses.manager.common.web.security.unsecured.UnsecuredAuthenticationFilter;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Defines all required imports for security related components.
 * To avoid import multiple components everytime and to avoid possible mismatch issues e.g. when
 * some imports will be missing this import selector helps to keep all required imports togethers.
 * This import should be used in {@link org.springframework.context.annotation.Import} annotation
 * on applications configuration level.
 */
public class SecurityImportSelector implements ImportSelector, EnvironmentAware {

    private static final boolean SECURITY_ENABLED_DEFAULT = false;

    private Environment environment;

    @Override
    public void setEnvironment(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public String[] selectImports(final AnnotationMetadata importingClassMetadata) {
        final var isSecurityEnabled = environment.getProperty(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, Boolean.class, SECURITY_ENABLED_DEFAULT);

        return isSecurityEnabled ? secured() : unsecured();
    }

    /**
     * When secured mode is enabled imports all required classes for that.
     */
    private String[] secured() {
        return new String[]{
                CurrentUserProvider.class.getName(),
                ResourceServerSecuredConfig.class.getName(),
                TokenAuthenticationFilter.class.getName(),
                CustomJwtAuthenticationConverter.class.getName()
        };
    }

    /**
     * When unsecured mode is enabled imports all required classes for that.
     */
    private String[] unsecured() {
        return new String[]{
                CurrentUserProvider.class.getName(),
                ResourceServerUnsecuredConfig.class.getName(),
                UnsecuredAuthenticationFilter.class.getName()
        };
    }

}
