package mm.expenses.manager.product.core;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.processor.ProcessorType;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.api.product.model.ProductPage;
import mm.expenses.manager.product.api.product.model.ProductResponse;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.processor.BaseProductHandler;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ProductService {

    private final Map<ProcessorType, BaseProductHandler> handlers;

    public ProductService(final List<BaseProductHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                        BaseProductHandler::getType,
                        Function.identity()
                ));
    }

    @Transactional
    public Product create(final CreateProductRequest request) {
        final var requestedData = requestOf(request);
        final var handler = getHandler(BaseProductHandler.Type.CREATE_PRODUCT);
        final var result = handler.handle(requestedData);

        return result.mapResponse(Product.class);
    }

    @Transactional
    public ProductResponse create(final CreateProductRequest request, final CurrencyCode defaultCurrency) {
        final var requestedData = requestOf(request, defaultCurrency);
        final var handler = getHandler(BaseProductHandler.Type.CREATE_PRODUCT);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(ProductResponse.class);
    }

    @Transactional
    public Product update(final UUID productId, final UpdateProductRequest request) {
        final var requestedData = requestOf(productId, request);
        final var handler = getHandler(BaseProductHandler.Type.UPDATE_PRODUCT);
        final var result = handler.handle(requestedData);

        return result.mapResponse(Product.class);
    }

    @Transactional
    public ProductResponse update(final UUID productId, final UpdateProductRequest request, final CurrencyCode defaultCurrency) {
        final var requestedData = requestOf(productId, request, defaultCurrency);
        final var handler = getHandler(BaseProductHandler.Type.UPDATE_PRODUCT);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(ProductResponse.class);
    }

    @Transactional
    public void updateMissingCurrencies() {
        final var handler = getHandler(BaseProductHandler.Type.UPDATE_PRODUCTS_PRICE_CURRENCIES);
        handler.handle(null);
    }

    @Transactional
    public void hardDelete() {
        final var handler = getHandler(BaseProductHandler.Type.HARD_DELETE_PRODUCTS);
        handler.handle(null);
    }

    public Product delete(final UUID productId) {
        final var requestedData = requestOf(productId);
        final var handler = getHandler(BaseProductHandler.Type.DELETE_SINGLE_PRODUCT);
        final var result = handler.handle(requestedData);

        return result.mapResponse(Product.class);
    }

    public void delete(final Set<UUID> productIds) {
        final var requestedData = requestOf(productIds);
        final var handler = getHandler(BaseProductHandler.Type.DELETE_MANY_PRODUCTS);
        handler.handle(requestedData);
    }

    public Product findById(final UUID productId, final Boolean isDeleted) {
        final var requestedData = requestOf(productId, isDeleted);
        final var handler = getHandler(BaseProductHandler.Type.FIND_PRODUCT);
        final var result = handler.handle(requestedData);

        return result.mapResponse(Product.class);
    }

    public ProductResponse findById(final UUID productId, final Boolean isDeleted, final CurrencyCode defaultCurrency) {
        final var requestedData = requestOf(productId, isDeleted, defaultCurrency);
        final var handler = getHandler(BaseProductHandler.Type.FIND_PRODUCT);
        final var result = handler.handleDecorated(requestedData);

        return result.mapDecoratedResponse(ProductResponse.class);
    }

    public Page<ProductResponse> search(final EntityFilter queryFilter) {
        final var requestedData = requestOf(queryFilter);
        final var handler = getHandler(BaseProductHandler.Type.SEARCH_PRODUCTS);
        final var result = handler.handle(requestedData);

        return ((BaseProductHandler.Response) result).getPagedResponse();
    }

    public ProductPage search(final EntityFilter queryFilter, final CurrencyCode defaultCurrency) {
        final var requestedData = requestOf(queryFilter, defaultCurrency);
        final var handler = getHandler(BaseProductHandler.Type.SEARCH_PRODUCTS);
        final var result = handler.handleDecorated(requestedData);

        return ((BaseProductHandler.Response) result).getDecoratedPagedResponse();
    }

    private BaseProductHandler getHandler(final BaseProductHandler.Type type) {
        try {
            if (Objects.isNull(type)) {
                throw new IllegalArgumentException("Cannot recognize type of product handler");
            }
            return handlers.get(type);
        } catch (final NullPointerException exception) {
            throw new IllegalArgumentException(String.format("Cannot recognize product handler of %s type", type));
        }
    }

    private BaseProductHandler.Request requestOf(final Object request) {
        return BaseProductHandler.Request.builder().request(request).build();
    }

    private BaseProductHandler.Request requestOf(final Object request, final CurrencyCode defaultCurrency) {
        return BaseProductHandler.Request.builder().request(request).expectedCurrency(defaultCurrency).build();
    }

    private BaseProductHandler.Request requestOf(final UUID productId, final Object request) {
        return BaseProductHandler.Request.builder().id(productId).request(request).build();
    }

    private BaseProductHandler.Request requestOf(final UUID productId, final Object request, final CurrencyCode defaultCurrency) {
        return BaseProductHandler.Request.builder().id(productId).request(request).expectedCurrency(defaultCurrency).build();
    }

    private BaseProductHandler.Request requestOf(final UUID productId, final Boolean isDeleted, final CurrencyCode defaultCurrency) {
        return BaseProductHandler.Request.builder().id(productId).isDeleted(isDeleted).expectedCurrency(defaultCurrency).build();
    }

}
