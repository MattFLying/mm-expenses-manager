package mm.expenses.manager.common.kafka.producer;

import mm.expenses.manager.common.beans.exception.AsyncException;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.kafka.BaseInitTest;
import mm.expenses.manager.common.kafka.exception.KafkaExceptionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncKafkaProducerTest extends BaseInitTest {

    @Test
    void send_messageCorrectlySent() throws Exception {
        // given
        final var value = "Test Value";
        final var message = new TestProducerBindingMessage(value, AsyncKafkaOperation.CREATE);

        // then
        when(streamBridge.send(message.getProducerBindingName(), message)).thenReturn(true);
        producer.send(message);

        // then
        verify(streamBridge, times(1)).send(message.getProducerBindingName(), message);
    }

    @Test
    void send_messageNotSent_throwsAsyncException() throws Exception {
        // given
        final var value = "Test Value";
        final var message = new TestProducerBindingMessage(value, AsyncKafkaOperation.CREATE);

        // then
        when(streamBridge.send(message.getProducerBindingName(), message)).thenReturn(false);

        // then
        assertThatThrownBy(() -> producer.send(message))
                .isInstanceOf(AsyncException.class)
                .hasMessage(KafkaExceptionMessage.ASYNC_PRODUCER_MESSAGE_SEND_FAILED.getMessage());
    }

    @Test
    void send_messageNotSent_messageIsNull() throws Exception {
        // given
        final TestProducerBindingMessage message = null;

        // when& then
        assertThatThrownBy(() -> producer.send(message))
                .isInstanceOf(AsyncException.class)
                .hasMessage(KafkaExceptionMessage.ASYNC_PRODUCER_MESSAGE_IS_NULL.getMessage());
        verifyNoInteractions(streamBridge);
    }

    @Test
    void send_messageNotSent_bindingIsNull() throws Exception {
        // given
        final TestProducerBindingMessage message = TestProducerBindingMessage.bindingNull();

        // when& then
        assertThatThrownBy(() -> producer.send(message))
                .isInstanceOf(AsyncException.class)
                .hasMessage(KafkaExceptionMessage.ASYNC_PRODUCER_BINDING_OR_TOPIC_IS_NULL.getMessage());
        verifyNoInteractions(streamBridge);
    }

    @Test
    void send_messageNotSent_bindingIsEmptyString() throws Exception {
        // given
        final TestProducerBindingMessage message = TestProducerBindingMessage.bindingEmptyString();

        // when& then
        assertThatThrownBy(() -> producer.send(message))
                .isInstanceOf(AsyncException.class)
                .hasMessage(KafkaExceptionMessage.ASYNC_PRODUCER_BINDING_OR_TOPIC_IS_NULL.getMessage());
        verifyNoInteractions(streamBridge);
    }

    @Test
    void send_messageNotSent_topicIsNull() throws Exception {
        // given
        final TestProducerBindingMessage message = TestProducerBindingMessage.topicNull();

        // when& then
        assertThatThrownBy(() -> producer.send(message))
                .isInstanceOf(AsyncException.class)
                .hasMessage(KafkaExceptionMessage.ASYNC_PRODUCER_BINDING_OR_TOPIC_IS_NULL.getMessage());
        verifyNoInteractions(streamBridge);
    }

    @Test
    void send_messageNotSent_topicIsEmptyString() throws Exception {
        // given
        final TestProducerBindingMessage message = TestProducerBindingMessage.topicEmptyString();

        // when& then
        assertThatThrownBy(() -> producer.send(message))
                .isInstanceOf(AsyncException.class)
                .hasMessage(KafkaExceptionMessage.ASYNC_PRODUCER_BINDING_OR_TOPIC_IS_NULL.getMessage());
        verifyNoInteractions(streamBridge);
    }

}