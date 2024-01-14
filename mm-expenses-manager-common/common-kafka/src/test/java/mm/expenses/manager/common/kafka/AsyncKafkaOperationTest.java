package mm.expenses.manager.common.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncKafkaOperationTest extends BaseInitTest {

    @ParameterizedTest
    @ArgumentsSource(AsyncKafkaOperationArgument.class)
    void of_mappedOperationIsSame(final AsyncKafkaOperation operation) throws Exception {
        // then
        final var result = AsyncKafkaOperation.of(operation);

        // then
        assertThat(result).isEqualTo(operation);
    }

    @Test
    void of_mappedOperationIsUndefinedIfPassedNull() throws Exception {
        // then
        final var result = AsyncKafkaOperation.of(null);

        // then
        assertThat(result).isEqualTo(AsyncKafkaOperation.UNDEFINED);
    }

}