package mm.expenses.manager.common.beans.async;

import mm.expenses.manager.common.beans.exception.AsyncException;
import mm.expenses.manager.common.beans.exception.BeansExceptionMessage;
import org.junit.jupiter.api.Test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AsyncMessageConsumerTest {

    @Test
    void logMessage_correctlyFormattedMessage() throws Exception {
        // given
        final var consumer = new AsyncMessageConsumer() {
        };
        final var value = "test value";
        final var consumerBinding = new AsyncConsumerBindingTest(value);

        // when
        final var result = consumer.logMessage(consumerBinding);

        // then
        assertThat(result)
                .isEqualTo(format(
                        "Received message on binding: %s, topic: %s, mapped to: %s. Message body: %s",
                        AsyncConsumerBindingTest.BINDING, AsyncConsumerBindingTest.TOPIC,
                        AsyncConsumerBindingTest.class, consumerBinding
                ));
    }

    @Test
    void logMessage_consumerMessageIsNull_throwAsyncConsumerException() throws Exception {
        // given
        final var consumer = new AsyncMessageConsumer() {
        };

        // when& then
        assertThatThrownBy(() -> consumer.logMessage(null))
                .isInstanceOf(AsyncException.class)
                .hasMessage(BeansExceptionMessage.ASYNC_CONSUMER_MESSAGE_IS_NULL.getMessage());
    }

    @Test
    void logMessage_consumerBindingIsNull_throwAsyncConsumerException() throws Exception {
        // given
        final var consumer = new AsyncMessageConsumer() {
        };
        final var consumerBinding = AsyncConsumerBindingTest.bindingNull();

        // when& then
        assertThatThrownBy(() -> consumer.logMessage(consumerBinding))
                .isInstanceOf(AsyncException.class)
                .hasMessage(BeansExceptionMessage.ASYNC_CONSUMER_BINDING_OR_TOPIC_IS_NULL.getMessage());
    }

    @Test
    void logMessage_consumerTopicIsNull_throwAsyncConsumerException() throws Exception {
        // given
        final var consumer = new AsyncMessageConsumer() {
        };
        final var consumerBinding = AsyncConsumerBindingTest.topicNull();

        // when& then
        assertThatThrownBy(() -> consumer.logMessage(consumerBinding))
                .isInstanceOf(AsyncException.class)
                .hasMessage(BeansExceptionMessage.ASYNC_CONSUMER_BINDING_OR_TOPIC_IS_NULL.getMessage());
    }

}