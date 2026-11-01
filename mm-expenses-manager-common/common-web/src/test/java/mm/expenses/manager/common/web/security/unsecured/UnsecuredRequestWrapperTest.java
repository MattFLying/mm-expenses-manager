package mm.expenses.manager.common.web.security.unsecured;

import mm.expenses.manager.common.web.security.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class UnsecuredRequestWrapperTest {

    @Test
    void shouldReturnUnsecuredContextPrincipal() {
        // given
        final var mockRequest = new MockHttpServletRequest();
        final var wrapper = new UnsecuredRequestWrapper(mockRequest);

        // when
        final var principal = wrapper.getUserPrincipal();

        // then
        assertThat(principal).isEqualTo(SecurityContext.unsecuredContext());
    }

    @Test
    void shouldDelegateOtherMethodsToUnderlyingRequest() {
        // given
        final var mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("POST");
        mockRequest.setRequestURI("/api/test");

        final var wrapper = new UnsecuredRequestWrapper(mockRequest);

        // when & then
        assertThat(wrapper.getMethod()).isEqualTo("POST");
        assertThat(wrapper.getRequestURI()).isEqualTo("/api/test");
    }

}