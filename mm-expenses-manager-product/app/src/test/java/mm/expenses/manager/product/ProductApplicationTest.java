package mm.expenses.manager.product;

import mm.expenses.manager.common.async.AsyncMessageProducer;
import mm.expenses.manager.product.client.FinanceApiClient;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.product.ProductRepository;
import org.junit.ClassRule;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.reset;

@AutoConfigureMockMvc
public class ProductApplicationTest extends ProductApplicationSpringTest {

    public static final MediaType DATA_FORMAT_JSON = MediaType.APPLICATION_JSON;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PriceConverter priceConverter;

    @MockBean
    protected ProductRepository productRepository;

    @MockBean
    protected AsyncMessageProducer asyncProducer;

    @MockBean
    protected FinanceApiClient financeApiClient;

    @Override
    protected void setupAfterEachTest() {
        reset(productRepository);
        reset(asyncProducer);
    }

}
