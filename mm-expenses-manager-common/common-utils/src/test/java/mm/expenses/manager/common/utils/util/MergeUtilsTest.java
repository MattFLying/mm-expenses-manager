package mm.expenses.manager.common.utils.util;

import mm.expenses.manager.common.utils.BaseInitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MergeUtilsTest extends BaseInitTest {

    @Test
    void firstWins_test() throws Exception {
        final var oldValue = "test 1";
        final var newValue = "test 2";

        final var winner = MergeUtils.firstWins(oldValue, newValue);

        assertThat(winner).isNotNull().isEqualTo(oldValue);
    }

    @Test
    void firstWins_oldValueIsNull_returnsNull_test() throws Exception {
        final var newValue = "test";

        final var winner = MergeUtils.firstWins(null, newValue);

        assertThat(winner).isNull();
    }

}