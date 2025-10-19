package mm.expenses.manager.common.postgresql.specification;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SpecificationScannerTest {

    @Test
    void scan_shouldCorrectlyScanClass() {
        // given
        val columnFieldName = "column";
        val nameFieldName = "name";
        val valueFieldName = "value";

        // when
        val result = SpecificationScanner.scan(TestClassToHandlerSpecification.class);

        val selectableFields = result.getSelectedFields();
        val filterableFields = result.getFilteredFields();
        val sortedFields = result.getSortedFields();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPagination()).isNull();

        assertThat(selectableFields).isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .containsExactlyInAnyOrder(columnFieldName);

        assertThat(sortedFields).isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .containsExactlyInAnyOrder(valueFieldName);

        assertThat(filterableFields).isNotNull()
                .isNotEmpty()
                .hasSize(2);

        val filterableNameField = filterableFields.get(0);
        assertThat(filterableNameField).isNotNull();
        assertThat(filterableNameField.getName()).isEqualTo(nameFieldName);
        assertThat(filterableNameField.getType()).isEqualTo(FieldType.String);
        assertThat(filterableNameField.getIsJsonBType()).isFalse();
        assertThat(filterableNameField.getIsStandard()).isTrue();

        val filterableColumnField = filterableFields.get(1);
        assertThat(filterableColumnField).isNotNull();
        assertThat(filterableColumnField.getName()).isEqualTo(columnFieldName);
        assertThat(filterableColumnField.getType()).isEqualTo(FieldType.String);
        assertThat(filterableColumnField.getIsJsonBType()).isTrue();
        assertThat(filterableColumnField.getIsStandard()).isTrue();
    }

}