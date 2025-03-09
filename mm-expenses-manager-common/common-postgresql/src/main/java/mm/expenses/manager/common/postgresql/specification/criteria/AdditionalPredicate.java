package mm.expenses.manager.common.postgresql.specification.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;

/**
 * Potential additional predicate definition to be handled in specific {@link mm.expenses.manager.common.postgresql.specification.SpecificationHandler}
 * based on specific entity type.
 *
 * @param <T> specific entity type
 */
public interface AdditionalPredicate<T> {

    /**
     * @return additional {@link Predicate} based on passed criteria parameters if needed.
     */
    Predicate handle(final List<CriteriaParameter> criteriaParameters, final Root<?> root, final CriteriaQuery<?> query, final CriteriaBuilder builder);

}
