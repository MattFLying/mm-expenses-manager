package mm.expenses.manager.product.processor.search;

import jakarta.persistence.criteria.Predicate;
import lombok.val;
import mm.expenses.manager.common.postgresql.specification.SpecificationHandler;
import mm.expenses.manager.common.postgresql.specification.SpecificationScanner;
import mm.expenses.manager.common.postgresql.specification.criteria.AdditionalPredicate;
import mm.expenses.manager.common.postgresql.specification.criteria.CriteriaParameter;
import mm.expenses.manager.common.postgresql.specification.criteria.SpecificationCriteria;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.product.processor.ProductFilterView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProductFilterViewSpecificationHandler extends SpecificationHandler<ProductFilterView> {

    private final SpecificationCriteria criteria;

    public ProductFilterViewSpecificationHandler(@Autowired final PaginationConfig paginationConfig) {
        criteria = SpecificationScanner.scan(ProductFilterView.class);
        criteria.setPagination(paginationConfig);
    }

    @Override
    public SpecificationCriteria getSpecificationCriteria() {
        return criteria;
    }

    @Override
    protected AdditionalPredicate<ProductFilterView> additionalPredicateDefinition() {
        return (criteriaParameters, root, query, builder) -> {
            val predicates = new ArrayList<Predicate>();

            // check if there are additional parameters related with price original and price currency to make correct predicate with or method
            val priceCriteriaByFieldName = criteriaParameters.stream()
                    .filter(criteriaParameter -> ProductFilterView.IS_PRICE_ORIGINAL_FIELD_NAME.equals(criteriaParameter.name()) || ProductFilterView.PRICE_CURRENCY_FIELD_NAME.equals(criteriaParameter.name()))
                    .collect(Collectors.toMap(
                            CriteriaParameter::name,
                            Function.identity()
                    ));

            if (priceCriteriaByFieldName.size() == 2) {
                val priceCurrency = priceCriteriaByFieldName.get(ProductFilterView.PRICE_CURRENCY_FIELD_NAME);
                val isPriceOriginal = priceCriteriaByFieldName.get(ProductFilterView.IS_PRICE_ORIGINAL_FIELD_NAME);

                val priceCurrencyPredicate = convertCriteriaParameterToPredicate(priceCurrency, root, query, builder);
                val isPriceOriginalPredicate = convertCriteriaParameterToPredicate(isPriceOriginal, root, query, builder);
                predicates.add(priceCurrencyPredicate);
                predicates.add(isPriceOriginalPredicate);

                return builder.or(predicates.toArray(Predicate[]::new));
            }
            return null;
        };
    }

}
