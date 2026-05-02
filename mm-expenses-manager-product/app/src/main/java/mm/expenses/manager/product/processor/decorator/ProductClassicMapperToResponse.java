package mm.expenses.manager.product.processor.decorator;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.product.api.product.model.ProductResponse;
import mm.expenses.manager.product.price.ProductPrice;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Classic mapper from {@link Product} to {@link ProductResponse}.
 */
@RequiredArgsConstructor
public class ProductClassicMapperToResponse extends ProductMappingStrategy {

    private final ProductMapper mapper;

    @Override
    public ProductResponse decorate(final Product product) {
        final var originalPrice = product.getPrices()
                .stream()
                .filter(ProductPrice::isOriginal)
                .findAny();

        return mapper.mapProductResponse(product, originalPrice.orElse(null));
    }

    @Override
    public Page<ProductResponse> decorate(final Page<Product> page) {
        final var content = page.getContent()
                .stream()
                .map(this::decorate)
                .toList();
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

}
