package mm.expenses.manager.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import mm.expenses.manager.order.api.order.model.CreateNewOrderRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderRequest;

import java.util.stream.Stream;

import static mm.expenses.manager.order.OrderCommonValidation.*;

/**
 * Custom order validator for any requests.
 */
class OrderValidator implements ConstraintValidator<ValidateOrder, Object> {

    @Override
    public boolean isValid(final Object request, final ConstraintValidatorContext context) {
        if (request instanceof CreateNewOrderRequest newRequest) {
            return isNewOrderRequestValid(newRequest, context);
        }
        if (request instanceof UpdateOrderRequest updateRequest) {
            return isUpdateOrderRequestValid(updateRequest, context);
        }
        return false;
    }

    private boolean isNewOrderRequestValid(final CreateNewOrderRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }
        context.disableDefaultConstraintViolation();

        final var isNameValid = isOrderNameNotEmpty(request.getName());
        final var newOrderedProductsValid = areNewOrderedProductsValid(request.getOrderedProducts(), context);
        handleOrderName(!isNameValid, context);

        return isNameValid && newOrderedProductsValid;
    }

    private boolean isUpdateOrderRequestValid(final UpdateOrderRequest request, final ConstraintValidatorContext context) {
        if (request == null) {
            return false;
        }
        var isNameValid = true;
        var updatedOrderedProductsValid = true;
        var newOrderedProductsValid = true;

        context.disableDefaultConstraintViolation();
        if (request.getName() != null) {
            isNameValid = isOrderNameNotEmpty(request.getName());
            handleOrderName(!isNameValid, context);
        }
        if (request.getOrderedProducts() != null) {
            updatedOrderedProductsValid = areUpdatedOrderedProductsValid(request.getOrderedProducts(), context);
            newOrderedProductsValid = areNewOrderedProductsValid(request.getNewProducts(), context);
        }
        return Stream.of(isNameValid, updatedOrderedProductsValid, newOrderedProductsValid).anyMatch(isValid -> isValid);
    }

}
