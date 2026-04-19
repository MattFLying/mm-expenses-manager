package mm.expenses.manager.product.processor.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiBadRequestException;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.product.ProductCommonValidation;
import mm.expenses.manager.product.api.product.model.UpdatePriceRequest;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPrice;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductAsyncHandler;
import mm.expenses.manager.product.processor.ProductRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Step in chain of {@link Product} update.
 * Modifies basic information in product if any detected.
 */
@Slf4j
@RequiredArgsConstructor
final class UpdateProductData extends UpdateProductChain {

    final static String IS_UPDATED_KEY = "isUpdated";

    private final ProductRepository repository;
    private final ProductAsyncHandler asyncHandler;
    private final Instant modificationTime;

    @Override
    public Product handleRequest(final UpdateProductRequest request) {
        try {
            context.getArgument(FindProductToUpdate.PRODUCT_KEY)
                    .ifPresentOrElse(
                            product -> {
                                var updated = false;
                                updated = updateProductName(request, (Product) product, updated);
                                updated = updateProductDetails(request, (Product) product, updated);
                                updated = updateProductPrice(request, (Product) product, updated);

                                context.addArgument(IS_UPDATED_KEY, updated);
                                context.addArgument(FindProductToUpdate.PRODUCT_KEY, product);
                            },
                            () -> {
                                log.error("Cannot update product because of one of required parameters is null.");
                                throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED);
                            }
                    );

            if (!hasNext()) {
                setNextHandler(new SaveUpdatedProduct(repository, asyncHandler, modificationTime));
            }
            return next.handleRequest(request);
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown product update error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_UPDATED, exception);
        }
    }

    private boolean updateProductName(final UpdateProductRequest request, final Product existedProduct, boolean updated) {
        if (Objects.nonNull(request.getName())) {
            if (ProductCommonValidation.isNameEmpty(request.getName())) {
                throw new ApiValidationException(ProductExceptionMessage.PRODUCT_NAME_NOT_VALID.withParameters(request.getName()));
            }
            existedProduct.setName(request.getName());
            updated = true;
        }
        return updated;
    }

    private boolean updateProductDetails(final UpdateProductRequest request, final Product existedProduct, boolean updated) {
        final var oldDetails = existedProduct.getDetails();
        final var newDetails = request.getDetails();
        if (MapUtils.isNotEmpty(request.getDetails())) {
            newDetails.forEach((name, value) -> {
                if (oldDetails.containsKey(name)) {
                    oldDetails.replace(name, value);
                } else {
                    oldDetails.put(name, value);
                }
            });
            updated = true;
        }
        return updated;
    }

    private boolean updateProductPrice(final UpdateProductRequest request, final Product existedProduct, boolean updated) {
        if (Objects.nonNull(request.getPrice())) {
            existedProduct.setPrice(update(existedProduct, request.getPrice()));
            updated = true;
        }
        return updated;
    }

    private List<ProductPrice> update(final Product product, final UpdatePriceRequest newPrice) {
        validateUpdatedPrice(newPrice);

        var oldPrices = product.getPrices();
        if (CollectionUtils.isEmpty(oldPrices)) {
            oldPrices = new ArrayList<>();
            oldPrices.add(createNewOriginalPrice(product, newPrice));
            return oldPrices;
        }

        updateOriginalPrice(newPrice, oldPrices);
        return oldPrices;
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

    private ProductPrice createNewOriginalPrice(final Product product, final UpdatePriceRequest newPrice) {
        return ProductPrice.builder()
                .value(newPrice.getValue())
                .currency(CurrencyCode.getCurrencyFromString(newPrice.getCurrency()))
                .date(DateUtils.instantToLocalDate(modificationTime).toString())
                .isOriginal(true)
                .createdAt(modificationTime)
                .lastModifiedAt(modificationTime)
                .isDeleted(product.isDeleted())
                .build();
    }

    private void updateOriginalPrice(final UpdatePriceRequest newPrice, final List<ProductPrice> oldPrices) {
        final var newValue = newPrice.getValue();
        final var newCurrency = newPrice.getCurrency();

        final var isNewValueDefined = Objects.nonNull(newValue);
        final var isNewCurrencyDefined = Objects.nonNull(newCurrency);

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
                                originalPrice.setLastModifiedAt(modificationTime);
                            },
                            () -> {
                                throw new ApiBadRequestException(ProductExceptionMessage.PRODUCT_PRICE_ORIGINAL_NOT_FOUND);
                            }
                    );
        }
    }

}
