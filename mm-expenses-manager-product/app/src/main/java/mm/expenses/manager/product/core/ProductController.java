package mm.expenses.manager.product.core;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.postgresql.pagination.PaginationHelper;
import mm.expenses.manager.common.utils.config.PaginationConfig;
import mm.expenses.manager.common.web.api.WebApi;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.product.api.product.ProductApi;
import mm.expenses.manager.product.api.product.model.*;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.search.ProductFilter;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(ProductWebApi.BASE_URL)
class ProductController implements ProductApi {

    private final PaginationHelper pagination;
    private final ProductService service;
    private final PriceConverter priceConverter;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductPage> findAll(@RequestParam(value = PaginationConfig.PAGE_NUMBER, required = false) final Integer pageNumber,
                                               @RequestParam(value = PaginationConfig.PAGE_SIZE, required = false) final Integer pageSize,
                                               @RequestParam(value = PaginationConfig.SORT, required = false) final SortProductRequest sortOrder,

                                               @RequestParam(value = ProductFilter.IS_DELETED_PROPERTY, required = false) final Boolean isDeleted,
                                               @RequestParam(value = ProductFilter.SHOULD_CONVERT_CURRENCY_PROPERTY, required = false) final Boolean shouldConvertCurrency,

                                               @RequestParam(value = ProductFilter.NAME_PROPERTY, required = false) final String name,
                                               @RequestParam(value = ProductFilter.NAME_OPERATION_PROPERTY, required = false) final TextOperationRequest nameOperation,

                                               @RequestParam(value = ProductFilter.PRICE_VALUE_PROPERTY, required = false) final BigDecimal priceValue,
                                               @RequestParam(value = ProductFilter.PRICE_VALUE_OPERATION_PROPERTY, required = false) final NumberOperationRequest priceValueOperation,

                                               @RequestParam(value = ProductFilter.PRICE_CURRENCY_PROPERTY, required = false) final String priceCurrency,
                                               @RequestParam(value = ProductFilter.PRICE_CURRENCY_OPERATION_PROPERTY, required = false) final CurrencyOperationRequest priceCurrencyOperation,

                                               @RequestParam(value = ProductFilter.GENERAL_QUERY_PROPERTY, required = false) final String query) {
        val queryFilter = ProductFilter.builder()
                .sortConfig(ProductSortOrder.of(sortOrder))
                .name(name)
                .nameOperation(nameOperation)
                .priceValue(priceValue)
                .priceValueOperation(priceValueOperation)
                .priceCurrency(priceCurrency)
                .priceCurrencyOperation(priceCurrencyOperation)
                .query(query)
                .isDeleted(isDeleted)
                .shouldConvertCurrency(shouldConvertCurrency)
                .paginationConfig(pagination.getPageRequest(pageNumber, pageSize))
                .build();

        return ResponseEntity.ok(service.search(queryFilter, priceConverter.getDefaultCurrency()));
    }

    @Override
    @GetMapping(value = WebApi.ID_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> findById(@PathVariable("id") final UUID id, final Boolean isDeleted) {
        return ResponseEntity.ok(service.findById(id, isDeleted, priceConverter.getDefaultCurrency()));
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> create(@RequestBody final CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request, priceConverter.getDefaultCurrency()));
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = WebApi.ID_URL, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> update(@PathVariable("id") final UUID id, @RequestBody final UpdateProductRequest request) {
        if (!isAnyUpdateProduct(request)) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_NO_UPDATE_DATA);
        }
        return ResponseEntity.ok(service.update(id, request, priceConverter.getDefaultCurrency()));
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = WebApi.ID_URL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("id") final UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteByIds(@RequestBody final ProductIds request) {
        service.delete(new HashSet<>(request.getIds()));

        return ResponseEntity.noContent().build();
    }

    private boolean isAnyUpdateProduct(final UpdateProductRequest request) {
        final var isNameUpdated = Objects.nonNull(request.getName());
        final var isPriceUpdated = Objects.nonNull(request.getPrice()) && isAnyUpdatePrice(request.getPrice());
        final var isDetailsUpdated = MapUtils.isNotEmpty(request.getDetails());

        return isNameUpdated || isPriceUpdated || isDetailsUpdated;
    }

    private boolean isAnyUpdatePrice(final UpdatePriceRequest request) {
        final var isCurrencyUpdated = Objects.nonNull(request.getCurrency());
        final var isValueUpdated = Objects.nonNull(request.getValue());

        return isCurrencyUpdated || isValueUpdated;
    }

}
