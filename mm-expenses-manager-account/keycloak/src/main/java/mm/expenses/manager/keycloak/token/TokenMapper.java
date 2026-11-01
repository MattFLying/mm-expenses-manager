package mm.expenses.manager.keycloak.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.config.ObjectMapperConfig;
import mm.expenses.manager.keycloak.InternalProperties;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import mm.expenses.manager.keycloak.exception.KeycloakSpiExceptionMessage;
import mm.expenses.manager.keycloak.http.UserHttpClient;
import org.keycloak.Config;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static mm.expenses.manager.keycloak.KeycloakEventsListenerFactory.*;

/**
 * Keycloak token mapper to extend generated token by Keycloak with custom claim
 * that contains available user permissions in whole system.
 * Has to be registered directly by Keycloak in realms/events view and to be defined in META-INF
 */
@Slf4j
public class TokenMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    static final String PROVIDER_ID = "em_keycloak-token-mapper";
    private static final String USER_INTERNAL_ID = "userInternalId";
    private static final String USER_PERMISSIONS = "permissions";
    private static final String USER_PERMISSIONS_PROPERTY = "user.internal.permissions";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    private InternalProperties internalProperties;
    private ObjectMapper mapper;
    private UserHttpClient userClient;

    static {
        final var permissionsProperty = new ProviderConfigProperty();
        permissionsProperty.setName(USER_PERMISSIONS_PROPERTY);
        permissionsProperty.setLabel("Get user permissions");
        permissionsProperty.setHelpText("Gets user permissions internally stored before processing");
        permissionsProperty.setType(ProviderConfigProperty.BOOLEAN_TYPE);
        permissionsProperty.setDefaultValue(Boolean.TRUE);

        CONFIG_PROPERTIES.add(permissionsProperty);
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(CONFIG_PROPERTIES, TokenMapper.class);
    }

    @Override
    public void init(final Config.Scope config) {
        try {
            // properties for internal usage initialization
            this.internalProperties = new InternalProperties();

            // configure common mapper
            this.mapper = ObjectMapperConfig.objectMapper();

            // configure access to account-service via HTTP
            final var accountServiceUrl = internalProperties.getEnvironmentVariable(ACCOUNT_SERVICE_SPI_URL_PROPERTY, ACCOUNT_SERVICE_SPI_DEFAULT_VALUE);
            final var originHeader = internalProperties.getEnvironmentVariable(KEYCLOAK_SPI_ORIGIN_HEADER_PROPERTY, KEYCLOAK_SPI_ORIGIN_HEADER_DEFAULT_VALUE);
            final var connectionTimeout = internalProperties.getEnvironmentVariable(HTTP_CONNECTION_TIMEOUT_IN_SECONDS_PROPERTY, HTTP_CONNECTION_TIMEOUT_IN_SECONDS_DEFAULT_VALUE);

            this.internalProperties.setAccountServiceUrl(String.valueOf(accountServiceUrl));
            this.internalProperties.setOriginHeader(String.valueOf(originHeader));
            this.internalProperties.setHttpConnectionTimeoutInSeconds(Integer.parseInt(String.valueOf(connectionTimeout)));

            this.userClient = new UserHttpClient(this.mapper, this.internalProperties);
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Cannot initialize Keycloak external data protocol mapper with error: {}", exception.getMessage(), exception);
        }
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getHelpText() {
        return "Builds token with custom claim built from user permissions.";
    }

    @Override
    public String getDisplayType() {
        return "EM Token Mapper";
    }

    @Override
    protected void setClaim(final IDToken token, final ProtocolMapperModel mappingModel, final UserSessionModel userSession,
                            final KeycloakSession keycloakSession, final ClientSessionContext clientSessionContext) {
        try {
            if (CONFIG_PROPERTIES.stream().anyMatch(configProperty -> USER_PERMISSIONS_PROPERTY.equals(configProperty.getName()) && Boolean.TRUE.equals(configProperty.getDefaultValue()))) {
                if (this.userClient.isAvailable()) {
                    final var userId = userSession.getUser().getId();
                    final var userResponse = this.userClient.getUser(UUID.fromString(userId));
                    if (Objects.nonNull(userResponse)) {
                        token.getOtherClaims().put(USER_INTERNAL_ID, userResponse.getId().toString());

                        if (Objects.nonNull(userResponse.getPermissions()) && !userResponse.getPermissions().isEmpty()) {
                            token.getOtherClaims().put(USER_PERMISSIONS, userResponse.getPermissions());
                        }
                    }
                } else {
                    log.error("[KEYCLOAK-SPI] User client unavailable, permissions could not be loaded.");
                }
            }
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Cannot fetch user to process permissions: {}", exception.getMessage(), exception);
            throw new KeycloakSpiException(KeycloakSpiExceptionMessage.USER_CANNOT_BE_DOWNLOADED_INTERNALLY, exception);
        }
    }

    @Override
    public void close() {
        if (Objects.nonNull(this.mapper)) {
            this.mapper.clearCaches();
        }
        if (Objects.nonNull(this.internalProperties)) {
            this.internalProperties.clean();
        }
    }

}
