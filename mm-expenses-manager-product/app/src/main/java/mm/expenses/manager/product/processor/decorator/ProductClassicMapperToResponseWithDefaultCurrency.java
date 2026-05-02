package mm.expenses.manager.product.processor.decorator;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.api.product.model.ProductResponse;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Objects;

/**
 * Mapper from {@link Product} to {@link ProductResponse} with converted prices to default price.
 */
@RequiredArgsConstructor
public class ProductClassicMapperToResponseWithDefaultCurrency extends ProductMappingStrategy {

    private final ProductMapper mapper;
    private final CurrencyCode expectedCurrency;

    @Override
    public ProductResponse decorate(final Product product) {
        validate();

        return mapper.mapProductResponse(product, expectedCurrency);
    }

    @Override
    public Page<ProductResponse> decorate(final Page<Product> page) {
        validate();

        final var content = page.getContent()
                .stream()
                .map(this::decorate)
                .toList();
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    @Override
    protected void validate() {
        if (Objects.isNull(mapper)) {
            throw new IllegalArgumentException("Product decorator is null");
        }
    }

}
