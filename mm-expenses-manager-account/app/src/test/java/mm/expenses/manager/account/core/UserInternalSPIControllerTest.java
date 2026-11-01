package mm.expenses.manager.account.core;

import mm.expenses.manager.account.AccountApplicationTest;
import mm.expenses.manager.account.core.user.RegisterUserBySPI;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.account.processor.UserHelper;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.utils.security.UserPermissions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class UserInternalSPIControllerTest extends AccountApplicationTest {

    @Nested
    class FindByKeycloakId {

        @Test
        void shouldFindByKeycloakId() throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();
            final var expectedPermission = Permission.getUserInfo;
            final var expectedGroup = expectedPermission.getGroup();
            expectedUser.setPermissions(new UserPermissions(Map.of(
                    expectedPermission.getGroup().toString(), Set.of(expectedPermission)
            )));

            final var spiUserId = UUID.randomUUID();
            when(currentUserProvider.getSecurityContext()).thenReturn(securityContext);
            when(securityContext.getUserId()).thenReturn(spiUserId);
            when(securityContext.isSpiRequest()).thenReturn(true);

            // when
            when(userRepository.findByIdOrKeycloakIdAndIsDeleted(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(Boolean.class))).thenReturn(Optional.of(expectedUser));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get("/api/internal/spi/users/{id}".replace("{id}", expectedUser.getKeycloakId().toString()))
                                    .contentType(DATA_FORMAT_JSON)
                    )

                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(jsonPath("$.id", is(expectedUser.getId().toString())))
                    .andExpect(jsonPath("$.keycloakId", is(expectedUser.getKeycloakId().toString())))
                    .andExpect(jsonPath("$.email", is(expectedUser.getEmail())))
                    .andExpect(jsonPath("$.username", is(expectedUser.getUsername())))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.lastModifiedAt").isNotEmpty())

                    .andExpect(jsonPath("$.details").isNotEmpty())
                    .andExpect(jsonPath("$.details", aMapWithSize(expectedUser.getDetails().size())))

                    .andExpect(jsonPath("$.details", hasKey("firstName")))
                    .andExpect(jsonPath("$.details.firstName", is(expectedUser.getDetails().get("firstName").toString())))

                    .andExpect(jsonPath("$.details", hasKey("lastName")))
                    .andExpect(jsonPath("$.details.lastName", is(expectedUser.getDetails().get("lastName").toString())))

                    .andExpect(jsonPath("$.permissions").isNotEmpty())
                    .andExpect(jsonPath("$.permissions", aMapWithSize(expectedUser.getPermissions().size())))

                    .andExpect(jsonPath("$.permissions", hasKey(expectedGroup.toString())))
                    .andExpect(jsonPath("$.permissions", aMapWithSize(expectedUser.getPermissions().get(expectedGroup.toString()).size())))

                    .andExpect(jsonPath("$.permissions." + expectedGroup).isNotEmpty())
                    .andExpect(jsonPath("$.permissions." + expectedGroup, hasItem(expectedPermission.toString())));
        }

        @Test
        void shouldNotFindByKeycloakId() throws Exception {
            // given
            final var userId = UUID.randomUUID().toString();
            final var spiUserId = UUID.randomUUID();
            when(currentUserProvider.getSecurityContext()).thenReturn(securityContext);
            when(securityContext.getUserId()).thenReturn(spiUserId);
            when(securityContext.isSpiRequest()).thenReturn(true);

            // when && then
            mockMvc.perform(
                            MockMvcRequestBuilders.get("/api/internal/spi/users/{id}".replace("{id}", userId))
                                    .contentType(DATA_FORMAT_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority(Permission.getUserInfo.getPermission())))
                    )
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(jsonPath("$.message", is(UserExceptionMessage.USER_NOT_FOUND.withParameters(userId).getMessage())));
        }

    }

    @Nested
    class RegisterUser {

        @Test
        void shouldRegisterUser() throws Exception {
            // given
            final var userCaptor = ArgumentCaptor.forClass(User.class);
            final var spiUserId = UUID.randomUUID();

            final var userId = UUID.randomUUID();
            final var email = String.format("%s@test.com", userId);
            final var userName = String.format("UN-%s", userId);
            final var userFirstName = String.format("FN-%s", userId);
            final var userLastName = String.format("LN-%s", userId);

            final var expectedUserToRegister = createRegisterUserBySPIRequest(userFirstName, userLastName, userId, email, userName);
            final var expectedUser = createExpectedUser(expectedUserToRegister);
            final var expectedPermissions = Arrays.stream(Permission.values()).collect(Collectors.toSet())
                    .stream().filter(Permission::isInitial)
                    .collect(Collectors.groupingBy(permission -> permission.getGroup().name(), Collectors.toSet()));

            // when
            when(userRepository.findByKeycloakId(ArgumentMatchers.any(UUID.class))).thenReturn(Optional.empty());
            when(currentUserProvider.getSecurityContext()).thenReturn(securityContext);
            when(securityContext.getUserId()).thenReturn(spiUserId);
            when(securityContext.isSpiRequest()).thenReturn(true);
            when(userRepository.save(userCaptor.capture())).thenReturn(expectedUser);

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.post("/api/internal/spi/users")
                                    .contentType(DATA_FORMAT_JSON)
                                    .content(objectMapper.writeValueAsString(expectedUserToRegister))
                                    .with(csrf())
                    )

                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated())

                    .andExpect(jsonPath("$.id", is(expectedUser.getId().toString())))
                    .andExpect(jsonPath("$.keycloakId", is(expectedUser.getKeycloakId().toString())))
                    .andExpect(jsonPath("$.email", is(expectedUser.getEmail())))
                    .andExpect(jsonPath("$.username", is(expectedUser.getUsername())))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.lastModifiedAt").isNotEmpty())

                    .andExpect(jsonPath("$.details").isNotEmpty())
                    .andExpect(jsonPath("$.details", aMapWithSize(expectedUser.getDetails().size())))

                    .andExpect(jsonPath("$.details", hasKey("firstName")))
                    .andExpect(jsonPath("$.details.firstName", is(expectedUser.getDetails().get("firstName").toString())))

                    .andExpect(jsonPath("$.details", hasKey("lastName")))
                    .andExpect(jsonPath("$.details.lastName", is(expectedUser.getDetails().get("lastName").toString())));

            final var savedUser = userCaptor.getValue();
            assertThat(savedUser.getKeycloakId()).isEqualTo(expectedUser.getKeycloakId());
            assertThat(savedUser.getEmail()).isEqualTo(expectedUser.getEmail());
            assertThat(savedUser.getUsername()).isEqualTo(expectedUser.getUsername());
            assertThat(savedUser.getCreatedAt()).isEqualTo(expectedUser.getCreatedAt());
            assertThat(savedUser.getLastModifiedAt()).isEqualTo(expectedUser.getLastModifiedAt());

            assertThat(savedUser.getDetails()).containsAllEntriesOf(expectedUser.getDetails());
            assertThat(savedUser.getDetails()).containsKey("rawDataCreation");

            assertThat(savedUser.getPermissions()).isNotNull()
                    .isNotEmpty()
                    .containsAllEntriesOf(expectedPermissions);
        }

        @Test
        void shouldNotRegisterUser_whenUserAlreadyExists() throws Exception {
            // given
            final var spiUserId = UUID.randomUUID();

            final var userId = UUID.randomUUID();
            final var email = String.format("%s@test.com", userId);
            final var userName = String.format("UN-%s", userId);
            final var userFirstName = String.format("FN-%s", userId);
            final var userLastName = String.format("LN-%s", userId);

            final var expectedUserToRegister = createRegisterUserBySPIRequest(userFirstName, userLastName, userId, email, userName);
            final var existingUser = createExpectedUser(expectedUserToRegister);

            // when
            when(userRepository.findByKeycloakId(ArgumentMatchers.any(UUID.class))).thenReturn(Optional.of(existingUser));
            when(currentUserProvider.getSecurityContext()).thenReturn(securityContext);
            when(securityContext.getUserId()).thenReturn(spiUserId);
            when(securityContext.isSpiRequest()).thenReturn(true);

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.post("/api/internal/spi/users")
                                    .contentType(DATA_FORMAT_JSON)
                                    .content(objectMapper.writeValueAsString(expectedUserToRegister))
                                    .with(csrf())
                    )

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(jsonPath("$.message", is(UserExceptionMessage.USER_ALREADY_EXISTS.getMessage())));
        }

    }

    private User createExpectedUser(final RegisterUserBySPI expectedUserToRegister) {
        final var details = new HashMap<String, Object>();
        details.put("firstName", expectedUserToRegister.getAttributes().getFirstName().orElse(null));
        details.put("lastName", expectedUserToRegister.getAttributes().getLastName().orElse(null));

        final var userId = UUID.fromString(expectedUserToRegister.getUserId());
        final var expectedUser = UserHelper.createUser();
        expectedUser.setId(userId);
        expectedUser.setKeycloakId(userId);
        expectedUser.setEmail(expectedUserToRegister.getEmail());
        expectedUser.setUsername(expectedUserToRegister.getUsername());
        expectedUser.setEmailVerified(true);
        expectedUser.setEnabled(true);
        expectedUser.setCreatedAt(expectedUserToRegister.getTimeAsInstant());
        expectedUser.setLastModifiedAt(expectedUserToRegister.getTimeAsInstant());
        expectedUser.setDetails(details);

        return expectedUser;
    }

    private RegisterUserBySPI createRegisterUserBySPIRequest(final String userFirstName, final String userLastName, final UUID userId, final String email, final String userName) {
        final var attributes = new RegisterUserBySPI.RegisterUserAttributesBySPI();
        attributes.put("firstName", List.of(userFirstName));
        attributes.put("lastName", List.of(userLastName));

        final var expectedUserToRegister = new RegisterUserBySPI();
        expectedUserToRegister.setUserId(userId.toString());
        expectedUserToRegister.setEmail(email);
        expectedUserToRegister.setUsername(userName);
        expectedUserToRegister.setIsEmailVerified(true);
        expectedUserToRegister.setIsEnabled(true);
        expectedUserToRegister.setTime(Instant.now().getEpochSecond());
        expectedUserToRegister.setAttributes(attributes);

        return expectedUserToRegister;
    }

}