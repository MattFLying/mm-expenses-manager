package mm.expenses.manager.common.utils.util;

import mm.expenses.manager.common.utils.BaseInitTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IdUtilsTest extends BaseInitTest {

    @Test
    void generateIdAsUUID_test() throws Exception {
        final var id = IdUtils.generateIdAsUUID();
        final var parsedIdToCompare = UUID.fromString(id.toString());

        assertThat(id).isNotNull().isEqualTo(parsedIdToCompare);
    }

    @Test
    void generateId_test() throws Exception {
        final var id = IdUtils.generateId();
        final var parsedIdToCompare = UUID.fromString(id);

        assertThat(id).isNotNull().isEqualTo(parsedIdToCompare.toString());
    }

}