package mm.expenses.manager.common.beans.async;

/**
 * Interface for asynchronous consumer's binding implementation to define from where the message should be received.
 */
public interface AsyncConsumerBinding {

    /**
     * The binding name of the message.
     *
     * @return binding name of the message
     */
    String getConsumerBindingName();

    /**
     * The topic name from where the message should be received related directly with the binding in getBindingName().
     *
     * @return topic name of the message
     */
    String getConsumerTopicName();

}
