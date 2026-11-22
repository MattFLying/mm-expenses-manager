package mm.expenses.manager.order;

import mm.expenses.manager.common.async.AsyncMessageProducer;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.web.security.CurrentUserProvider;
import mm.expenses.manager.common.web.security.SecurityContext;
import mm.expenses.manager.order.client.FinanceApiClient;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.price.PriceConverter;
import mm.expenses.manager.order.product.ProductRepository;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
public class SecuredOrderApplicationTest extends OrderApplicationSpringTest {

    public static final MediaType DATA_FORMAT_JSON = MediaType.APPLICATION_JSON;

    protected static final UUID KEYCLOAK_ID = UUID.randomUUID();
    protected static final UUID USER_ID = UUID.randomUUID();

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    protected final Set<Permission> permissions = new HashSet<>();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PriceConverter priceConverter;

    @MockitoBean
    protected ProductRepository productRepository;

    @MockitoBean
    protected OrderRepository orderRepository;

    @MockitoBean
    protected AsyncMessageProducer asyncProducer;

    @MockitoBean
    protected FinanceApiClient financeApiClient;

    @MockitoBean
    private CurrentUserProvider currentUserProvider;

    @Override
    protected void setupBeforeEachTest() {
        final var securityContext = SecurityContext.context(
                null, null,
                KEYCLOAK_ID, USER_ID,
                null, null,
                permissions
        );

        when(currentUserProvider.getSecurityContext()).thenReturn(securityContext);
    }

    @Override
    protected void setupAfterEachTest() {
        reset(productRepository);
        reset(orderRepository);
        reset(asyncProducer);
        permissions.clear();
    }

}
