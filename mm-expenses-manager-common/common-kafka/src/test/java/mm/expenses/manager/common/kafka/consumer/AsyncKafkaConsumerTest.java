package mm.expenses.manager.common.kafka.consumer;

import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.kafka.BaseInitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncKafkaConsumerTest extends BaseInitTest {

    @ParameterizedTest
    @ArgumentsSource(AsyncKafkaOperationArgument.class)
    void getOperationOrUndefined_mappedOperationIsSame(final AsyncKafkaOperation operation) throws Exception {
        // given
        final var consumer = new AsyncKafkaConsumer() {
        };

        // then
        final var result = consumer.getOperationOrUndefined(operation);

        // then
        assertThat(result).isEqualTo(operation);
    }

    @Test
    void getOperationOrUndefined_mappedOperationIsUndefinedIfPassedNull() throws Exception {
        // given
        final var consumer = new AsyncKafkaConsumer() {
        };

        // then
        final var result = consumer.getOperationOrUndefined(null);

        // then
        assertThat(result).isEqualTo(AsyncKafkaOperation.UNDEFINED);
    }

}