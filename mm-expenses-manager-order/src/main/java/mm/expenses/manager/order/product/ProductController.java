package mm.expenses.manager.order.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ApiBadRequestException;
import mm.expenses.manager.exception.ApiNotFoundException;
import mm.expenses.manager.order.product.exception.ProductCreationException;
import mm.expenses.manager.order.product.exception.ProductExceptionCode;
import mm.expenses.manager.order.product.exception.ProductNotFoundException;
import mm.expenses.manager.order.product.exception.ProductUpdateException;
import mm.expenses.manager.order.product.model.CreateNewProduct;
import mm.expenses.manager.order.product.model.Product;
import mm.expenses.manager.order.product.model.ProductDto;
import mm.expenses.manager.order.product.model.UpdateProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static mm.expenses.manager.order.UrlDefaultPaths.PRODUCT_URL;

@RestController
@RequestMapping(PRODUCT_URL)
@RequiredArgsConstructor
class ProductController {

    private final ProductService service;
    private final ProductMapper mapper;

    @GetMapping
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
                throw new ApiBadRequestException(ProductExceptionCode.PRODUCT_FILTERS_INCORRECT.getCode(), "Incorrect filters");
        }
        return new PageImpl<>(result.stream().map(mapper::mapToDto).collect(Collectors.toList()), pageable, result.getTotalElements());
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<ProductDto> findById(@PathVariable("id") final String id) {
        return service.findById(id)
                .map(mapper::mapToDto)
                .map(product -> ResponseEntity.ok().body(product))
                .orElseThrow(() -> new ApiNotFoundException(ProductExceptionCode.NOT_FOUND.getCode(), "Product of id: " + id + " does not exists."));
    }

    @PostMapping
    ResponseEntity<ProductDto> create(@RequestBody final CreateNewProduct newProduct) {
        try {
            return service.create(newProduct)
                    .map(mapper::mapToDto)
                    .map(created -> ResponseEntity.status(HttpStatus.CREATED).body(created))
                    .orElseThrow(() -> new ApiNotFoundException(ProductExceptionCode.NOT_FOUND.getCode(), "Product could not be created."));
        } catch (final ProductCreationException exception) {
            throw new ApiBadRequestException(ProductExceptionCode.NEW_PRODUCT_VALIDATION.getCode(), exception.getMessage());
        }
    }

    @PutMapping(value = "/{id}")
    ResponseEntity<ProductDto> update(@PathVariable("id") final String id, @RequestBody final UpdateProduct product) {
        try {
            return service.update(id, product)
                    .map(mapper::mapToDto)
                    .map(updated -> ResponseEntity.status(HttpStatus.CREATED).body(updated))
                    .orElseThrow(() -> new ApiNotFoundException(ProductExceptionCode.NOT_FOUND.getCode(), "Product could not be updated."));
        } catch (final ProductNotFoundException exception) {
            throw notFoundException(exception);
        } catch (final ProductUpdateException exception) {
            throw new ApiBadRequestException(ProductExceptionCode.UPDATE_PRODUCT_VALIDATION.getCode(), exception.getMessage());
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
    }

    private ApiNotFoundException notFoundException(final ProductNotFoundException exception) {
        return new ApiNotFoundException(ProductExceptionCode.NOT_FOUND.getCode(), exception.getMessage());
    }

}
