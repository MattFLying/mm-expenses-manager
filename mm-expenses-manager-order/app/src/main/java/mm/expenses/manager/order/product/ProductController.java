package mm.expenses.manager.order.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.PaginationConfig;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.common.utils.util.BooleanUtils;
import mm.expenses.manager.common.web.RequestProcessor;
import mm.expenses.manager.common.web.WebContext;
import mm.expenses.manager.common.web.WebInterceptor;
import mm.expenses.manager.common.web.exception.ApiBadRequestException;
import mm.expenses.manager.common.web.exception.ApiNotFoundException;
import mm.expenses.manager.order.api.product.ProductApi;
import mm.expenses.manager.order.api.product.model.*;
import mm.expenses.manager.order.product.exception.ProductCreationException;
import mm.expenses.manager.order.product.exception.ProductExceptionMessage;
import mm.expenses.manager.order.product.exception.ProductUpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashSet;

@RestController
@RequestMapping(ProductWebApi.BASE_URL)
@RequiredArgsConstructor
class ProductController implements ProductApi {

    private final PaginationHelper pagination;
    private final WebInterceptor interceptor;

    private final ProductMapper mapper;
    private final ProductService productService;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductPage> findAll(@RequestParam(value = PaginationConfig.PAGE_NUMBER_PROPERTY, required = false) final Integer pageNumber,
                                               @RequestParam(value = PaginationConfig.PAGE_SIZE_PROPERTY, required = false) final Integer pageSize,
                                               @RequestParam(value = ProductRequestFilter.NAME_PROPERTY, required = false) final String name,
                                               @RequestParam(value = ProductRequestFilter.PRICE_PROPERTY, required = false) final BigDecimal price,
                                               @RequestParam(value = ProductRequestFilter.PRICE_LESS_THAN_PROPERTY, required = false) final Boolean lessThan,
                                               @RequestParam(value = ProductRequestFilter.PRICE_GREATER_THAN_PROPERTY, required = false) final Boolean greaterThan,
                                               @RequestParam(value = ProductRequestFilter.PRICE_MIN_PROPERTY, required = false) final BigDecimal priceMin,
                                               @RequestParam(value = ProductRequestFilter.PRICE_MAX_PROPERTY, required = false) final BigDecimal priceMax) {
        final var pageable = pagination.getPageRequest(pageNumber, pageSize);

        final var context = WebContext.of(ProductWebApi.FIND_ALL);
        final RequestProcessor processor = webContext -> {
            final var filters = new ProductRequestFilter(name, price, priceMin, priceMax, BooleanUtils.booleanPrimitiveOrDefault(lessThan), BooleanUtils.booleanPrimitiveOrDefault(greaterThan));

            return mapper.mapToPageResponse(
                    switch (filters.filter()) {
                        case BY_NAME -> productService.findByName(filters.name(), pageable);
                        case BY_PRICE_RANGE -> productService.findByPriceRange(filters.priceMin(), filters.priceMax(), pageable);
                        case BY_PRICE_LESS_THAN -> productService.findByPriceLess(filters.price(), pageable);
                        case BY_PRICE_GREATER_THAN -> productService.findByPriceGreater(filters.price(), pageable);
                        case ALL -> productService.findAll(pageable);
                        default -> throw new ApiBadRequestException(ProductExceptionMessage.PRODUCT_FILTERS_INCORRECT);
                    }
            );
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> findById(@PathVariable("id") final String id) {
        final var context = WebContext.of(ProductWebApi.FIND_BY_ID).requestId(id);
        final RequestProcessor processor = webContext -> {
            final var productId = webContext.getRequestId();

            return productService.findById(productId)
                    .map(mapper::mapToResponse)
                    .orElseThrow(() -> new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId)));
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> create(@RequestBody final CreateNewProductRequest request) {
        final var context = WebContext.of(ProductWebApi.CREATE).requestBody(request);
        final RequestProcessor processor = webContext -> {
            try {
                final var newProduct = (CreateNewProductRequest) webContext.getRequestBody();
                return productService.create(newProduct).map(mapper::mapToResponse);
            } catch (final ProductCreationException exception) {
                throw new ApiBadRequestException(ProductExceptionMessage.NEW_PRODUCT_VALIDATION, exception);
            }
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> update(@PathVariable("id") final String id, @RequestBody final UpdateProductRequest request) {
        final var context = WebContext.of(ProductWebApi.UPDATE).requestId(id).requestBody(request);
        final RequestProcessor processor = webContext -> {
            try {
                final var updateProductRequest = (UpdateProductRequest) webContext.getRequestBody();
                return productService.update(webContext.getRequestId(), updateProductRequest)
                        .map(mapper::mapToResponse)
                        .orElseThrow(() -> new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(webContext.getRequestId())));
            } catch (final ProductUpdateException exception) {
                throw new ApiBadRequestException(ProductExceptionMessage.UPDATE_PRODUCT_VALIDATION, exception);
            }
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("id") final String id) {
        final var context = WebContext.of(ProductWebApi.DELETE).requestId(id);
        final RequestProcessor processor = webContext -> {
            productService.remove(webContext.getRequestId());
            return true;
        };
        return interceptor.processRequest(processor, context);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteByIds(@RequestBody final ProductIds request) {
        final var context = WebContext.of(ProductWebApi.DELETE_BY_IDS).requestIds(request.getIds());
        final RequestProcessor processor = webContext -> {
            productService.removeByIds(new HashSet<>(webContext.getRequestIds()));
            return true;
        };
        return interceptor.processRequest(processor, context);
    }

}
