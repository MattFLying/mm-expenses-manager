package mm.expenses.manager.keycloak;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.utils.config.ObjectMapperConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * Keycloak listener factory provider to be registered directly by Keycloak in realms/events view
 * and to be defined in META-INF
 */
@Slf4j
public class KeycloakEventsListenerFactory implements EventListenerProviderFactory {

    public static final String ACCOUNT_SERVICE_SPI_URL_PROPERTY = "ACCOUNT_SERVICE_SPI_URL";
    public static final String ACCOUNT_SERVICE_SPI_DEFAULT_VALUE = "http://host.docker.internal:8187/api/internal/spi";

    public static final String KEYCLOAK_SPI_ORIGIN_HEADER_PROPERTY = "KEYCLOAK_SPI_ORIGIN_HEADER";
    public static final String KEYCLOAK_SPI_ORIGIN_HEADER_DEFAULT_VALUE = "system-spi";

    public static final String HTTP_CONNECTION_TIMEOUT_IN_SECONDS_PROPERTY = "HTTP_CONNECTION_TIMEOUT_IN_SECONDS";
    public static final int HTTP_CONNECTION_TIMEOUT_IN_SECONDS_DEFAULT_VALUE = 5;

    private static final String KAFKA_BOOTSTRAP_SERVER_PROPERTY = "KAFKA_BOOTSTRAP_SERVER";
    private static final String KAFKA_BOOTSTRAP_SERVER_DEFAULT_VALUE = "kafka:29092";

    static final String PROVIDER_ID = "em_keycloak-event-listener";

    private ObjectMapper mapper;
    private KafkaProducer<String, String> producer;

    private InternalProperties internalProperties;

    public KeycloakEventsListenerFactory() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public EventListenerProvider create(final KeycloakSession session) {
        return new KeycloakEventsListener(session, this.producer, this.mapper, this.internalProperties);
    }

    @Override
    public void init(final Config.Scope config) {
        try {
            // properties for internal usage initialization
            this.internalProperties = new InternalProperties();

            // configure common mapper
            this.mapper = ObjectMapperConfig.objectMapper();

            // configure access to Kafka
            final var properties = new Properties();
            final var kafkaBootstrapServer = internalProperties.getEnvironmentVariable(KAFKA_BOOTSTRAP_SERVER_PROPERTY, KAFKA_BOOTSTRAP_SERVER_DEFAULT_VALUE);
            this.internalProperties.setKafkaBootstrapServer(String.valueOf(kafkaBootstrapServer));

            properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.internalProperties.getKafkaBootstrapServer());
            properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
            properties.put(ProducerConfig.ACKS_CONFIG, "1");
            properties.put(ProducerConfig.RETRIES_CONFIG, 3);

            this.producer = new KafkaProducer<>(properties);

            // configure access to account-service via HTTP
            final var accountServiceUrl = internalProperties.getEnvironmentVariable(ACCOUNT_SERVICE_SPI_URL_PROPERTY, ACCOUNT_SERVICE_SPI_DEFAULT_VALUE);
            final var originHeader = internalProperties.getEnvironmentVariable(KEYCLOAK_SPI_ORIGIN_HEADER_PROPERTY, KEYCLOAK_SPI_ORIGIN_HEADER_DEFAULT_VALUE);
            final var connectionTimeout = internalProperties.getEnvironmentVariable(HTTP_CONNECTION_TIMEOUT_IN_SECONDS_PROPERTY, HTTP_CONNECTION_TIMEOUT_IN_SECONDS_DEFAULT_VALUE);

            this.internalProperties.setAccountServiceUrl(String.valueOf(accountServiceUrl));
            this.internalProperties.setOriginHeader(String.valueOf(originHeader));
            this.internalProperties.setHttpConnectionTimeoutInSeconds(Integer.parseInt(String.valueOf(connectionTimeout)));
        } catch (final Exception exception) {
            log.error("[KEYCLOAK-SPI] Cannot initialize Keycloak events listener factory with error: {}", exception.getMessage(), exception);
        }
    }

    @Override
    public void postInit(final KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {
        if (Objects.nonNull(this.mapper)) {
            this.mapper.clearCaches();
        }
        if (Objects.nonNull(this.producer)) {
            this.producer.close();
        }
        if (Objects.nonNull(this.internalProperties)) {
            this.internalProperties.clean();
        }
    }

}
