package mm.expenses.manager.common.web.security;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.unsecured.UnsecuredAuthenticationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration when secured mode is expected.
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, havingValue = "false", matchIfMissing = true)
class ResourceServerUnsecuredConfig {

    @Bean
    public AccessDeniedHandler accessDeniedHandler2() {
        return new ApiAccessDeniedHandler();
    }

    /**
     * Security filter chain to be executed when the security is disabled for every possible REST calls.
     */
    @Bean
    public SecurityFilterChain securityFilterChainForDisabledSecurity(final HttpSecurity http, final AccessDeniedHandler accessDeniedHandler) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new UnsecuredAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .exceptionHandling(configurer -> configurer.accessDeniedHandler(accessDeniedHandler))
                .build();
    }

    /**
     * Ignores security for specific paths.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                SecurityRequestConfig.API_PUBLIC_PATH,
                SecurityRequestConfig.API_PUBLIC_PATH_OLD,
                SecurityRequestConfig.SWAGGER_API_DOCS_PATH,
                SecurityRequestConfig.SWAGGER_UI_PATH,
                SecurityRequestConfig.SWAGGER_UI_HTML_PATH
        );
    }

}
