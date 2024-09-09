package mm.expenses.manager.common.kafka.consumer;

import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.kafka.async.AsyncConsumerBinding;

/**
 * Interface for kafka consumer binding implementation to define from where the message should be received.
 */
public interface AsyncKafkaConsumerBinding extends AsyncConsumerBinding {

    /**
     * Gets the kafka asynchronous operation to be done within expected topic.
     * Each topic message can be split for different operations defined by {@link AsyncKafkaOperation}.
     * Value is not required and handling specific operations has to be done by consumers implementations,
     * if value is undefined or null it has to be handled also in proper way.
     *
     * @return asynchronous kafka operation to be done on the topic
     */
    AsyncKafkaOperation getOperation();

}
