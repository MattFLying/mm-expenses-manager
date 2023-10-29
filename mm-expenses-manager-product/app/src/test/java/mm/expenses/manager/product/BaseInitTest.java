package mm.expenses.manager.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Basic initialization class for testing classes.
 */
public abstract class BaseInitTest {

    /**
     * Prepare setup steps for each test before execution or nothing will happen when it won't be overriden
     */
    protected void setupBeforeEachTest() {

    }

    /**
     * Prepare setup steps for each test after execution or nothing will happen when it won't be overriden
     */
    protected void setupAfterEachTest() {

    }

    @BeforeEach
    protected void beforeEachTest() {
        setupBeforeEachTest();
    }

    @AfterEach
    protected void afterEachTest() {
        setupAfterEachTest();
    }

}
