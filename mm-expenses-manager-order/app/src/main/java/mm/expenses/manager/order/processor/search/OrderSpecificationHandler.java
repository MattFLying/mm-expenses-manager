package mm.expenses.manager.order.processor.search;

import jakarta.persistence.criteria.*;
import mm.expenses.manager.common.postgresql.specification.FieldType;
import mm.expenses.manager.common.postgresql.specification.Operation;
import mm.expenses.manager.common.postgresql.specification.criteria.*;
import mm.expenses.manager.common.postgresql.specification.SpecificationScanner;
import mm.expenses.manager.common.postgresql.specification.SpecificationHandler;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.order.core.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderSpecificationHandler extends SpecificationHandler<Order> {

    private final SpecificationCriteria criteria;

    public OrderSpecificationHandler(@Autowired final PaginationConfig paginationConfig) {
        criteria = SpecificationScanner.scan(Order.class);
        criteria.setPagination(paginationConfig);

        criteria.getFilteredFields().add(FilteredField.of(OrderFilter.PRODUCTS_COUNT_PROPERTY, FieldType.Integer, false));
    }

    @Override
    public SpecificationCriteria getSpecificationCriteria() {
        return criteria;
    }

    @Override
    protected AdditionalPredicate<Order> additionalPredicateDefinition() {
        return (criteriaParameters, root, query, builder) -> {
            final var productsCountCriteriaParameter = criteriaParameters.stream()
                    .filter(criteriaParameter -> OrderFilter.PRODUCTS_COUNT_PROPERTY.equals(criteriaParameter.name()))
                    .findAny();

            if (productsCountCriteriaParameter.isPresent()) {
                final var parameter = productsCountCriteriaParameter.get();

                final var join = root.join(OrderFilter.PRODUCTS_PROPERTY, JoinType.LEFT);
                final var countJoin = builder.count(join);
                final var valueForCount = Integer.valueOf(parameter.values().get(0).toString());

                return query.groupBy(root.get("id"))
                        .having(createPredicate(builder, countJoin, valueForCount, parameter.operation()))
                        .getRestriction();
            }
            return null;
        };
    }

    private Predicate createPredicate(final CriteriaBuilder builder, final Expression<Long> countJoin, final Integer valueForCount, final Operation operation) {
        return switch (operation) {
            case greaterThan -> builder.gt(countJoin, valueForCount);
            case greaterThanOrEqual -> builder.ge(countJoin, valueForCount);
            case lessThan -> builder.lt(countJoin, valueForCount);
            case lessThanOrEqual -> builder.le(countJoin, valueForCount);
            case notEqual -> builder.notEqual(countJoin, valueForCount);
            default -> builder.equal(countJoin, valueForCount);
        };
    }

}
