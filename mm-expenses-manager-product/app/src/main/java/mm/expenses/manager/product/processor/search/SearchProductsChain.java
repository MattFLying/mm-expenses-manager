package mm.expenses.manager.product.processor.search;

import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.processor.ProductFilterView;
import org.springframework.data.domain.Page;

/**
 * Default abstraction to build chains of responsibility for {@link ProductFilterView} search process.
 */
abstract sealed class SearchProductsChain extends ChainCommandExecution<EntityFilter, Page<ProductFilterView>> permits PrepareSpecificationCriterias, SearchProductsByCriteria, ConvertPricesForSearchProducts, PrepareSearchProductsResult {

}
