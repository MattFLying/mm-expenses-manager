package mm.expenses.manager.common.web.security.unsecured;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UnsecuredAuthenticationFilterTest {

    private final UnsecuredAuthenticationFilter filter = new UnsecuredAuthenticationFilter();

    @Mock
    private FilterChain filterChain;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(UnsecuredAuthenticationFilter.class);

    @Test
    void shouldExecuteFilterAndContinueChain() throws ServletException, IOException {
        // given
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        // when
        filter.doFilter(request, response, filterChain);

        // then
        verify(filterChain).doFilter(any(UnsecuredRequestWrapper.class), eq(response));
    }

    @Test
    void shouldLoadBean_whenPropertyIsMissing() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(UnsecuredAuthenticationFilter.class));
    }

    @Test
    void shouldLoadBean_whenPropertyIsFalse() {
        contextRunner.withPropertyValues(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH + "=false")
                .run(context -> assertThat(context).hasSingleBean(UnsecuredAuthenticationFilter.class));
    }

    @Test
    void shouldNotLoadBean_whenPropertyIsTrue() {
        contextRunner.withPropertyValues(SecurityRequestConfig.SECURITY_ENABLED_PROPERTY_PATH + "=true")
                .run(context -> assertThat(context).doesNotHaveBean(UnsecuredAuthenticationFilter.class));
    }

}