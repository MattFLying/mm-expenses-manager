package mm.expenses.manager.product.price;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.price.Price;
import mm.expenses.manager.product.ProductCommonValidation;
import mm.expenses.manager.product.api.product.model.CreatePriceRequest;
import mm.expenses.manager.product.api.product.model.UpdatePriceRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PriceService {

    private final PriceMapper mapper;

    public Price create(final CreatePriceRequest request) {
        return mapper.map(request);
    }

    public Price update(final Price oldPrice, final UpdatePriceRequest newPrice) {
        final var price = Price.builder()
                .value(getUpdatedOrOriginalValue(oldPrice, newPrice))
                .currency(getUpdatedOrOriginalCurrency(oldPrice, newPrice))
                .build();
        if (!ProductCommonValidation.isPriceValueValid(price.getValue())) {
            throw new ApiValidationException(ProductExceptionMessage.PRODUCT_PRICE_VALUE_NOT_VALID.withParameters(newPrice.getValue()));
        }
        if (!ProductCommonValidation.isPriceCurrencyCodeValid(price.getCurrency())) {
            throw new ApiValidationException(ProductExceptionMessage.PRODUCT_PRICE_CURRENCY_NOT_VALID);
        }
        return price;
    }

    private BigDecimal getUpdatedOrOriginalValue(final Price oldPrice, final UpdatePriceRequest newPrice) {
        return Objects.nonNull(newPrice.getValue()) ? newPrice.getValue() : oldPrice.getValue();
    }

    private CurrencyCode getUpdatedOrOriginalCurrency(final Price oldPrice, final UpdatePriceRequest newPrice) {
        return Objects.nonNull(newPrice.getCurrency())
                ? CurrencyCode.getCurrencyFromString(newPrice.getCurrency())
                : oldPrice.getCurrency();
    }

}
