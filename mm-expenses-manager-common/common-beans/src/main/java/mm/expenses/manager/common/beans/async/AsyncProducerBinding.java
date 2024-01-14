package mm.expenses.manager.common.beans.async;

/**
 * Interface for asynchronous producer's binding implementation to define where the message should be sent.
 */
public interface AsyncProducerBinding {

    /**
     * The binding name of the message.
     *
     * @return binding name of the message
     */
    String getProducerBindingName();

    /**
     * The topic name where the message should be sent related directly with the binding in getBindingName().
     *
     * @return topic name of the message
     */
    String getProducerTopicName();

}
