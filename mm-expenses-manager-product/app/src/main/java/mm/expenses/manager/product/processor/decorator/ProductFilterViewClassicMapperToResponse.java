package mm.expenses.manager.product.processor.decorator;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.product.api.product.model.ProductResponse;
import mm.expenses.manager.product.core.ProductFilterView;
import mm.expenses.manager.product.core.ProductMapper;
import org.springframework.data.domain.Page;

/**
 * Classic mapper from {@link ProductFilterView} to {@link ProductResponse}.
 */
@RequiredArgsConstructor
public class ProductFilterViewClassicMapperToResponse extends ProductFilterViewMappingStrategy {

    private final ProductMapper mapper;

    @Override
    public ProductResponse decorate(final ProductFilterView product) {
        return mapper.map(product);
    }

    @Override
    public Page<ProductResponse> decorate(final Page<ProductFilterView> pagedProducts) {
        return mapper.mapPages(pagedProducts);
    }

}
