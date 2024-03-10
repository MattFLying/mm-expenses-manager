package mm.expenses.manager.order;

import jakarta.validation.ConstraintValidatorContext;
import mm.expenses.manager.common.exceptions.base.ExceptionType;
import mm.expenses.manager.order.api.order.model.CreateNewOrderedProductRequest;
import mm.expenses.manager.order.api.order.model.UpdateOrderedProductRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class OrderCommonValidation {

    public static void handleOrderName(final boolean isNameEmpty, ConstraintValidatorContext context) {
        if (isNameEmpty) {
            final var exceptionType = OrderExceptionMessage.ORDER_NAME_EMPTY;
            context.buildConstraintViolationWithTemplate(exceptionType.getMessage()).addPropertyNode(exceptionType.getCode()).addConstraintViolation();
        }
    }

    public static void handleOrderedProducts(final boolean areOrderedProductsPresent, final Collection<ExceptionType> orderedProductsFailures, ConstraintValidatorContext context) {
        if (!areOrderedProductsPresent) {
            final var exceptionType = OrderExceptionMessage.ORDER_PRODUCTS_EMPTY;
            context.buildConstraintViolationWithTemplate(exceptionType.getMessage()).addPropertyNode(exceptionType.getCode()).addConstraintViolation();
            return;
        }
        handleOrderedProductsUpdate(orderedProductsFailures, context);
    }

    public static void handleOrderedProductsUpdate(final Collection<ExceptionType> orderedProductsFailures, ConstraintValidatorContext context) {
        if (CollectionUtils.isNotEmpty(orderedProductsFailures)) {
            orderedProductsFailures.forEach(
                    orderedProductFailure -> context.buildConstraintViolationWithTemplate(orderedProductFailure.getMessage())
                            .addPropertyNode(orderedProductFailure.getCode())
                            .addConstraintViolation()
            );
        }
    }

    public static boolean isOrderNameNotEmpty(final String name) {
        return StringUtils.isNotBlank(name);
    }

    public static boolean areNewOrderedProductsValid(final List<CreateNewOrderedProductRequest> orderedProducts, final ConstraintValidatorContext context) {
        final var areOrderedProductsPresent = CollectionUtils.isNotEmpty(orderedProducts);
        final var orderedProductsFailures = orderedProducts.stream()
                .map(orderedProduct -> validateProductQuantity(orderedProduct.getProductId(), orderedProduct.getQuantity()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        handleOrderedProducts(areOrderedProductsPresent, orderedProductsFailures, context);

        return areOrderedProductsPresent && CollectionUtils.isEmpty(orderedProductsFailures);
    }

    public static boolean areUpdatedOrderedProductsValid(final List<UpdateOrderedProductRequest> orderedProducts, final ConstraintValidatorContext context) {
        if (CollectionUtils.isEmpty(orderedProducts)) {
            return true;
        }
        final var orderedProductsFailures = orderedProducts.stream()
                .map(orderedProduct -> validateProductQuantity(orderedProduct.getProductId(), orderedProduct.getQuantity()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        handleOrderedProductsUpdate(orderedProductsFailures, context);

        return CollectionUtils.isEmpty(orderedProductsFailures);
    }

    private static ExceptionType validateProductQuantity(final UUID orderId, final Double orderQuantity) {
        if (Objects.isNull(orderQuantity)) {
            return OrderExceptionMessage.ORDER_QUANTITY_EMPTY.withParameters(orderId);
        }
        if (orderQuantity == Double.MIN_VALUE) {
            return OrderExceptionMessage.ORDER_QUANTITY_ZERO.withParameters(orderId);
        }
        if (Double.isNaN(orderQuantity) || Double.doubleToRawLongBits(orderQuantity) < 0) {
            return OrderExceptionMessage.ORDER_QUANTITY_UNKNOWN_OR_NEGATIVE.withParameters(orderId);
        }
        return null;
    }

}
