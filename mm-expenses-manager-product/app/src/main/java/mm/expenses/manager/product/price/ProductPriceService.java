package mm.expenses.manager.product.price;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.product.ProductCommonValidation;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.api.product.model.UpdatePriceRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.product.Product;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ProductPriceService {

    private final ProductPriceMapper mapper;

    public ProductPrice create(final CreateProductRequest request) {
        return mapper.map(request, LocalDate.now().toString());
    }

    public List<ProductPrice> update(final Product product, final UpdatePriceRequest newPrice, final Instant updatedTime) {
        validateUpdatedPrice(newPrice);

        var oldPrices = product.getPrices();
        if (CollectionUtils.isEmpty(oldPrices)) {
            oldPrices = new ArrayList<>();
            oldPrices.add(createNewOriginalPrice(product, newPrice, updatedTime));
            return oldPrices;
        }

        updateOriginalPrice(newPrice, updatedTime, oldPrices);
        return oldPrices;
    }

    private ProductPrice createNewOriginalPrice(final Product product, final UpdatePriceRequest newPrice, final Instant updatedTime) {
        return ProductPrice.builder()
                .value(newPrice.getValue())
                .currency(CurrencyCode.getCurrencyFromString(newPrice.getCurrency()))
                .date(DateUtils.instantToLocalDate(updatedTime).toString())
                .isOriginal(true)
                .createdAt(updatedTime)
                .lastModifiedAt(updatedTime)
                .isDeleted(product.isDeleted())
                .build();
    }

    private void updateOriginalPrice(final UpdatePriceRequest newPrice, final Instant updatedTime, final List<ProductPrice> oldPrices) {
        val newValue = newPrice.getValue();
        val newCurrency = newPrice.getCurrency();

        val isNewValueDefined = Objects.nonNull(newValue);
        val isNewCurrencyDefined = Objects.nonNull(newCurrency);

        if (isNewValueDefined || isNewCurrencyDefined) {
            oldPrices.stream()
                    .filter(ProductPrice::isOriginal)
                    .findAny()
                    .ifPresentOrElse(
                            originalPrice -> {
                                if (isNewValueDefined) {
                                    originalPrice.setValue(newValue);
                                }
                                if (isNewCurrencyDefined) {
                                    originalPrice.setCurrency(CurrencyCode.getCurrencyFromString(newCurrency));
                                }
                                originalPrice.setLastModifiedAt(updatedTime);
                            },
                            () -> {
                                throw new ApiBadRequestException(ProductExceptionMessage.PRODUCT_PRICE_ORIGINAL_NOT_FOUND);
                            }
                    );
        }
    }

    private void validateUpdatedPrice(final UpdatePriceRequest newPrice) {
        if (Objects.nonNull(newPrice)) {
            if (Objects.nonNull(newPrice.getValue())) {
                if (!ProductCommonValidation.isPriceValueValid(newPrice.getValue())) {
                    throw new ApiValidationException(ProductExceptionMessage.PRODUCT_PRICE_VALUE_NOT_VALID.withParameters(newPrice.getValue()));
                }
            }
            if (Objects.nonNull(newPrice.getCurrency())) {
                if (!ProductCommonValidation.isPriceCurrencyCodeValid(newPrice.getCurrency())) {
                    throw new ApiValidationException(ProductExceptionMessage.PRODUCT_PRICE_CURRENCY_NOT_VALID);
                }
            }
        }
    }

}
