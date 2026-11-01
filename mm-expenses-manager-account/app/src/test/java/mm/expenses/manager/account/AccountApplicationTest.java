package mm.expenses.manager.account;

import mm.expenses.manager.account.core.user.UserRepository;
import mm.expenses.manager.common.async.AsyncMessageProducer;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.web.security.CurrentUserProvider;
import mm.expenses.manager.common.web.security.SecurityContext;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

@AutoConfigureMockMvc
public class AccountApplicationTest extends AccountApplicationSpringTest {

    public static final MediaType DATA_FORMAT_JSON = MediaType.APPLICATION_JSON;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected AsyncMessageProducer asyncProducer;

    @MockitoBean
    protected CurrentUserProvider currentUserProvider;

    protected SecurityContext securityContext = mock(SecurityContext.class);

    @Override
    protected void setupAfterEachTest() {
        reset(userRepository);
        reset(asyncProducer);
        reset(currentUserProvider);
        reset(securityContext);
    }

    protected String urlPath(final String basePath, final String path) {
        return String.format("%s/%s", basePath, path);
    }

    protected void shouldCheckMethodsPreAuthorizeAnnotationValue(final Class<?> clazz, final Integer expectedPreAuthorizeMethodsCount, final Map<String, List<Permission>> expectedPreAuthorizeMethodWithPermissions) {
        // given
        final var methodsByPreAuthorizeValue = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> Objects.nonNull(AnnotationUtils.getAnnotation(method, PreAuthorize.class)))
                .collect(Collectors.toMap(
                        Method::getName,
                        method -> Objects.requireNonNull(AnnotationUtils.getAnnotation(method, PreAuthorize.class)).value()
                ));

        // when && then
        assertThat(methodsByPreAuthorizeValue.size()).isEqualTo(expectedPreAuthorizeMethodsCount);
        assertThat(expectedPreAuthorizeMethodWithPermissions).hasSize(expectedPreAuthorizeMethodsCount);

        expectedPreAuthorizeMethodWithPermissions.forEach((methodName, permissions) -> {
            if (permissions.size() == 1) {
                assertThat(methodsByPreAuthorizeValue.get(methodName)).isEqualTo(preAuthorizeValueFormatter(permissions.get(0)));
            } else {
                permissions.forEach(permission -> {
                    assertThat(methodsByPreAuthorizeValue.get(methodName)).isEqualTo(preAuthorizeValueFormatter(permission));
                });
            }
        });
    }

    private String preAuthorizeValueFormatter(final Permission permission) {
        return String.format("hasAuthority('%s')", permission);
    }

}
