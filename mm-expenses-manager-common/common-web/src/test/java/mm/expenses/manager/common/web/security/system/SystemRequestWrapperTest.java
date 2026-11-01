package mm.expenses.manager.common.web.security.system;

import mm.expenses.manager.common.web.config.SecurityRequestConfig;
import mm.expenses.manager.common.web.security.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class SystemRequestWrapperTest {

    @Test
    void shouldReturnSystemContextPrincipal() {
        // given
        final var mockRequest = new MockHttpServletRequest();
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var principal = wrapper.getUserPrincipal();

        // then
        assertThat(principal).isEqualTo(SecurityContext.systemContext());
    }

    @Test
    void shouldReturnSpiOriginHeaderNormalized() {
        // given
        final var mockRequest = new MockHttpServletRequest();

        mockRequest.addHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE.toLowerCase());
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var headerValue = wrapper.getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME);

        // then
        assertThat(headerValue).isEqualTo(SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE);
    }

    @Test
    void shouldReturnSystemOriginHeaderNormalized() {
        // given
        final var mockRequest = new MockHttpServletRequest();

        mockRequest.addHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE);
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var headerValue = wrapper.getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME);

        // then
        assertThat(headerValue).isEqualTo(SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE);
    }

    @Test
    void shouldReturnOriginalHeader_whenValueUnknown() {
        // given
        final var unknownOrigin = "UNKNOWN_ORIGIN";
        final var mockRequest = new MockHttpServletRequest();

        mockRequest.addHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME, unknownOrigin);
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var headerValue = wrapper.getHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME);

        // then
        assertThat(headerValue).isEqualTo(unknownOrigin);
    }

    @Test
    void shouldReturnStandardHeaderForOtherNames() {
        // given
        final var customHeader = "X-Custom-Header";
        final var customValue = "CustomValue";
        final var mockRequest = new MockHttpServletRequest();

        mockRequest.addHeader(customHeader, customValue);
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var headerValue = wrapper.getHeader(customHeader);

        // then
        assertThat(headerValue).isEqualTo(customValue);
    }

    @Test
    void shouldReturnHeadersEnumerationForOrigin() {
        // given
        final var mockRequest = new MockHttpServletRequest();

        mockRequest.addHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE);
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var headersEnum = wrapper.getHeaders(SecurityRequestConfig.HEADER_ORIGIN_NAME);

        // then
        assertThat(headersEnum).isNotNull();
        assertThat(Collections.list(headersEnum)).containsExactly(SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE);
    }

    @Test
    void shouldReturnStandardHeadersEnumerationForOthers() {
        // given
        final var customHeader = "X-Custom-Header";
        final var mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader(customHeader, "test1");
        mockRequest.addHeader(customHeader, "test2");

        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var headersEnum = wrapper.getHeaders(customHeader);

        // then
        assertThat(Collections.list(headersEnum)).containsExactly("test1", "test2");
    }

    @Test
    void shouldAddOriginHeaderNameToHeaderNamesIfMissing() {
        // given
        final var mockRequest = new MockHttpServletRequest();
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var headerNamesEnum = wrapper.getHeaderNames();

        // then
        assertThat(Collections.list(headerNamesEnum)).contains(SecurityRequestConfig.HEADER_ORIGIN_NAME);
    }

    @Test
    void shouldNotDuplicateOriginHeaderNameIfAlreadyPresent() {
        // given
        final var mockRequest = new MockHttpServletRequest();

        mockRequest.addHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME, SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE);
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // when
        final var headerNamesEnum = wrapper.getHeaderNames();

        // then
        final var count = Collections.list(headerNamesEnum).stream()
                .filter(name -> name.equalsIgnoreCase(SecurityRequestConfig.HEADER_ORIGIN_NAME))
                .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldEvaluateHelperMethodsCorrectly() {
        // given
        final var mockRequest = new MockHttpServletRequest();
        final var wrapper = new SystemRequestWrapper(mockRequest);

        // then
        assertThat(wrapper.hasOriginHeader(SecurityRequestConfig.HEADER_ORIGIN_NAME)).isTrue();
        assertThat(wrapper.hasOriginHeader("SOME_OTHER_HEADER")).isFalse();

        assertThat(wrapper.hasSpiOrigin(SecurityRequestConfig.HEADER_SPI_ORIGIN_VALUE)).isTrue();
        assertThat(wrapper.hasSpiOrigin("invalid")).isFalse();

        assertThat(wrapper.hasSystemOrigin(SecurityRequestConfig.HEADER_SYSTEM_ORIGIN_VALUE)).isTrue();
        assertThat(wrapper.hasSystemOrigin("invalid")).isFalse();
    }

}