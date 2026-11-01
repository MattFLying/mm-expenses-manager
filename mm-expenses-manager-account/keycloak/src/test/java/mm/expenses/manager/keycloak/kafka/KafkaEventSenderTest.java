package mm.expenses.manager.keycloak.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaEventSenderTest {

    @Mock
    private KafkaProducer<String, String> producer;

    @Mock
    private ObjectMapper mapper;

    private KafkaEventSender eventSender;

    @BeforeEach
    public void setUp() {
        eventSender = new TestKafkaEventSender(producer, mapper);
    }

    @Test
    public void shouldBeAvailable_whenProducerIsNotNull() {
        // when
        final var available = eventSender.isAvailable();

        // then
        assertThat(available).isTrue();
    }

    @Test
    public void shouldNotBeAvailable_whenProducerIsNull() {
        // given
        final var nullProducerSender = new TestKafkaEventSender(null, mapper);

        // when
        final var available = nullProducerSender.isAvailable();

        // then
        assertThat(available).isFalse();
    }

    @Test
    public void shouldCreateRecordSuccessfully() throws Exception {
        // given
        final var topic = "test-topic";
        final var key = "test-key";
        final var payload = "test-message";
        final var jsonPayload = "\"test-message\"";

        when(mapper.writeValueAsString(payload)).thenReturn(jsonPayload);

        // when
        final var record = eventSender.record(topic, key, payload);

        // then
        assertThat(record).isNotNull();
        assertThat(record.topic()).isEqualTo(topic);
        assertThat(record.key()).isEqualTo(key);
        assertThat(record.value()).isEqualTo(jsonPayload);
    }

    @Test
    public void shouldThrowKeycloakSpiException_whenTopicIsEmpty() {
        // given
        final var topic = "";
        final var key = "test-key";
        final var payload = "test-message";

        // when & then
        assertThatThrownBy(() -> eventSender.record(topic, key, payload))
                .isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldThrowKeycloakSpiException_whenValueIsNull() {
        // given
        final var topic = "test-topic";
        final var key = "test-key";

        // when & then
        assertThatThrownBy(() -> eventSender.record(topic, key, null))
                .isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldThrowKeycloakSpiException_whenJsonProcessingFails() throws Exception {
        // given
        final var topic = "test-topic";
        final var key = "test-key";
        final var payload = new Object();

        when(mapper.writeValueAsString(payload)).thenThrow(mock(JsonProcessingException.class));

        // when & then
        assertThatThrownBy(() -> eventSender.record(topic, key, payload))
                .isInstanceOf(KeycloakSpiException.class);
    }

    @Test
    public void shouldThrowKeycloakSpiException_whenUnexpectedErrorOccurs() throws Exception {
        // given
        final var topic = "test-topic";
        final var key = "test-key";
        final var payload = "test-message";

        when(mapper.writeValueAsString(payload)).thenThrow(new RuntimeException("Unexpected error"));

        // when & then
        assertThatThrownBy(() -> eventSender.record(topic, key, payload))
                .isInstanceOf(KeycloakSpiException.class);
    }

    private static class TestKafkaEventSender extends KafkaEventSender {

        public TestKafkaEventSender(final KafkaProducer<String, String> producer, final ObjectMapper mapper) {
            super(producer, mapper);
        }

    }

}
