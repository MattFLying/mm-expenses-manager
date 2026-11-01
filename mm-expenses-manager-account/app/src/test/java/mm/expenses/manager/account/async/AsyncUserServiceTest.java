package mm.expenses.manager.account.async;

import mm.expenses.manager.account.async.message.UserRegistrationMessage;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AsyncUserServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRegistrationMessage requestMessage;

    @Mock
    private User expectedUser;

    @InjectMocks
    private AsyncUserService asyncUserService;

    @Test
    public void shouldCreateUserFromEventSuccessfully() {
        // given
        when(userService.create(requestMessage)).thenReturn(expectedUser);

        // when
        final var result = asyncUserService.createFromEvent(requestMessage);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedUser);
        verify(userService, times(1)).create(requestMessage);
    }

    @Test
    public void shouldPropagateException_whenUserServiceFails() {
        // given
        when(userService.create(any(UserRegistrationMessage.class))).thenThrow(new RuntimeException("Error"));

        // when & then
        assertThatThrownBy(() -> asyncUserService.createFromEvent(requestMessage))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Error");

        verify(userService, times(1)).create(requestMessage);
    }

}