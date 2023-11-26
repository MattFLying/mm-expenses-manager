package mm.expenses.manager.common.web;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mm.expenses.manager.common.web.api.WebApi;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Application context for any WEB action that can be shared between different objects related with any possible action.
 */
@ToString
@Getter
@Setter(value = AccessLevel.PROTECTED)
public final class WebContext {

    private WebApi webApi;

    private String requestId;

    private Object requestBody;

    private Collection<String> requestIds;

    private Map<String, String[]> parametersMap;

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
    public WebContext requestId(final String requestId) {
        setRequestId(requestId);
        return this;
    }

    /**
     * If some web action requires multiple request ids they can be set by this method.
     *
     * @param requestIds - multiple request ids
     * @return updated context
     */
    public WebContext requestIds(final Collection<String> requestIds) {
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
    public WebContext parametersMap(final Map<String, String[]> parameters) {
        setParametersMap(parameters);
        return this;
    }

    /**
     * If any parameters are needed for specific web action with excluded null values they can be set by this method.
     *
     * @param parameters - parameters map
     * @return updated context with parameters without null values
     */
    public WebContext parametersMapSkipNullValues(final Map<String, String[]> parameters) {
        return parametersMap(
                parameters.entrySet()
                        .stream()
                        .filter(entry -> Objects.nonNull(entry.getValue()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

}
