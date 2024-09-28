package mm.expenses.manager.common.async;

import java.io.Serializable;

/**
 * Interface for asynchronous binding implementation to define from/to where the message should be received/sent.
 */
public interface AsyncBinding extends Serializable {

    /**
     * The producer binding name of the message.
     *
     * @return producer binding name of the message
     */
    String producerBindingName();

    /**
     * The consumer binding name of the message.
     *
     * @return consumer binding name of the message
     */
    String consumerBindingName();

    /**
     * The topic name of the message related directly with the binding.
     *
     * @return topic name of the message
     */
    String topicName();

}
