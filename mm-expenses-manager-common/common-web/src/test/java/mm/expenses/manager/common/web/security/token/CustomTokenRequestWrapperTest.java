package mm.expenses.manager.common.web.security.token;

import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.security.Principal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomTokenRequestWrapperTest {

    @Mock
    private Principal principal;

    @Test
    void shouldAddBearerPrefixWhenMissing() {
        // given
        final var request = new MockHttpServletRequest();

        // when
        final var wrapper = new CustomTokenRequestWrapper(request, "raw-token-123");

        // then
        assertThat(wrapper.getHeader(SecurityRequestConfig.AUTHORIZATION)).isEqualTo(SecurityRequestConfig.BEARER + " raw-token-123");
    }

    @Test
    void shouldPreserveExistingBearerPrefix() {
        // given
        final var request = new MockHttpServletRequest();

        // when
        final var wrapper = new CustomTokenRequestWrapper(request, "bearer   token-with-spaces   ");

        // then
        assertThat(wrapper.getHeader(SecurityRequestConfig.AUTHORIZATION)).isEqualTo("bearer   token-with-spaces");
    }

    @Test
    void shouldReturnNull_whenTokenIsBlank() {
        // given
        final var request = new MockHttpServletRequest();

        // when
        final var wrapper = new CustomTokenRequestWrapper(request, "   ");

        // then
        assertThat(wrapper.getHeader(SecurityRequestConfig.AUTHORIZATION)).isNull();
    }

    @Test
    void shouldAddAuthorizationHeaderNameToHeaderNamesIfMissing() {
        // given
        final var request = new MockHttpServletRequest();
        final var wrapper = new CustomTokenRequestWrapper(request, "token-xyz");

        // when
        final var headerNamesEnum = wrapper.getHeaderNames();

        // then
        assertThat(Collections.list(headerNamesEnum)).contains(SecurityRequestConfig.AUTHORIZATION);
    }

    @Test
    void shouldNotDuplicateAuthorizationHeaderIfAlreadyPresent() {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.AUTHORIZATION, "Old-Auth");

        final var wrapper = new CustomTokenRequestWrapper(request, "new-token");

        // when
        final var headerNamesEnum = wrapper.getHeaderNames();

        // then
        final var count = Collections.list(headerNamesEnum).stream()
                .filter(name -> name.equalsIgnoreCase(SecurityRequestConfig.AUTHORIZATION))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldReturnFormattedTokenForAuthorizationHeaderCaseInsensitive() {
        // given
        final var request = new MockHttpServletRequest();
        final var wrapper = new CustomTokenRequestWrapper(request, "my-token");

        // when & then
        assertThat(wrapper.getHeader("authorization")).isEqualTo(SecurityRequestConfig.BEARER + " my-token");
        assertThat(wrapper.getHeader("AUTHORIZATION")).isEqualTo(SecurityRequestConfig.BEARER + " my-token");
    }

    @Test
    void shouldDelegateOtherHeadersToUnderlyingRequest() {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader("X-Custom", "CustomValue");

        final var wrapper = new CustomTokenRequestWrapper(request, "my-token");

        // when
        final var header = wrapper.getHeader("X-Custom");

        // then
        assertThat(header).isEqualTo("CustomValue");
    }

    @Test
    void shouldReturnHeadersEnumerationForAuthorization() {
        // given
        final var request = new MockHttpServletRequest();
        final var wrapper = new CustomTokenRequestWrapper(request, "token-enum");

        // when
        final var headersEnum = wrapper.getHeaders(SecurityRequestConfig.AUTHORIZATION);

        // then
        assertThat(Collections.list(headersEnum)).containsExactly(SecurityRequestConfig.BEARER + " token-enum");
    }

    @Test
    void shouldReturnCustomUserPrincipal() {
        // given
        final var request = new MockHttpServletRequest();
        final var wrapper = new CustomTokenRequestWrapper(request, "token-user");

        // when
        final var userPrincipal = wrapper.getUserPrincipal();

        // then
        assertThat(userPrincipal).isInstanceOf(CustomTokenRequestWrapper.UserPrincipalFromCustomToken.class);
        final var customPrincipal = (CustomTokenRequestWrapper.UserPrincipalFromCustomToken) userPrincipal;

        assertThat(customPrincipal.getToken()).isEqualTo(SecurityRequestConfig.BEARER + " token-user");
        assertThat(customPrincipal.getPrincipal()).isNull();
    }

    @Test
    void shouldTestUserPrincipalFromCustomTokenBehavior() {
        // given
        when(principal.getName()).thenReturn("john_doe");
        final var expectedToken = "Bearer token-123";

        // when
        final var customPrincipal = new CustomTokenRequestWrapper.UserPrincipalFromCustomToken(principal, expectedToken);

        // then
        assertThat(customPrincipal.getName()).isEqualTo("john_doe");
        assertThat(customPrincipal.getToken()).isEqualTo(expectedToken);
        assertThat(customPrincipal.getPrincipal()).isEqualTo(principal);
    }

    @Test
    void shouldReturnNullName_whenUnderlyingPrincipalIsNull() {
        // given
        final var customPrincipal = new CustomTokenRequestWrapper.UserPrincipalFromCustomToken(null, "Bearer token-123");

        // when & then
        assertThat(customPrincipal.getName()).isNull();
    }

}
