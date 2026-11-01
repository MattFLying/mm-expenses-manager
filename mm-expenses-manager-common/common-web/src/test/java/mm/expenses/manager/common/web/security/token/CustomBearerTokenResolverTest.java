package mm.expenses.manager.common.web.security.token;

import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class CustomBearerTokenResolverTest {

    private final CustomBearerTokenResolver tokenResolver = new CustomBearerTokenResolver();

    @Test
    void shouldReturnNull_whenRequestIsNull() {
        // when
        final var token = tokenResolver.resolve(null);

        // then
        assertThat(token).isNull();
    }

    @Test
    void shouldReturnNull_whenAuthorizationHeaderIsMissing() {
        // given
        final var request = new MockHttpServletRequest();

        // when
        final var token = tokenResolver.resolve(request);

        // then
        assertThat(token).isNull();
    }

    @Test
    void shouldReturnNull_whenAuthorizationHeaderIsBlank() {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.AUTHORIZATION, "   ");

        // when
        final var token = tokenResolver.resolve(request);

        // then
        assertThat(token).isNull();
    }

    @Test
    void shouldExtractToken_whenBearerPrefixIsPresent() {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.AUTHORIZATION, "Bearer test-token-123");

        // when
        final var token = tokenResolver.resolve(request);

        // then
        assertThat(token).isEqualTo("test-token-123");
    }

    @Test
    void shouldExtractToken_whenBearerPrefixHasDifferentCase() {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.AUTHORIZATION, "bearer test-token-456");

        // when
        final var token = tokenResolver.resolve(request);

        // then
        assertThat(token).isEqualTo("test-token-456");
    }

    @Test
    void shouldTrimToken_whenBearerPrefixIsPresent() {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.AUTHORIZATION, "Bearer   test-token-789   ");

        // when
        final var token = tokenResolver.resolve(request);

        // then
        assertThat(token).isEqualTo("test-token-789");
    }

    @Test
    void shouldReturnRawHeader_whenBearerPrefixIsMissing() {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.AUTHORIZATION, "raw-token-abc");

        // when
        final var token = tokenResolver.resolve(request);

        // then
        assertThat(token).isEqualTo("raw-token-abc");
    }

    @Test
    void shouldTrimRawHeader_whenBearerPrefixIsMissingWithSpaces() {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.AUTHORIZATION, "   raw-token-xyz   ");

        // when
        final var token = tokenResolver.resolve(request);

        // then
        assertThat(token).isEqualTo("raw-token-xyz");
    }

}