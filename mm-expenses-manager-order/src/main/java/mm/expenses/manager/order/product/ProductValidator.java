package mm.expenses.manager.order.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.order.currency.Price;
import mm.expenses.manager.order.currency.PriceMapper;
import mm.expenses.manager.order.product.model.CreateNewProduct;
import mm.expenses.manager.order.product.model.Product;
import mm.expenses.manager.order.product.model.UpdateProduct;
import mm.expenses.manager.order.validator.DomainValidator.Validator;
import mm.expenses.manager.order.validator.ValidatorMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@Component
@RequiredArgsConstructor
class ProductValidator implements Validator<Product, CreateNewProduct, UpdateProduct> {

    private final PriceMapper priceMapper;

    @Override
    public Collection<ValidatorMessage> validateDomain(final Product object) {
        return validate(object.getName(), object.getPrice());
    }

    @Override
    public Collection<ValidatorMessage> validateNew(final CreateNewProduct object) {
        return validate(object.getName(), priceMapper.map(object.getPrice()));
    }

    @Override
    public Collection<ValidatorMessage> validateUpdate(final UpdateProduct object) {
        return validate(object.getName(), priceMapper.map(object.getPrice()));
    }

    private Collection<ValidatorMessage> validate(final String name, final Price price) {
        final var validators = new ArrayList<ValidatorMessage>();
        validators.addAll(validateProductName(name));
        validators.addAll(validateProductPrice(price));
        return validators;
    }

    private Collection<ValidatorMessage> validateProductName(final String name) {
        if (StringUtils.isBlank(name)) {
            return Collections.singleton(new ValidatorMessage("Name cannot be empty."));
        }
        return Collections.emptyList();
    }

    private Collection<ValidatorMessage> validateProductPrice(final Price price) {
        if (Objects.isNull(price)) {
            return Collections.singleton(new ValidatorMessage("Price cannot be null."));
        } else {
            final var validators = new ArrayList<ValidatorMessage>();
            final var isFreeOrNegative = isFreePriceOrNegative(price.getAmount());
            if (isFreeOrNegative || !price.isPriceFormatValid()) {
                validators.add(new ValidatorMessage("Price is not valid."));
            }

            final var isCurrencyEmpty = Objects.isNull(price.getCurrency());
            if (isCurrencyEmpty) {
                validators.add(new ValidatorMessage("Currency cannot be empty."));
            }
            return validators;
        }
    }

    private boolean isFreePriceOrNegative(final BigDecimal amount) {
        return Objects.nonNull(amount) && amount.compareTo(BigDecimal.ZERO) <= 0;
    }

}
