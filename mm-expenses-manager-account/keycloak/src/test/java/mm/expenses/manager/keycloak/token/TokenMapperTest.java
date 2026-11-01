package mm.expenses.manager.keycloak.token;

import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import mm.expenses.manager.keycloak.http.UserHttpClient;
import mm.expenses.manager.keycloak.model.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.keycloak.Config;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.representations.IDToken;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TokenMapperTest {

    private static final UUID USER_INTERNAL_ID = UUID.randomUUID();
    private static final String USER_KEYCLOAK_ID = UUID.randomUUID().toString();

    @Mock
    private UserHttpClient userClient;

    @Mock
    private Config.Scope configScope;

    @Mock
    private IDToken idToken;

    @Mock
    private ProtocolMapperModel mappingModel;

    @Mock
    private UserSessionModel userSession;

    @Mock
    private UserModel userModel;

    @Mock
    private KeycloakSession keycloakSession;

    @Mock
    private ClientSessionContext clientSessionContext;

    private TokenMapper tokenMapper;

    @BeforeEach
    public void setUp() throws Exception {
        tokenMapper = new TokenMapper();

        final var userClientField = TokenMapper.class.getDeclaredField("userClient");
        userClientField.setAccessible(true);
        userClientField.set(tokenMapper, userClient);
    }

    @Test
    public void shouldReturnMetadataAndConfig() {
        assertThat(tokenMapper.getId()).isEqualTo(TokenMapper.PROVIDER_ID);
        assertThat(tokenMapper.getDisplayCategory()).isEqualTo(TokenMapper.TOKEN_MAPPER_CATEGORY);
        assertThat(tokenMapper.getDisplayType()).isEqualTo("EM Token Mapper");
        assertThat(tokenMapper.getHelpText()).isEqualTo("Builds token with custom claim built from user permissions.");
        assertThat(tokenMapper.getConfigProperties()).isNotEmpty();
    }

    @Test
    public void shouldInitializeSuccessfully() {
        tokenMapper.init(configScope);
    }

    @EnumSource(Permission.class)
    @ParameterizedTest
    public void shouldSetClaimSuccessfullyWithPermissions(final Permission permission) throws Throwable {
        // given
        final var permissions = Map.of(permission.getGroup().toString(), Set.of(permission));
        final var userResponse = new UserResponse();
        userResponse.setId(USER_INTERNAL_ID);
        userResponse.setPermissions(permissions);

        final var otherClaims = new HashMap<String, Object>();

        when(userClient.isAvailable()).thenReturn(true);
        when(userSession.getUser()).thenReturn(userModel);
        when(userModel.getId()).thenReturn(USER_KEYCLOAK_ID);
        when(userClient.getUser(UUID.fromString(USER_KEYCLOAK_ID))).thenReturn(userResponse);
        when(idToken.getOtherClaims()).thenReturn(otherClaims);

        // when
        setClaimByReflection(idToken, mappingModel, userSession, keycloakSession, clientSessionContext);

        // then
        assertThat(otherClaims).containsEntry("userInternalId", USER_INTERNAL_ID.toString());
        assertThat(otherClaims).containsEntry("permissions", permissions);
    }

    @Test
    public void shouldSetClaimWithoutPermissions() throws Throwable {
        // given
        final var userResponse = new UserResponse();
        userResponse.setId(USER_INTERNAL_ID);
        userResponse.setPermissions(null);

        final var otherClaims = new HashMap<String, Object>();

        when(userClient.isAvailable()).thenReturn(true);
        when(userSession.getUser()).thenReturn(userModel);
        when(userModel.getId()).thenReturn(USER_KEYCLOAK_ID);
        when(userClient.getUser(any(UUID.class))).thenReturn(userResponse);
        when(idToken.getOtherClaims()).thenReturn(otherClaims);

        // when
        setClaimByReflection(idToken, mappingModel, userSession, keycloakSession, clientSessionContext);

        // then
        assertThat(otherClaims).containsEntry("userInternalId", USER_INTERNAL_ID.toString());
        assertThat(otherClaims).doesNotContainKey("permissions");
    }

    @Test
    public void shouldSkipWhenClientUnavailable() throws Throwable {
        // given
        when(userClient.isAvailable()).thenReturn(false);

        // when
        setClaimByReflection(idToken, mappingModel, userSession, keycloakSession, clientSessionContext);

        // then
        verify(userClient, never()).getUser(any());
    }

    @Test
    public void shouldThrowExceptionWhenErrorOccurs() throws Exception {
        // given
        when(userClient.isAvailable()).thenReturn(true);
        when(userSession.getUser()).thenReturn(userModel);
        when(userModel.getId()).thenReturn("invalid-uuid");

        // when & then
        assertThatThrownBy(() -> setClaimByReflection(idToken, mappingModel, userSession, keycloakSession, clientSessionContext)).isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldCloseSafely() {
        // when & then
        tokenMapper.close();
    }

    private void setClaimByReflection(final IDToken token, final ProtocolMapperModel mappingModel, final UserSessionModel userSession,
                                      final KeycloakSession keycloakSession, final ClientSessionContext clientSessionContext) throws Throwable {
        try {
            final var setClaimMethod = TokenMapper.class.getDeclaredMethod(
                    "setClaim", IDToken.class, ProtocolMapperModel.class, UserSessionModel.class, KeycloakSession.class, ClientSessionContext.class
            );
            setClaimMethod.setAccessible(true);
            setClaimMethod.invoke(tokenMapper, token, mappingModel, userSession, keycloakSession, clientSessionContext);
        } catch (final InvocationTargetException exception) {
            throw exception.getTargetException();
        }
    }

}
