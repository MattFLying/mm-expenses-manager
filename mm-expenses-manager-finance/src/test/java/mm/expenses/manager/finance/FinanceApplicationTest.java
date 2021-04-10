package mm.expenses.manager.finance;

import junitparams.JUnitParamsRunner;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@RunWith(JUnitParamsRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FinanceApplication.class)
public class FinanceApplicationTest extends BaseInitTest {

    public static final MediaType DATA_FORMAT_JSON = MediaType.APPLICATION_JSON;

    public static final String DATA_FORMAT_JSON_NAME = DATA_FORMAT_JSON.getSubtype();

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    protected MockMvc mockMvc;

}
