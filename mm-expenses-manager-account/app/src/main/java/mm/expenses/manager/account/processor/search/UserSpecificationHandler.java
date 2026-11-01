package mm.expenses.manager.account.processor.search;

import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.common.postgresql.specification.SpecificationHandler;
import mm.expenses.manager.common.postgresql.specification.SpecificationScanner;
import mm.expenses.manager.common.postgresql.specification.criteria.SpecificationCriteria;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserSpecificationHandler extends SpecificationHandler<User> {

    private final SpecificationCriteria criteria;

    public UserSpecificationHandler(@Autowired final PaginationConfig paginationConfig) {
        criteria = SpecificationScanner.scan(User.class);
        criteria.setPagination(paginationConfig);
    }

    @Override
    public SpecificationCriteria getSpecificationCriteria() {
        return criteria;
    }

}
