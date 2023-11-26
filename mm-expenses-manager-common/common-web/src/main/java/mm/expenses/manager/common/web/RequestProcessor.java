package mm.expenses.manager.common.web;

/**
 * Web API request processor to execute some specific processing within current {@link WebContext}.
 */
@FunctionalInterface
public interface RequestProcessor {

    /**
     * Defines action to be executed for current request with current request context {@link WebContext}.
     *
     * @param context - request context
     * @return expected response object for current request
     */
    Object process(final WebContext context);

}
