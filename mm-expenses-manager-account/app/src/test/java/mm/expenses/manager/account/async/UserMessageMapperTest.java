package mm.expenses.manager.account.async;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mm.expenses.manager.account.async.message.UserRegistrationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserMessageMapperTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserRegistrationMessage userRegistrationMessage;

    private UserMessageMapper userMessageMapper;

    @BeforeEach
    public void setUp() {
        userMessageMapper = new UserMessageMapper(objectMapper);
    }

    @Test
    public void shouldMapRegistrationMessageSuccessfully() throws Exception {
        // given
        final var jsonMessage = "{\"userId\":\"123\"}";
        when(objectMapper.readValue(jsonMessage, UserRegistrationMessage.class)).thenReturn(userRegistrationMessage);

        // when
        final var result = userMessageMapper.registration(jsonMessage);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(userRegistrationMessage);
    }

    @Test
    public void shouldThrowRuntimeExceptionOnJsonProcessingException() throws Exception {
        // given
        final var invalidJsonMessage = "invalid-json";
        final var jsonProcessingException = mock(JsonProcessingException.class);

        when(objectMapper.readValue(eq(invalidJsonMessage), eq(UserRegistrationMessage.class))).thenThrow(jsonProcessingException);

        // when & then
        assertThatThrownBy(() -> userMessageMapper.registration(invalidJsonMessage))
                .isInstanceOf(RuntimeException.class)
                .hasCause(jsonProcessingException);
    }

    @Test
    public void shouldThrowRuntimeExceptionOnGenericException() throws Exception {
        // given
        final var jsonMessage = "{\"userId\":\"123\"}";
        final var unexpectedException = new RuntimeException("Unexpected IO error");

        when(objectMapper.readValue(eq(jsonMessage), eq(UserRegistrationMessage.class))).thenThrow(unexpectedException);

        // when & then
        assertThatThrownBy(() -> userMessageMapper.registration(jsonMessage))
                .isInstanceOf(RuntimeException.class)
                .hasCause(unexpectedException);
    }

}