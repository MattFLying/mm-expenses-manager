package mm.expenses.manager.common.web.chain;

import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.web.security.SecurityContext;
import mm.expenses.manager.common.web.security.SecurityMode;

import java.util.*;

/**
 * Secured chain of responsibility command definition.
 *
 * @param <Request>  request type
 * @param <Response> response type
 */
public abstract class SecuredChainCommandExecution<Request, Response> extends ChainCommandExecution<Request, Response> {

    public final static String IS_SECURED = "isSecured";
    public final static String USER_ID = "userId";
    public final static String PERMISSIONS = "permissions";

    /**
     * Builds chain of secured steps to be executed in given order.
     *
     * @param securityContext security context to be respected during processing
     * @param first           first step in whole chain
     * @param chain           next steps in whole chain
     * @return result of all steps in chain
     */
    public static SecuredChainCommandExecution build(final SecurityContext securityContext, final SecuredChainCommandExecution first, final SecuredChainCommandExecution... chain) {
        first.handleSecurity(securityContext);

        if (Objects.nonNull(chain)) {
            var head = first;
            for (var next : chain) {
                head.setNextHandler(next);
                head = next;
            }
        }

        return first;
    }

    /**
     * Builds chain of secured steps just for one step.
     *
     * @param securityContext security context to be respected during processing
     * @param first           first step in whole chain
     * @return result of all steps in chain but in this case this is only one step available here
     */
    public static SecuredChainCommandExecution build(final SecurityContext securityContext, final SecuredChainCommandExecution first) {
        return build(securityContext, first, null);
    }

    /**
     * @return checks in chain context if security is enabled for the execution.
     */
    protected boolean isSecured() {
        return (boolean) context.getArgument(IS_SECURED).orElse(false);
    }

    /**
     * @return returns identifier of the user already executed the operation, otherwise returns null.
     */
    protected UUID getUserId() {
        return (UUID) context.getArgument(USER_ID).orElse(null);
    }

    /**
     * @return returns all available permissions for the user already executed the operation, otherwise returns empty collection.
     */
    protected Set<Permission> getPermissions() {
        return (Set<Permission>) context.getArgument(PERMISSIONS).orElse(Collections.emptySet());
    }

    /**
     * Prepares context for the commands chain with the security specific details based on the {@link SecurityContext}.
     *
     * @param securityContext security context to be respected during processing
     */
    private void handleSecurity(final SecurityContext securityContext) {
        Optional.ofNullable(securityContext)
                .ifPresentOrElse(
                        security -> {
                            final var isSecured = SecurityMode.SECURED.equals(security.getSecurityMode());
                            context.addArgument(IS_SECURED, isSecured);
                            context.addArgument(USER_ID, security.getUserId());
                            context.addArgument(PERMISSIONS, security.getPermissions());
                        },
                        () -> context.addArgument(IS_SECURED, false)
                );
    }

}
