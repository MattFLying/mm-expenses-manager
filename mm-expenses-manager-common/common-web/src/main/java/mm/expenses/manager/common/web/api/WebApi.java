package mm.expenses.manager.common.web.api;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Defines Web API definition of available endpoints.
 * Should be used for enums.
 */
public interface WebApi {

    /**
     * Default endpoints path as a main resource.
     *
     * @return default url path for specific resource eg. /examples
     */
    String getBasePath();

    /**
     * Url path to current endpoint. The base path is not included in this endpoint definition.
     *
     * @return url path
     */
    String getPathUrl();

    /**
     * Http method for current endpoint.
     *
     * @return Http method
     */
    HttpMethod getMethod();

    /**
     * Expected Http status that should be retrieved if endpoint has been successfully called.
     *
     * @return expected Http status
     */
    HttpStatus getExpectedSuccessfulStatus();

    /**
     * Current Web Api url path.
     *
     * @return current Web Api url path
     */
    default WebApi getPath() {
        return this;
    }

    /**
     * Returns full URL path of current endpoint including base path and the current url path url.
     *
     * @return full URL path
     */
    default String getFullPathUrl() {
        return String.format("%s%s", getBasePath(), getPathUrl());
    }

}
