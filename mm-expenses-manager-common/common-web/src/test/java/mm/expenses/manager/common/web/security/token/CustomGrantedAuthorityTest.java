package mm.expenses.manager.common.web.security.token;

import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.utils.security.PermissionGroup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomGrantedAuthorityTest {

    @Mock
    private Permission anotherPermission;

    @Mock
    private PermissionGroup group;

    @Mock
    private PermissionGroup anotherGroup;

    @Test
    void shouldThrowException_whenPermissionIsNull() {
        assertThatThrownBy(() -> new CustomGrantedAuthority(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("A granted permission is required");
    }

    @EnumSource(Permission.class)
    @ParameterizedTest
    void shouldInitializeFieldsAndReturnCorrectValues(final Permission permission) {
        // when
        final var authority = new CustomGrantedAuthority(permission);

        // then
        assertThat(authority.getPermission()).isEqualTo(permission);
        assertThat(authority.getGroup()).isEqualTo(permission.getGroup());
        assertThat(authority.getRole()).isEqualTo(permission.getRole());
        assertThat(authority.getAuthority()).isEqualTo(permission.getPermission());
        assertThat(authority.toString()).isEqualTo(permission.getRole());
    }

    @EnumSource(Permission.class)
    @ParameterizedTest
    void shouldBeEqualToSelf(final Permission permission) {
        // when
        final var authority = new CustomGrantedAuthority(permission);

        // then
        assertThat(authority.equals(authority)).isTrue();
    }

    @EnumSource(Permission.class)
    @ParameterizedTest
    void shouldNotBeEqualNullOrDifferentClass(final Permission permission) {
        // when
        final var authority = new CustomGrantedAuthority(permission);

        // then
        assertThat(authority.equals(null)).isFalse();
        assertThat("Some String".equals(authority)).isFalse();
    }

    @EnumSource(Permission.class)
    @ParameterizedTest
    void shouldBeEqual_whenAllComponentsMatch(final Permission permission) {
        // when
        final var authority1 = new CustomGrantedAuthority(permission);
        final var authority2 = new CustomGrantedAuthority(permission);

        // then
        assertThat(authority1).isEqualTo(authority2);
        assertThat(authority1.hashCode()).isEqualTo(authority2.hashCode());
    }

    @EnumSource(Permission.class)
    @ParameterizedTest
    void shouldNotBeEqual_whenPermissionsDiffer(final Permission permission) {
        // given
        when(anotherPermission.getGroup()).thenReturn(group);
        when(anotherPermission.getRole()).thenReturn("ROLE_ADMIN");

        // when
        final var authority1 = new CustomGrantedAuthority(permission);
        final var authority2 = new CustomGrantedAuthority(anotherPermission);

        // then
        assertThat(authority1).isNotEqualTo(authority2);
    }

    @EnumSource(Permission.class)
    @ParameterizedTest
    void shouldNotBeEqual_whenGroupsDiffer(final Permission permission) {
        // given
        when(anotherPermission.getGroup()).thenReturn(anotherGroup);
        when(anotherPermission.getRole()).thenReturn("ROLE_ADMIN");

        // when
        final var authority1 = new CustomGrantedAuthority(permission);
        final var authority2 = new CustomGrantedAuthority(anotherPermission);

        // then
        assertThat(authority1).isNotEqualTo(authority2);
    }

    @EnumSource(Permission.class)
    @ParameterizedTest
    void shouldNotBeEqual_whenRolesDiffer(final Permission permission) {
        // given
        when(anotherPermission.getGroup()).thenReturn(group);
        when(anotherPermission.getRole()).thenReturn("ROLE_USER");

        // when
        final var authority1 = new CustomGrantedAuthority(permission);
        final var authority2 = new CustomGrantedAuthority(anotherPermission);

        // then
        assertThat(authority1).isNotEqualTo(authority2);
    }

}
