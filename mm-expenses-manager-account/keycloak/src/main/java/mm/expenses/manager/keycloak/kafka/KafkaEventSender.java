package mm.expenses.manager.keycloak.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.keycloak.request.RequestSender;
import mm.expenses.manager.keycloak.exception.KeycloakSpiException;
import mm.expenses.manager.keycloak.exception.KeycloakSpiExceptionMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Objects;

/**
 * Request sender responsible for sending event via Kafka
 */
@Slf4j
abstract class KafkaEventSender implements RequestSender {

    protected final KafkaProducer<String, String> producer;
    protected final ObjectMapper mapper;

    protected KafkaEventSender(final KafkaProducer<String, String> producer, final ObjectMapper mapper) {
        this.producer = producer;
        this.mapper = mapper;
    }

    @Override
    public boolean isAvailable() {
        return Objects.nonNull(producer);
    }

    /**
     * Create a record to be sent to Kafka which is a simple pair of the topic and message.
     *
     * @param topic - the topic the record will be sent to
     * @param key   - the key that will be included in the message, some specific value to be identified by
     * @param value - expected message to be sent which will be mapped into JSON value
     * @return prepared record to be sent via Kafka
     * @throws JsonProcessingException if something is wrong with mapping the message into JSON value
     */
    protected ProducerRecord<String, String> record(final String topic, final String key, final Object value) throws JsonProcessingException {
        try {
            if (StringUtils.isEmpty(topic)) {
                log.error("[KEYCLOAK-SPI] For passed key: {} topic is missing with the message: {}", key, value);
                throw new KeycloakSpiException(KeycloakSpiExceptionMessage.KAFKA_TOPIC_MISSING);
            }
            if (Objects.isNull(value)) {
                log.error("[KEYCLOAK-SPI] For passed key: {} and topic: {} the message is missing", key, topic);
                throw new KeycloakSpiException(KeycloakSpiExceptionMessage.KAFKA_MESSAGE_MISSING);
            }
            return new ProducerRecord<>(topic, key, mapper.writeValueAsString(value));
        } catch (final JsonProcessingException exception) {
            log.error("[KEYCLOAK-SPI] For passed key: {} and topic: {} the message: {} could not be correctly mapped into JSON", key, topic, value);
            throw new KeycloakSpiException(KeycloakSpiExceptionMessage.KAFKA_MESSAGE_JSON_MAPPING, exception);
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Unknown error occurred during Kafka producer record creation for topic: {}, key: {} and message: {}", topic, key, exception.getMessage());
            throw new KeycloakSpiException(KeycloakSpiExceptionMessage.UNKNOWN_ERROR, exception);
        }
    }

}
