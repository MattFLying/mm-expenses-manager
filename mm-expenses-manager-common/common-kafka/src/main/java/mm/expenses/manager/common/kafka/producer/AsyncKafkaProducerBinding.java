package mm.expenses.manager.common.kafka.producer;

import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.async.AsyncProducerBinding;

/**
 * Interface for kafka producer binding implementation to define from where the message should be received.
 */
public interface AsyncKafkaProducerBinding extends AsyncProducerBinding {

    /**
     * Sets the kafka asynchronous operation to be done within expected topic.
     * Each topic message can be split for different operations defined by {@link AsyncKafkaOperation}.
     * Value is not required, but it has to be handled by specific consumer of the topic
     * in {@link mm.expenses.manager.common.kafka.consumer.AsyncKafkaConsumer} implementation.
     *
     * @return asynchronous kafka operation to be done on the topic
     */
    AsyncKafkaOperation getOperation();

}
