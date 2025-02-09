package mm.expenses.manager.order.order;

import mm.expenses.manager.common.postgresql.specification.criteria.SpecificationCriteria;
import mm.expenses.manager.common.postgresql.specification.SpecificationScanner;
import mm.expenses.manager.common.postgresql.specification.SpecificationHandler;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class OrderSpecificationHandler extends SpecificationHandler<Order> {

    private final SpecificationCriteria criteria;

    OrderSpecificationHandler(@Autowired final PaginationConfig paginationConfig) {
        criteria = SpecificationScanner.scan(Order.class);
        criteria.setPagination(paginationConfig);
    }

    @Override
    public SpecificationCriteria getSpecificationCriteria() {
        return criteria;
    }

}
