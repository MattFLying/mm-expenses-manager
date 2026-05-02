package mm.expenses.manager.product.processor;

import lombok.*;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.common.utils.processor.ProcessorType;
import mm.expenses.manager.product.core.Product;
import mm.expenses.manager.product.core.ProductFilterView;
import mm.expenses.manager.product.core.ProductMapper;
import mm.expenses.manager.product.api.product.model.ProductPage;
import mm.expenses.manager.product.api.product.model.ProductResponse;
import mm.expenses.manager.product.currency.PriceConverter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;

/**
 * Default handler definition to process specific handlers.
 */
@RequiredArgsConstructor
public abstract class BaseProductHandler extends ProcessorHandler {

    protected final ProductMapper mapper;
    protected final PriceConverter priceConverter;

    @Getter
    @SuperBuilder
    public static class Request extends ProcessorHandler.Request {

        private Boolean isDeleted;

        private Boolean shouldConvertCurrency;

        private CurrencyCode expectedCurrency;

        public boolean isDeleted() {
            return Objects.nonNull(isDeleted) && isDeleted;
        }

    }

    @Getter
    @SuperBuilder
    public static class Response extends ProcessorHandler.Response {

        private Page<Product> pagedProductResponse;

        private Page<ProductResponse> pagedResponse;

        private Page<ProductFilterView> pagedFilteredResponse;

        private List<Product> listedResponse;

        private ProductPage decoratedPagedResponse;

    }

    /**
     * Possible handler types to be implemented and to be used.
     */
    public enum Type implements ProcessorType {
        CREATE_PRODUCT,
        UPDATE_PRODUCT,
        DELETE_SINGLE_PRODUCT,
        DELETE_MANY_PRODUCTS,
        FIND_PRODUCT,
        SEARCH_PRODUCTS,

        HARD_DELETE_PRODUCTS,
        UPDATE_PRODUCTS_PRICE_CURRENCIES
    }

}
