package mm.expenses.manager.order.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.exception.api.ApiBadRequestException;
import mm.expenses.manager.common.beans.exception.api.ApiNotFoundException;
import mm.expenses.manager.order.api.product.ProductApi;
import mm.expenses.manager.order.api.product.model.*;
import mm.expenses.manager.order.pageable.PageFactory;
import mm.expenses.manager.order.product.exception.ProductCreationException;
import mm.expenses.manager.order.product.exception.ProductNotFoundException;
import mm.expenses.manager.order.product.exception.ProductUpdateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashSet;

import static mm.expenses.manager.order.config.UrlDefaultPaths.PRODUCT_URL;

@RestController
@RequestMapping(PRODUCT_URL)
@RequiredArgsConstructor
class ProductController implements ProductApi {

    private final ProductService service;
    private final ProductMapper mapper;

    @Override
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductPage findAll(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
                               @RequestParam(value = "pageSize", required = false) Integer pageSize,
                               @RequestParam(value = "name", required = false) String name,
                               @RequestParam(value = "price", required = false) BigDecimal price,
                               @RequestParam(value = "lessThan", required = false) Boolean lessThan,
                               @RequestParam(value = "greaterThan", required = false) Boolean greaterThan,
                               @RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
                               @RequestParam(value = "priceMax", required = false) BigDecimal priceMax) {
        final var pageable = PageFactory.getPageRequest(pageNumber, pageSize);
        final var filters = new ProductRequestFilter(name, price, priceMin, priceMax, lessThan, greaterThan);
        final var result = switch (filters.filter()) {
            case BY_NAME -> service.findByName(name, pageable);
            case BY_PRICE_RANGE -> service.findByPriceRange(priceMin, priceMax, pageable);
            case BY_PRICE_LESS_THAN -> service.findByPriceLess(price, pageable);
            case BY_PRICE_GREATER_THAN -> service.findByPriceGreater(price, pageable);
            case ALL -> service.findAll(pageable);
            default -> throw new ApiBadRequestException(null);
        };
        return mapper.mapToPageResponse(result);
    }

    @Override
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse findById(@PathVariable("id") String id) {
        return service.findById(id)
                .map(mapper::mapToResponse)
                .orElseThrow();
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse create(@RequestBody CreateNewProductRequest request) {
        try {
            return service.create(mapper.mapToNewRequest(request))
                    .map(mapper::mapToResponse)
                    .orElseThrow();
        } catch (final ProductCreationException exception) {
            throw new ApiBadRequestException(null);
        }
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ProductResponse update(@PathVariable("id") String id, @RequestBody UpdateProductRequest request) {
        try {
            return service.update(id, mapper.mapToUpdatedRequest(request))
                    .map(mapper::mapToResponse)
                    .orElseThrow();
        } catch (final ProductNotFoundException exception) {
            throw new ApiNotFoundException(null);
        } catch (final ProductUpdateException exception) {
            throw new ApiBadRequestException(null);
        }
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteById(@PathVariable("id") String id) {
        try {
            service.remove(id);
        } catch (final ProductNotFoundException exception) {
            throw new ApiNotFoundException(null);
        }
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void deleteByIds(@RequestBody ProductIds request) {
        try {
            service.removeByIds(new HashSet<>(request.getIds()));
        } catch (final ProductNotFoundException exception) {
            throw new ApiNotFoundException(null);
        }
    }

    // To be tracked later
    /*@GetMapping
    Page<ProductDto> findAllFiltered(@RequestParam(value = "name", required = false) String name,
                                     @RequestParam(value = "price", required = false) BigDecimal price,
                                     @RequestParam(value = "lessThan", required = false) boolean lessThan,
                                     @RequestParam(value = "greaterThan", required = false) boolean greaterThan,
                                     @RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
                                     @RequestParam(value = "priceMax", required = false) BigDecimal priceMax,
                                     final Pageable pageable) {
        final var filters = new ProductRequestFilter(name, price, priceMin, priceMax, lessThan, greaterThan);
        Page<Product> result;
        switch (filters.filter()) {
            case BY_NAME:
                result = service.findByName(name, pageable);
                break;
            case BY_PRICE_RANGE:
                result = service.findByPriceRange(priceMin, priceMax, pageable);
                break;
            case BY_PRICE_LESS_THAN:
                result = service.findByPriceLess(price, pageable);
                break;
            case BY_PRICE_GREATER_THAN:
                result = service.findByPriceGreater(price, pageable);
                break;
            case ALL:
                result = service.findAll(pageable);
                break;
            default:
                //throw new ApiBadRequestException(ProductExceptionCode.PRODUCT_FILTERS_INCORRECT.getCode(), "Incorrect filters");
                throw new ApiBadRequestException(null);
        }
        return new PageImpl<>(result.stream().map(mapper::mapToDto).collect(Collectors.toList()), pageable, result.getTotalElements());
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<ProductDto> findById(@PathVariable("id") final String id) {
        return service.findById(id)
                .map(mapper::mapToDto)
                .map(product -> ResponseEntity.ok().body(product))
                .orElseThrow();
                //.orElseThrow(() -> new ApiNotFoundException(ProductExceptionCode.NOT_FOUND.getCode(), "Product of id: " + id + " does not exists."));
    }

    @PostMapping
    ResponseEntity<ProductDto> create(@RequestBody final CreateNewProduct newProduct) {
        try {
            return service.create(newProduct)
                    .map(mapper::mapToDto)
                    .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                    .orElseThrow();
                    //.orElseThrow(() -> new ApiNotFoundException(ProductExceptionCode.NOT_FOUND.getCode(), "Product could not be created."));
        } catch (final ProductCreationException exception) {
            //throw new ApiBadRequestException(ProductExceptionCode.NEW_PRODUCT_VALIDATION.getCode(), exception.getMessage());
            throw new ApiBadRequestException(null);
        }
    }

    @PutMapping(value = "/{id}")
    ResponseEntity<ProductDto> update(@PathVariable("id") final String id, @RequestBody final UpdateProduct product) {
        try {
            return service.update(id, product)
                    .map(mapper::mapToDto)
                    .map(updated -> ResponseEntity.status(HttpStatus.CREATED).body(updated))
                    .orElseThrow();
                    //.orElseThrow(() -> new ApiNotFoundException(ProductExceptionCode.NOT_FOUND.getCode(), "Product could not be updated."));
        } catch (final ProductNotFoundException exception) {
            throw notFoundException(exception);
        } catch (final ProductUpdateException exception) {
            //throw new ApiBadRequestException(ProductExceptionCode.UPDATE_PRODUCT_VALIDATION.getCode(), exception.getMessage());
            throw new ApiBadRequestException(null);
        }
    }

    @DeleteMapping(value = "/{id}")
    ResponseEntity<Void> delete(@PathVariable("id") final String id) {
        try {
            service.remove(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (final ProductNotFoundException exception) {
            throw notFoundException(exception);
        }
    }

    @PostMapping(value = "/remove")
    ResponseEntity<Void> delete(@RequestBody final Set<String> ids) {
        try {
            service.removeByIds(ids);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (final ProductNotFoundException exception) {
            throw notFoundException(exception);
        }
    }*/

}
