package mm.expenses.manager.common.web;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mm.expenses.manager.common.web.api.WebApi;

import java.util.*;

/**
 * Application context for any WEB action that can be shared between different objects related with any possible action.
 */
@ToString
@Getter
@Setter(value = AccessLevel.PROTECTED)
public final class WebContext {

    private WebApi webApi;

    private UUID requestId;

    private Object requestBody;

    private Collection<UUID> requestIds;

    private WebUrlParameters parameters;

    private Map<String, String> headers;

    /**
     * Creates context for specific {@link WebApi} path.
     *
     * @param webApi - web api url path
     * @return created context
     */
    public static WebContext of(final WebApi webApi) {
        var api = new WebContext();
        api.setWebApi(webApi);

        return api;
    }

    /**
     * Defines expected request id for current context.
     *
     * @param requestId - request id
     * @return updated context
     */
    public WebContext requestId(final UUID requestId) {
        setRequestId(requestId);
        return this;
    }

    /**
     * If some web action requires multiple request ids they can be set by this method.
     *
     * @param requestIds - multiple request ids
     * @return updated context
     */
    public WebContext requestIds(final Collection<UUID> requestIds) {
        setRequestIds(requestIds);
        return this;
    }

    /**
     * If some web action requires some request body to be processed it can be set by this method.
     *
     * @param requestBody - request body
     * @return updated context
     */
    public WebContext requestBody(final Object requestBody) {
        setRequestBody(requestBody);
        return this;
    }

    /**
     * If any parameters are needed for specific web action they can be set by this method.
     *
     * @param parameters - parameters map
     * @return updated context
     */
    public WebContext parameters(final WebUrlParameters parameters) {
        setParameters(parameters);
        return this;
    }

    /**
     * If any headers are needed for specific web action they can be set by this method.
     *
     * @param headers - headers map
     * @return updated context
     */
    public WebContext headersMap(final Map<String, String> headers) {
        setHeaders(headers);
        return this;
    }

}
