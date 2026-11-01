package mm.expenses.manager.account.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.account.AccountApplicationTest;
import mm.expenses.manager.account.exception.UserExceptionMessage;
import mm.expenses.manager.account.processor.UserHelper;
import mm.expenses.manager.account.processor.search.UserFilter;
import mm.expenses.manager.common.utils.security.Permission;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class UserControllerTest extends AccountApplicationTest {

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
                            MockMvcRequestBuilders.get(urlPath("/users"))
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

        @ValueSource(booleans = {true, false})
        @ParameterizedTest
        void shouldFindAll_byIsDeleted(final Boolean isDeleted) throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();
            expectedUser.setDeleted(isDeleted);

            // when
            when(userRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedUser)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(urlPath("/users"))
                                    .param(UserFilter.IS_DELETED_PROPERTY, isDeleted.toString())
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

        @ParameterizedTest
        @EnumSource(TextOperationRequest.class)
        void shouldFindAll_byUserName(final TextOperationRequest operation) throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();

            // when
            when(userRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedUser)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(urlPath("/users"))
                                    .param(UserFilter.USERNAME_PROPERTY, expectedUser.getUsername())
                                    .param(UserFilter.USERNAME_OPERATION_PROPERTY, operation.name())
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

        @ParameterizedTest
        @EnumSource(TextOperationRequest.class)
        void shouldFindAll_byEmail(final TextOperationRequest operation) throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();

            // when
            when(userRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedUser)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(urlPath("/users"))
                                    .param(UserFilter.EMAIL_PROPERTY, expectedUser.getEmail())
                                    .param(UserFilter.EMAIL_OPERATION_PROPERTY, operation.name())
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

        @ParameterizedTest
        @EnumSource(TextOperationRequest.class)
        void shouldFindAll_byUserNameAndEmail(final TextOperationRequest operation) throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();

            // when
            when(userRepository.findAll(ArgumentMatchers.any(Specification.class), ArgumentMatchers.any(Pageable.class))).thenReturn(new PageImpl<>(List.of(expectedUser)));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(urlPath("/users"))
                                    .param(UserFilter.USERNAME_PROPERTY, expectedUser.getUsername())
                                    .param(UserFilter.USERNAME_OPERATION_PROPERTY, operation.name())
                                    .param(UserFilter.EMAIL_PROPERTY, expectedUser.getEmail())
                                    .param(UserFilter.EMAIL_OPERATION_PROPERTY, operation.name())
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

    @Nested
    class FindById {

        @Test
        void shouldFindByUserId() throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();

            // when
            when(userRepository.findByIdOrKeycloakIdAndIsDeleted(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(Boolean.class))).thenReturn(Optional.of(expectedUser));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(urlPath("/users/{id}").replace("{id}", expectedUser.getId().toString()))
                                    .contentType(DATA_FORMAT_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority(Permission.getUserInfo.getPermission())))
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
                    .andExpect(jsonPath("$.details.lastName", is(expectedUser.getDetails().get("lastName").toString())));
        }

        @Test
        void shouldFindByKeycloakId() throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();

            // when
            when(userRepository.findByIdOrKeycloakIdAndIsDeleted(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(Boolean.class))).thenReturn(Optional.of(expectedUser));

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(urlPath("/users/{id}").replace("{id}", expectedUser.getKeycloakId().toString()))
                                    .contentType(DATA_FORMAT_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority(Permission.getUserInfo.getPermission())))
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
                    .andExpect(jsonPath("$.details.lastName", is(expectedUser.getDetails().get("lastName").toString())));
        }

        @Test
        void shouldNotFindById() throws Exception {
            // given
            final var userId = UUID.randomUUID().toString();

            // when && then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(urlPath("/users/{id}").replace("{id}", userId))
                                    .contentType(DATA_FORMAT_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority(Permission.getUserInfo.getPermission())))
                    )
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.content().contentType(DATA_FORMAT_JSON))
                    .andExpect(jsonPath("$.message", is(UserExceptionMessage.USER_NOT_FOUND.withParameters(userId).getMessage())));
        }

    }

    @Nested
    class FindOwnAccount {

        @Test
        void shouldReturnDetailsForOwnAccount() throws Exception {
            // given
            final var expectedUser = UserHelper.createUser();

            // when
            when(userRepository.findByIdOrKeycloakIdAndIsDeleted(ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(UUID.class), ArgumentMatchers.any(Boolean.class))).thenReturn(Optional.of(expectedUser));
            when(currentUserProvider.getSecurityContext()).thenReturn(securityContext);
            when(securityContext.getUserId()).thenReturn(expectedUser.getId());

            // then
            mockMvc.perform(
                            MockMvcRequestBuilders.get(urlPath("/users/own"))
                                    .contentType(DATA_FORMAT_JSON)
                                    .with(jwt().authorities(new SimpleGrantedAuthority(Permission.getOwnAccountInfo.getPermission())))
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
                    .andExpect(jsonPath("$.details.lastName", is(expectedUser.getDetails().get("lastName").toString())));
        }

    }

    @Test
    public void shouldCheckMethodsPreAuthorizeAnnotationValue() {
        // given
        final var methodsByPreAuthorizeValue = Arrays.stream(UserController.class.getDeclaredMethods())
                .filter(method -> Objects.nonNull(AnnotationUtils.getAnnotation(method, PreAuthorize.class)))
                .toList()
                .size();
        final var methodsWithPermissions = Map.of(
                "findAll", List.of(Permission.getUsersInfo),
                "findById", List.of(Permission.getUserInfo),
                "findOwnAccount", List.of(Permission.getOwnAccountInfo)
        );

        // when && then
        shouldCheckMethodsPreAuthorizeAnnotationValue(UserController.class, methodsByPreAuthorizeValue, methodsWithPermissions);
    }

    private String urlPath(final String path) {
        return urlPath("/api/v1", path);
    }

    @Getter
    @RequiredArgsConstructor
    public enum TextOperationRequest {

        equal("equal"),

        notEqual("notEqual"),

        startsWith("startsWith"),

        endsWith("endsWith"),

        contains("contains");

        private final String value;

    }

}