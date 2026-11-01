package mm.expenses.manager.common.web.security.token;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterTest {

    private final TokenAuthenticationFilter filter = new TokenAuthenticationFilter();

    @Mock
    private FilterChain filterChain;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TokenAuthenticationFilter.class);

    @Test
    void shouldWrapRequest_whenCustomTokenIsPresent() throws ServletException, IOException {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.TOKEN, "my-custom-api-token");

        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(any(CustomTokenRequestWrapper.class), eq(response));
    }

    @Test
    void shouldNotWrapRequest_whenTokenIsMissing() throws ServletException, IOException {
        // given
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(eq(request), eq(response));
    }

    @Test
    void shouldNotWrapRequest_whenTokenIsBlank() throws ServletException, IOException {
        // given
        final var request = new MockHttpServletRequest();
        request.addHeader(SecurityRequestConfig.TOKEN, "   ");

        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(eq(request), eq(response));
    }

    @Test
    void shouldLoadBean_whenSecurityEnabledIsTrue() {
        contextRunner.withPropertyValues(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH + "=true")
                .run(context -> assertThat(context).hasSingleBean(TokenAuthenticationFilter.class));
    }

    @Test
    void shouldNotLoadBean_whenPropertyIsMissing() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(TokenAuthenticationFilter.class));
    }

    @Test
    void shouldNotLoadBean_whenPropertyIsFalse() {
        contextRunner.withPropertyValues(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH + "=false")
                .run(context -> assertThat(context).doesNotHaveBean(TokenAuthenticationFilter.class));
    }

    @Test
    void shouldHaveHighestPrecedenceOrder() {
        // given
        final var orderAnnotation = AnnotationUtils.findAnnotation(TokenAuthenticationFilter.class, Order.class);

        // then
        assertThat(orderAnnotation).isNotNull();
        assertThat(orderAnnotation.value()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

}
