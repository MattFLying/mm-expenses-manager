package mm.expenses.manager.product.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.product.category.dto.response.CategoryResponse;
import mm.expenses.manager.product.category.dto.request.CreateCategoryRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("categories")
@Tag(name = "Categories", description = "Provide API for categories management.")
class CategoryController {

    private final CategoryMapper mapper;

    @Operation(
            summary = "Creates new category.",
            description = "Create a new category with all details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = CategoryResponse.class)
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
    CategoryResponse create(@Parameter(description = "Category request data.") @Valid @RequestBody final CreateCategoryRequest createCategoryRequest) {
        return mapper.map(Category.create(mapper.map(createCategoryRequest)));
    }

}
