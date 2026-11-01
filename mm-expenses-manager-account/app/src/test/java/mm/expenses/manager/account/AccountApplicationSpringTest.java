package mm.expenses.manager.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@RunWith(JUnitParamsRunner.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = AccountApplication.class)
public abstract class AccountApplicationSpringTest extends BaseInitTest {

    @Autowired
    protected ObjectMapper objectMapper;

}
