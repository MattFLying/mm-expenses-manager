package mm.expenses.manager.product.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.exception.api.ApiConflictException;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.product.dto.request.CreateProductRequest;
import mm.expenses.manager.product.product.dto.request.UpdateProductRequest;
import mm.expenses.manager.product.product.dto.response.ProductPage;
import mm.expenses.manager.product.product.dto.response.ProductResponse;
import mm.expenses.manager.product.product.query.ProductQueryFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
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

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Objects;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("products")
@Tag(name = "Products", description = "Provide API for products management.")
class ProductController {

    private final ProductMapper mapper;

    @Operation(
            summary = "Finds all available products by specific filters.",
            description = "Check for available products relates with specified filters or check all.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProductPage.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ProductPage findAll(@Parameter(description = "Specific name of the product.") @RequestParam(value = "name", required = false) String name,
                        @Parameter(description = "Specific price of the product.") @RequestParam(value = "price", required = false) @Min(0) BigDecimal price,
                        @Parameter(description = "If price is passed then could be used to specify if price should be less than.") @RequestParam(value = "lessThan", required = false) Boolean lessThan,
                        @Parameter(description = "If price is passed then could be used to specify if price should be greater than.") @RequestParam(value = "greaterThan", required = false) Boolean greaterThan,
                        @Parameter(description = "Specific price for minimal price range.") @RequestParam(value = "priceMin", required = false) @Min(0) BigDecimal priceMin,
                        @Parameter(description = "Specific price for maximum price range.") @RequestParam(value = "priceMax", required = false) @Min(0) BigDecimal priceMax,
                        @Parameter(description = "Page number.") @RequestParam(value = "pageNumber", required = false) @Min(0) final Integer pageNumber,
                        @Parameter(description = "Page size.") @RequestParam(value = "pageSize", required = false) @Min(1) final Integer pageSize,
                        @Parameter(description = "Sorting type.") @RequestParam(value = "sortOrder", required = false) final SortOrder sortOrder,
                        @Parameter(description = "Ascending sorting by default or descending if false.") @RequestParam(value = "sortDesc", required = false) final Boolean sortDesc) {
        final var queryFilter = ProductQueryFilter.builder()
                .name(name)
                .price(price)
                .priceMin(priceMin)
                .priceMax(priceMax)
                .lessThan(lessThan)
                .greaterThan(greaterThan)
                .build();

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
                throw new ApiBadRequestException(ProductExceptionMessage.PRICE_MIN_AND_PRICE_MAX_MUST_BE_PASSED.withParameters(queryFilter.getPriceMin(), queryFilter.getPriceMax()));
            }
            if (queryFilter.isPriceLessOrGreaterUsed()) {
                throw new ApiBadRequestException(ProductExceptionMessage.PRICE_LESS_THAN_OR_GREATER_THAN_NOT_ALLOWED_FOR_PRICE_RANGE);
            }
        }
        return mapper.map(
                Product.findProducts(queryFilter, pageNumber, pageSize, sortOrder, sortDesc)
        );
    }

    @Operation(
            summary = "Creates new product.",
            description = "Create a new product with all details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProductResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ProductResponse create(@Parameter(description = "Product request data.") @Valid @RequestBody final CreateProductRequest createProductRequest) {
        return mapper.map(
                Product.create(mapper.map(createProductRequest))
        );
    }

    @Operation(
            summary = "Update product.",
            description = "Partially update product.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ProductResponse.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Bad Request",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    ),
                    @ApiResponse(responseCode = "409", description = "Conflict",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ProductResponse update(@Parameter(description = "Product id.") @PathVariable("id") final String id,
                           @Parameter(description = "Product request data.") @Valid @RequestBody final UpdateProductRequest updateProductRequest) {
        if (!updateProductRequest.isAnyUpdate()) {
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_NO_UPDATE_DATA);
        }
        return mapper.map(
                Product.update(mapper.map(id, updateProductRequest))
        );
    }

    @Operation(
            summary = "Delete product.",
            description = "Remove product by given id.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "404", description = "Not Found",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ExceptionMessage.class)
                            )
                    )
            }
    )
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    void deleteById(@Parameter(description = "Product id.") @PathVariable("id") final String id) {
        Product.delete(id);
    }

}
