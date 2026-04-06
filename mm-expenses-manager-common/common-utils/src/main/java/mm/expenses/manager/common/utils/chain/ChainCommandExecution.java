package mm.expenses.manager.common.utils.chain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Base chain of responsibility command definition.
 *
 * @param <Request>  request type
 * @param <Response> response type
 */
public abstract class ChainCommandExecution<Request, Response> {

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    protected Context context = new Context();

    protected ChainCommandExecution<Request, Response> next;

    /**
     * @return true if it has next step in chain, otherwise return false
     */
    protected boolean hasNext() {
        return Objects.nonNull(next);
    }

    /**
     * Defines the next step in chain to be executed.
     *
     * @param nextStep next step in chain to be executed
     */
    public void setNextHandler(final ChainCommandExecution<Request, Response> nextStep) {
        this.next = nextStep;
        if (Objects.nonNull(this.next)) {
            this.next.setContext(this.context);
        }
    }

    /**
     * Defines specific steps to be executed in current state in chain.
     *
     * @param request requested object
     * @return specific return type object
     */
    public abstract Response handleRequest(final Request request);

    /**
     * Builds chain of steps to be executed in given order.
     *
     * @param first first step in whole chain
     * @param chain next steps in whole chain
     * @return result of all steps in chain
     */
    public static ChainCommandExecution build(final ChainCommandExecution first, final ChainCommandExecution... chain) {
        var head = first;
        for (var next : chain) {
            head.setNextHandler(next);
            head = next;
        }
        return first;
    }

    /**
     * Chain context to be shared between all steps in chain.
     */
    public static class Context {

        public final static String SAVED_KEY = "saved";

        @Getter
        private final Map<String, Object> arguments = new HashMap<>();

        /**
         * Adds argument to be stored in context.
         *
         * @param key   name of the argument
         * @param value value of the argument
         */
        public void addArgument(final String key, final Object value) {
            this.arguments.put(key, value);
        }

        /**
         * Add arguments to be stored in context.
         *
         * @param arguments map of arguments to be stored in the context
         */
        public void addArguments(final Map<String, Object> arguments) {
            arguments.forEach(this::addArgument);
        }

        /**
         * Gets specific argument stored in the context by specific key.
         *
         * @param key key of stored argument
         * @return value of specific key if exists otherwise null
         */
        public Optional<Object> getArgument(final String key) {
            return Optional.ofNullable(this.arguments.getOrDefault(key, null));
        }

    }

}
