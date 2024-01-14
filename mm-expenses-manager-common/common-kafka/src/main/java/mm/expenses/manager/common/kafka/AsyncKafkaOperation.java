package mm.expenses.manager.common.kafka;

import java.util.Objects;

/**
 * Defines possible operations supported via asynchronous communications.
 */
public enum AsyncKafkaOperation {
    CREATE, UPDATE, DELETE,

    /**
     * Uses in special cases when {@link AsyncKafkaOperation} is null.
     */
    UNDEFINED;

    /**
     * Retrieves original asynchronous operation from {@link AsyncKafkaOperation} object or UNDEFINED is original object is null.
     *
     * @param operation - asynchronous operation object to be verified
     * @return original asynchronous operation or UNDEFINED is original object is null
     */
    public static AsyncKafkaOperation of(final AsyncKafkaOperation operation) {
        if (Objects.isNull(operation)) {
            return UNDEFINED;
        }
        return operation;
    }

}
