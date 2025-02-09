package mm.expenses.manager.common.postgresql.specification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Result of created specification based on specific handler.
 */
public record HandledSpecificationResult<T>(Specification<T> specification, Pageable pageable) {

}
