package mm.expenses.manager.common.postgresql.specification;

import jakarta.persistence.Column;
import lombok.Data;
import mm.expenses.manager.common.utils.specification.SpecificationDetailsAnnotation;

import java.time.Instant;

@Data
class TestClassToHandlerSpecification {

    @SpecificationDetailsAnnotation(canBeFiltered = true)
    private String name;

    @SpecificationDetailsAnnotation(canBeSorted = true)
    private Integer value;

    @SpecificationDetailsAnnotation(canBeFiltered = true)
    @Column(columnDefinition = "jsonb")
    private String column;

    private Instant time;

}
