package mm.expenses.manager.finance;

import junitparams.JUnitParamsRunner;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@RunWith(JUnitParamsRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = FinanceApplication.class)
public class FinanceApplicationTest extends BaseInitTest {

    protected static final String PROVIDER_NAME = "nbp";

    @ClassRule
    protected static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    protected final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    protected MockMvc mockMvc;


    @BeforeEach
    protected void beforeEachTest() {
        setupBeforeEachTest();
    }

    @AfterEach
    protected void afterEachTest() {
        setupAfterEachTest();
    }

}
