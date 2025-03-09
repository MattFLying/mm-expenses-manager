package mm.expenses.manager.product.product;

import jakarta.persistence.criteria.*;
import lombok.val;
import mm.expenses.manager.common.postgresql.exception.SpecificationCriteriaException;
import mm.expenses.manager.common.postgresql.specification.SpecificationHandler;
import mm.expenses.manager.common.postgresql.specification.SpecificationScanner;
import mm.expenses.manager.common.postgresql.specification.criteria.AdditionalPredicate;
import mm.expenses.manager.common.postgresql.specification.criteria.CriteriaParameter;
import mm.expenses.manager.common.postgresql.specification.criteria.SpecificationCriteria;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class ProductSpecificationHandler extends SpecificationHandler<Product> {

    public static final String PRICE_FIELD_NAME = "price";
    public static final String PRICE_VALUE_FIELD_NAME = "value";
    public static final String PRICE_CURRENCY_FIELD_NAME = "currency";

    private final SpecificationCriteria criteria;

    ProductSpecificationHandler(@Autowired final PaginationConfig paginationConfig) {
        criteria = SpecificationScanner.scan(Product.class);
        criteria.setPagination(paginationConfig);
    }

    @Override
    public SpecificationCriteria getSpecificationCriteria() {
        return criteria;
    }

    /**
     * @return additional {@link Predicate} is needed for products {@link org.springframework.data.jpa.domain.Specification} definition
     * in case if there is some filtering based on JsonB fields.
     */
    @Override
    protected AdditionalPredicate<Product> additionalPredicateDefinition() {
        return (criteriaParameters, root, query, builder) -> {
            val parametersIterator = criteriaParameters.iterator();
            val predicates = new ArrayList<Predicate>();
            while (parametersIterator.hasNext()) {
                val criteriaParameter = parametersIterator.next();
                val splitParameter = criteriaParameter.name().split("\\.");

                if (splitParameter.length > 2) {
                    throw new SpecificationCriteriaException(ProductExceptionMessage.PRODUCT_FILTERING_PATH_SIZE_EXCEEDED.getMessage());
                } else if (splitParameter.length == 2) {
                    val rootParameterName = splitParameter[0];
                    val expectedParameterName = splitParameter[1];
                    if (PRICE_FIELD_NAME.equals(rootParameterName)) {
                        parametersIterator.remove();
                        definePredicateForJsonBPrice(root, builder, expectedParameterName, predicates, criteriaParameter, rootParameterName);
                    }
                }
            }
            if (!predicates.isEmpty()) {
                return builder.and(predicates.toArray(Predicate[]::new));
            }
            return null;
        };
    }

    private void definePredicateForJsonBPrice(final Root<?> root, final CriteriaBuilder builder, final String expectedParameterName, final List<Predicate> predicates, final CriteriaParameter criteriaParameter, final String rootParameterName) {
        if (StringUtils.equalsAnyIgnoreCase(expectedParameterName, PRICE_VALUE_FIELD_NAME, PRICE_CURRENCY_FIELD_NAME)) {
            handleJsonBParameter(predicates, builder, root, criteriaParameter, rootParameterName, expectedParameterName);
        } else {
            throw new SpecificationCriteriaException(SpecificationCriteriaException.FIELD_NOT_AVAILABLE_IN_OBJECT_FIELD_MESSAGE, rootParameterName, expectedParameterName);
        }
    }

}
