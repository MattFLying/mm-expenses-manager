package mm.expenses.manager.common.postgresql.specification.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import mm.expenses.manager.common.postgresql.specification.Operation;

/**
 * Represents specific converter for specific {@link Operation}.
 */
@FunctionalInterface
public interface CriteriaParameterConverter {

    /**
     * Defines restrictions for specific {@link CriteriaParameter} to be used during handling JPA {@link org.springframework.data.jpa.domain.Specification}s.
     */
    Predicate restrict(final CriteriaParameter criteriaParameter, final Root<?> root, final CriteriaBuilder builder);

}
