package mm.expenses.manager.order.order;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.order.order.model.*;
import mm.expenses.manager.validator.DomainValidator.Validator;
import mm.expenses.manager.validator.ValidatorMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class OrderValidator implements Validator<Order, CreateNewOrder, UpdateOrder> {

    @Override
    public Collection<ValidatorMessage> validateDomain(final Order object) {
        final var validators = new ArrayList<ValidatorMessage>();
        validators.addAll(validateName(object.getName()));
        validators.addAll(validateDomainOrderedProducts(object.getOrderedProducts()));
        return validators;
    }

    @Override
    public Collection<ValidatorMessage> validateNew(final CreateNewOrder object) {
        final var validators = new ArrayList<ValidatorMessage>();
        validators.addAll(validateName(object.getName()));
        validators.addAll(validateNewOrderedProducts(object.getOrderedProducts()));
        return validators;
    }

    @Override
    public Collection<ValidatorMessage> validateUpdate(final UpdateOrder object) {
        final var validators = new ArrayList<ValidatorMessage>();
        validators.addAll(validateName(object.getName()));
        validators.addAll(validateUpdatedOrderedProducts(object.getOrderedProducts()));
        validators.addAll(validateNewOrderedProducts(object.getNewProducts()));
        return validators;
    }

    private Collection<ValidatorMessage> validateDomainOrderedProducts(final List<Order.OrderedProduct> objects) {
        return objects.stream()
                .map(orderedProduct -> validateProductQuantity(orderedProduct.getId(), orderedProduct.getQuantity()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<ValidatorMessage> validateNewOrderedProducts(final List<CreateNewOrder.CreateNewOrderedProduct> objects) {
        return objects.stream()
                .map(orderedProduct -> validateProductQuantity(orderedProduct.getProductId(), orderedProduct.getQuantity()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<ValidatorMessage> validateUpdatedOrderedProducts(final List<UpdateOrder.UpdateOrderedProduct> objects) {
        return objects.stream()
                .map(orderedProduct -> validateProductQuantity(orderedProduct.getId(), orderedProduct.getQuantity()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Collection<ValidatorMessage> validateName(final String name) {
        if (StringUtils.isBlank(name)) {
            return Collections.singleton(new ValidatorMessage("Name cannot be empty."));
        }
        return Collections.emptyList();
    }

    private Collection<ValidatorMessage> validateProductQuantity(final String id, final Double quantity) {
        final var validators = new ArrayList<ValidatorMessage>();
        if (Objects.isNull(quantity)) {
            validators.add(new ValidatorMessage("Quantity cannot be empty for id: " + id));
            return validators;
        }
        if (quantity == 0) {
            validators.add(new ValidatorMessage("Quantity cannot be zero for id: " + id));
        }
        if (Double.isNaN(quantity) || Double.doubleToRawLongBits(quantity) < 0) {
            validators.add(new ValidatorMessage("Quantity cannot be unknown or negative for id: " + id));
        }
        return validators;
    }

}
