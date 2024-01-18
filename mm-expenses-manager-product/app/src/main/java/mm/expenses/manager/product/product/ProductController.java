package mm.expenses.manager.product.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.pagination.PaginationConfig;
import mm.expenses.manager.common.beans.pagination.PaginationHelper;
import mm.expenses.manager.common.web.exception.ApiBadRequestException;
import mm.expenses.manager.common.web.exception.ApiConflictException;
import mm.expenses.manager.product.api.product.ProductApi;
import mm.expenses.manager.product.api.product.model.*;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.Objects;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("products")
class ProductController implements ProductApi {

    private final PaginationHelper pagination;
    private final ProductMapper mapper;
    private final ProductService service;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductPage findAll(@RequestParam(value = PaginationConfig.PAGE_NUMBER_PROPERTY, required = false) final Integer pageNumber,
                               @RequestParam(value = PaginationConfig.PAGE_SIZE_PROPERTY, required = false) final Integer pageSize,
                               @RequestParam(value = PaginationConfig.SORT_ORDER_PROPERTY, required = false) final SortOrderRequest sortOrder,
                               @RequestParam(value = PaginationConfig.SORT_DESC_PROPERTY, required = false) final Boolean sortDesc,
                               @RequestParam(value = "name", required = false) final String name,
                               @RequestParam(value = "price", required = false) final BigDecimal price,
                               @RequestParam(value = "lessThan", required = false) final Boolean lessThan,
                               @RequestParam(value = "greaterThan", required = false) final Boolean greaterThan,
                               @RequestParam(value = "priceMin", required = false) final BigDecimal priceMin,
                               @RequestParam(value = "priceMax", required = false) final BigDecimal priceMax) {
        final var queryFilter = new ProductQueryFilter(name, price, priceMin, priceMax, lessThan, greaterThan);

        if ((Objects.nonNull(pageNumber) && Objects.isNull(pageSize)) || (Objects.isNull(pageNumber) && Objects.nonNull(pageSize))) {
            throw new ApiBadRequestException(ProductExceptionMessage.PAGE_SIZE_AND_PAGE_NUMBER_MUST_BE_FILLED);
        }

        if (queryFilter.isPriceAndPriceRangeOriented()) {
            throw new ApiBadRequestException(ProductExceptionMessage.PRICE_AND_PRICE_RANGE_NOT_ALLOWED);
        }

        if (queryFilter.isPriceOriented()) {
            if (queryFilter.isPriceLessAndGreaterUsed()) {
                throw new ApiBadRequestException(ProductExceptionMessage.PRICE_CAN_BE_LESS_THAN_OR_GREATER_THAN_AT_ONCE);
            }
        } else if (queryFilter.isAnyOfPriceRangeUsed()) {
            if (!queryFilter.isPriceRangeOriented()) {
                throw new ApiBadRequestException(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.withParameters(queryFilter.priceMin(), queryFilter.priceMax()));
            }
            if (queryFilter.isPriceLessOrGreaterUsed()) {
                throw new ApiBadRequestException(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE);
            }
        }

        return mapper.map(
                service.findProducts(queryFilter, pagination.getPageRequest(pageNumber, pageSize, ProductSortOrder.of(sortOrder, sortDesc)))
        );
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse findById(@PathVariable("id") final UUID id, final Boolean isDeleted) {
        final var product = service.findById(id, isDeleted);
        return mapper.mapProductResponse(product);
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse create(@RequestBody final CreateProductRequest request) {
        return mapper.mapProductResponse(service.create(request));
    }

    @Override
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse update(@PathVariable("id") final UUID id, @RequestBody final UpdateProductRequest request) {
        if (!isAnyUpdateProduct(request)) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_NO_UPDATE_DATA);
        }
        return mapper.mapProductResponse(service.update(id, request));
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteById(@PathVariable("id") final UUID id) {
        service.delete(id);
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
