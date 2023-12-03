package mm.expenses.manager.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;

import java.util.stream.Stream;

import static mm.expenses.manager.product.ProductCommonValidation.*;

/**
 * Custom product validator for any requests.
 */
class ProductValidator implements ConstraintValidator<ValidateProduct, Object> {

    @Override
    public boolean isValid(final Object request, final ConstraintValidatorContext context) {
        if (request instanceof CreateProductRequest newRequest) {
            return isNewProductRequestValid(newRequest, context);
        }
        if (request instanceof UpdateProductRequest updateRequest) {
            return isUpdateProductRequestValid(updateRequest, context);
        }
        return false;
    }

    private boolean isNewProductRequestValid(final CreateProductRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }
        final var isNameValid = isProductNameNotEmpty(request.getName());
        final var isDetailsValid = isNewProductDetailsValid(request.getDetails());
        final var isPriceValid = isPriceValid(request.getPrice().getValue(), request.getPrice().getCurrency(), context);

        context.disableDefaultConstraintViolation();
        handleProductName(!isNameValid, context);
        handleProductDetails(!isDetailsValid, context);

        return isNameValid && isDetailsValid && isPriceValid;
    }

    private boolean isUpdateProductRequestValid(final UpdateProductRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }
        var isNameValid = true;
        var isPriceValid = true;

        context.disableDefaultConstraintViolation();
        if (request.getName() != null) {
            isNameValid = isProductNameNotEmpty(request.getName());
            handleProductName(!isNameValid, context);
        }
        if (request.getPrice() != null) {
            isPriceValid = isPriceValid(request.getPrice().getValue(), request.getPrice().getCurrency(), context);
        }
        return Stream.of(isNameValid, isPriceValid).anyMatch(isValid -> isValid);
    }

}
