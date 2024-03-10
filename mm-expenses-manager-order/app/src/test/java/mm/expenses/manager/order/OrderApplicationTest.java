package mm.expenses.manager.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import mm.expenses.manager.common.beans.async.AsyncMessageProducer;
import mm.expenses.manager.order.order.OrderRepository;
import mm.expenses.manager.order.product.ProductRepository;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.reset;

@AutoConfigureMockMvc
@RunWith(JUnitParamsRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = OrderApplication.class)
public class OrderApplicationTest extends BaseInitTest {

    public static final MediaType DATA_FORMAT_JSON = MediaType.APPLICATION_JSON;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected ProductRepository productRepository;

    @MockBean
    protected OrderRepository orderRepository;

    @MockBean
    protected AsyncMessageProducer asyncProducer;

    @Override
    protected void setupAfterEachTest() {
        reset(productRepository);
        reset(orderRepository);
        reset(asyncProducer);
    }

}
