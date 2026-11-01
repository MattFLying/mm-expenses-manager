package mm.expenses.manager.keycloak.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.keycloak.model.UserRegisteredModel;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.UserModel;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserKafkaSenderTest {

    private static final String EVENT_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();

    @Mock
    private KafkaProducer<String, String> producer;

    @Mock
    private ObjectMapper mapper;

    @Mock
    private Event event;

    @Mock
    private UserModel user;

    @Mock
    private RecordMetadata recordMetadata;

    private UserKafkaSender userKafkaSender;

    @BeforeEach
    public void setUp() {
        userKafkaSender = new UserKafkaSender(producer, mapper);
    }

    @Test
    public void registerUser_shouldRegisterUserSuccessfully() throws Exception {
        // given
        final var jsonPayload = "{\"some\":\"json\"}";

        when(event.getUserId()).thenReturn(USER_ID);
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(mapper.writeValueAsString(any(UserRegisteredModel.class))).thenReturn(jsonPayload);

        // when
        userKafkaSender.registerUser(event, user);

        // then
        final var recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(producer).send(recordCaptor.capture(), any(Callback.class));

        final var capturedRecord = recordCaptor.getValue();
        assertThat(capturedRecord.topic()).isEqualTo(UserKafkaSender.USER_REGISTER_TOPIC);
        assertThat(capturedRecord.key()).isEqualTo(USER_ID);
        assertThat(capturedRecord.value()).isEqualTo(jsonPayload);
    }

    @Test
    public void registerUser_shouldHandleKafkaSendCallbackError() throws Exception {
        // given
        final var jsonPayload = "{\"some\":\"json\"}";

        when(event.getUserId()).thenReturn(USER_ID);
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(mapper.writeValueAsString(any())).thenReturn(jsonPayload);

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onCompletion(null, new RuntimeException("Kafka broker down"));
            return null;
        }).when(producer).send(any(ProducerRecord.class), any(Callback.class));

        // when
        userKafkaSender.registerUser(event, user);

        // then
        verify(producer).send(any(ProducerRecord.class), any(Callback.class));
    }

    @Test
    public void registerUser_shouldHandleKafkaSendCallbackSuccess() throws Exception {
        // given
        final var jsonPayload = "{\"some\":\"json\"}";

        when(event.getUserId()).thenReturn(USER_ID);
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(mapper.writeValueAsString(any())).thenReturn(jsonPayload);

        doAnswer(invocation -> {
            Callback callback = invocation.getArgument(1);
            callback.onCompletion(recordMetadata, null);
            return null;
        }).when(producer).send(any(ProducerRecord.class), any(Callback.class));

        // when
        userKafkaSender.registerUser(event, user);

        // then
        verify(producer).send(any(ProducerRecord.class), any(Callback.class));
    }

    @Test
    public void registerUser_shouldCatchJsonProcessingException_whenMappingFails() throws Exception {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(mapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("Mapping error") {
        });

        // when
        userKafkaSender.registerUser(event, user);

        // then
        verify(producer, never()).send(any(), any());
    }

    @Test
    public void registerUser_shouldCatchGenericExceptionDuringRegistration() throws Exception {
        // given
        when(event.getId()).thenReturn(EVENT_ID);
        when(event.getType()).thenReturn(EventType.REGISTER);
        when(mapper.writeValueAsString(any())).thenThrow(new RuntimeException("Unexpected error"));

        // when
        userKafkaSender.registerUser(event, user);

        // then
        verify(producer, never()).send(any(), any());
    }

    @Test
    public void getUser_shouldReturnNull() {
        // when
        final var response = userKafkaSender.getUser(UUID.randomUUID());

        // then
        assertThat(response).isNull();
    }

    @Test
    public void shouldReturnPriority() {
        // when
        final var priority = userKafkaSender.priority();

        // then
        assertThat(priority).isEqualTo(0);
    }

}
