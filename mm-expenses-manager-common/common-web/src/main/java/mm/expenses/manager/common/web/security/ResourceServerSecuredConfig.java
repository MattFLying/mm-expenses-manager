package mm.expenses.manager.common.web.security;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.system.SystemAuthenticationFilter;
import mm.expenses.manager.common.web.security.system.SystemAuthorizationDecision;
import mm.expenses.manager.common.web.security.token.CustomBearerTokenResolver;
import mm.expenses.manager.common.web.security.token.CustomJwtAuthenticationConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

/**
 * Security configuration when secured mode is expected.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(name = SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH, havingValue = "true")
class ResourceServerSecuredConfig {

    private final CustomJwtAuthenticationConverter converter;

    @Bean
    public BearerTokenResolver customBearerTokenResolver() {
        return new CustomBearerTokenResolver();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new ApiAccessDeniedHandler();
    }

    /**
     * Security filter chain to be executed at the top of others to verify if there are any system calls.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChainForSystemCalls(final HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        new SystemAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class
                )
                .securityMatcher(
                        new OrRequestMatcher(
                                // handles Keycloak SPI communication
                                new AndRequestMatcher(
                                        pathPattern(SecurityRequestConfig.API_INTERNAL_SPI_PATH),
                                        new RequestHeaderRequestMatcher(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE)
                                ),

                                // handles internally system communication between services
                                new AndRequestMatcher(
                                        pathPattern(SecurityRequestConfig.API_INTERNAL_SYSTEM_PATHS),
                                        new RequestHeaderRequestMatcher(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE)
                                )
                        )
                )
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                .anyRequest()
                                .access(SystemAuthorizationDecision::new)
                )
                .build();
    }

    /**
     * Security filter chain to be executed after the system verification to process public API or UI requests.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChainForPublicApiCalls(final HttpSecurity http, final AccessDeniedHandler accessDeniedHandler) throws Exception {
        return http.securityMatcher(
                        new OrRequestMatcher(
                                // this is for public access to API swagger but each call is validated separately based on owned permissions
                                pathPattern(SecurityRequestConfig.API_PUBLIC_PATH),
                                pathPattern(SecurityRequestConfig.API_PUBLIC_PATH_OLD),

                                // at this moment it is not in use but for further usage and improvements
                                pathPattern(SecurityRequestConfig.API_UI_PATH)
                        )
                )
                .authorizeHttpRequests(authorizationManagerRequestMatcherRegistry ->
                        authorizationManagerRequestMatcherRegistry
                                // this is for public access to API swagger but each call is validated separately based on owned permissions
                                .requestMatchers(SecurityRequestConfig.API_PUBLIC_PATH).permitAll()
                                .requestMatchers(SecurityRequestConfig.API_PUBLIC_PATH_OLD).permitAll()

                                // at this moment it is not in use but for further usage and improvements
                                .requestMatchers(SecurityRequestConfig.API_UI_PATH).authenticated()
                )
                .exceptionHandling(configurer -> configurer.accessDeniedHandler(accessDeniedHandler))
                .oauth2ResourceServer(oauth2Configurer -> oauth2Configurer.jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(converter)))
                .build();
    }

    private PathPatternRequestMatcher pathPattern(final String path) {
        return PathPatternRequestMatcher.pathPattern(path);
    }

}
