package mm.expenses.manager.common.web.config;

/**
 * Simple configuration of security properties reused within the application.
 */
public final class SecurityRequestConfig {

    public static final String SECURITY_ENABLED_PROPERTY_PATH = "app.configuration.security-enabled";

    public static final String DEFAULT_KEYCLOAK_URL = "http://localhost:8182";
    public static final String DEFAULT_KEYCLOAK_REALM = "em-app-realm";
    public static final String KEYCLOAK_OAUTH_SCHEME_NAME = "Expenses Manager Authorization";

    public static final String SWAGGER_API_DOCS_PATH = "/v3/api-docs/**";
    public static final String SWAGGER_UI_PATH = "/swagger-ui/**";
    public static final String SWAGGER_UI_HTML_PATH = "/swagger-ui.html";

    public static final String API_PUBLIC_PATH = "/api/v1/**";
    public static final String API_PUBLIC_PATH_OLD = "/api/**";
    public static final String API_INTERNAL_SYSTEM_PATH = "/api/internal/system";
    public static final String API_INTERNAL_SYSTEM_PATHS = API_INTERNAL_SYSTEM_PATH + "/**";
    public static final String API_INTERNAL_SYSTEM_USER_KEYCLOAK_PATH = API_INTERNAL_SYSTEM_PATH + "/users/keycloak/{id}";
    public static final String API_INTERNAL_SPI_PATH = "/api/internal/spi/**";
    public static final String API_UI_PATH = "/api/ui/**";

    public static final String TOKEN = "Token";
    public static final String BEARER = "Bearer";
    public static final String AUTHORIZATION = "Authorization";

    public static final String HEADER_ORIGIN_NAME = "Origin";
    public static final String HEADER_SYSTEM_ORIGIN_VALUE = "system";
    public static final String HEADER_SPI_ORIGIN_VALUE = "system-spi";

}
