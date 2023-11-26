package mm.expenses.manager.common.web.api;

import mm.expenses.manager.common.exceptions.base.EmUncheckedException;
import mm.expenses.manager.common.web.exception.WebExceptionMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

/**
 * Defines available HTTP methods in whole application.
 */
public enum WebHttpMethod {
    GET, POST, PUT, PATCH, DELETE;

    /**
     * Returns specific {@link WebHttpMethod} related with passed argument.
     *
     * @param method - method name
     * @return {@link WebHttpMethod} of passed method name
     */
    public static WebHttpMethod of(final String method) {
        if (StringUtils.isEmpty(method)) {
            throw new EmUncheckedException(WebExceptionMessage.HTTP_METHOD_NULL);
        }
        return WebHttpMethod.valueOf(method.toUpperCase());
    }

    /**
     * Returns {@link HttpMethod} of current {@link WebHttpMethod}.
     *
     * @return returns spring related {@link HttpMethod} component for current {@link WebHttpMethod}
     */
    public HttpMethod getHttpMethod() {
        return HttpMethod.valueOf(name());
    }

}
