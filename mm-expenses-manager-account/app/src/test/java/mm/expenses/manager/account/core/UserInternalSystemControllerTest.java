package mm.expenses.manager.account.core;

import mm.expenses.manager.account.AccountApplicationTest;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.account.processor.UserHelper;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.utils.security.UserPermissions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class UserInternalSystemControllerTest extends AccountApplicationTest {

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

            final var systemUserId = UUID.randomUUID();
            when(currentUserProvider.getSecurityContext()).thenReturn(securityContext);
            when(securityContext.getUserId()).thenReturn(systemUserId);
            when(securityContext.isSystemRequest()).thenReturn(true);

            // when
            when(userRepository.findByIdOrKeycloakIdAndIsDeleted(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(Boolean.class))).thenReturn(Optional.of(expectedUser));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get("/api/internal/system/users/keycloak/{id}".replace("{id}", expectedUser.getKeycloakId().toString()))
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
            final var systemUserId = UUID.randomUUID();
            when(currentUserProvider.getSecurityContext()).thenReturn(securityContext);
            when(securityContext.getUserId()).thenReturn(systemUserId);
            when(securityContext.isSystemRequest()).thenReturn(true);

            // when && then
            mockMvc.perform(
                            MockMvcRequestBuilders.get("/api/internal/system/users/keycloak/{id}".replace("{id}", userId))
                                    .contentType(DATA_FORMAT_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority(Permission.getUserInfo.getPermission())))
                    )
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(jsonPath("$.message", is(UserExceptionMessage.USER_NOT_FOUND.withParameters(userId).getMessage())));
        }

    }

    @Nested
    class FindAll {

        @Test
        void shouldFindAll() throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();

            // when
            when(userRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedUser)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get("/api/internal/system/users")
                                    .contentType(DATA_FORMAT_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority(Permission.getUsersInfo.getPermission())))
                    )

                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())

                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements", is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.hasNext", is(false)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.first", is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.last", is(true)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.content", hasSize(1)))

                    .andExpect(jsonPath("$.content[0].id", is(expectedUser.getId().toString())))
                    .andExpect(jsonPath("$.content[0].keycloakId", is(expectedUser.getKeycloakId().toString())))
                    .andExpect(jsonPath("$.content[0].email", is(expectedUser.getEmail())))
                    .andExpect(jsonPath("$.content[0].username", is(expectedUser.getUsername())))
                    .andExpect(jsonPath("$.content[0].createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.content[0].lastModifiedAt").isNotEmpty())

                    .andExpect(jsonPath("$.content[0].details").isNotEmpty())
                    .andExpect(jsonPath("$.content[0].details", aMapWithSize(expectedUser.getDetails().size())))

                    .andExpect(jsonPath("$.content[0].details", hasKey("firstName")))
                    .andExpect(jsonPath("$.content[0].details.firstName", is(expectedUser.getDetails().get("firstName").toString())))

                    .andExpect(jsonPath("$.content[0].details", hasKey("lastName")))
                    .andExpect(jsonPath("$.content[0].details.lastName", is(expectedUser.getDetails().get("lastName").toString())));
        }

    }

}