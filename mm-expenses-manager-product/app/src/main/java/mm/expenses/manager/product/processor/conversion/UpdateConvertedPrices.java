package mm.expenses.manager.product.processor.conversion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionRequest;
import mm.expenses.manager.finance.api.calculations.model.CurrencyConversionResponse;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPrice;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductRepository;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Step in chain of currencies conversion for {@link Product}s with missing currencies.
 * Convert and update missing prices for products.
 */
@Slf4j
@RequiredArgsConstructor
final class UpdateConvertedPrices extends ProductsCurrenciesConversionChain<Collection<Product>, Map<Product, List<ProductPrice>>> {

    private final ProductRepository repository;
    private final PriceConverter priceConverter;
    private final Instant updateTime;

    @Override
    public Map<Product, List<ProductPrice>> handleRequest(final Collection<Product> products) {
        try {
            final var updatedProducts = new HashMap<Product, List<ProductPrice>>();
            final var productsById = products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
            final var currenciesAvailable = CurrencyCode.available();

            final var pricesConversionRequests = new ArrayList<CurrencyConversionRequest>();
            productsById.forEach((productId, product) -> findAndPrepareProductPriceConversionRequests(product, currenciesAvailable, pricesConversionRequests));

            if (!pricesConversionRequests.isEmpty()) {
                final var pricesByProductId = new HashMap<String, List<ProductPrice>>();
                final var convertedPrices = getConvertedPrices(pricesConversionRequests);
                final var productsWithUpdatedPrices = getProductsWithUpdatedPrices(products, convertedPrices, pricesByProductId);

                updatedProducts.putAll(productsWithUpdatedPrices.stream()
                        .map(repository::save)
                        .collect(Collectors.toMap(
                                Function.identity(),
                                product -> pricesByProductId.get(product.getId().toString())
                        ))
                );
            }

            if (hasNext()) {
                return next.handleRequest(products);
            }
            return updatedProducts;
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products prices conversion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_PRICES_CANNOT_BE_CONVERTED, exception);
        }
    }

    private void findAndPrepareProductPriceConversionRequests(final Product product, final Set<CurrencyCode> currenciesAvailable, final List<CurrencyConversionRequest> pricesConversionRequests) {
        final var prices = product.getPrices();
        final var pricesByCurrency = prices.stream().collect(Collectors.groupingBy(ProductPrice::getCurrency));

        final var missingCurrencies = currenciesAvailable.stream()
                .filter(currencyCode -> !pricesByCurrency.containsKey(currencyCode))
                .collect(Collectors.toSet());

        // original price is the main goal here but in case if it is missing just takes the first available price
        // and prepare conversion requests based on that. Price is always required to be present, so there is always at least one position
        final var priceOriginalOrAny = prices.stream()
                .filter(ProductPrice::isOriginal)
                .findAny()
                .orElseGet(() -> prices.get(0));

        pricesConversionRequests.addAll(priceConverter.createConversionRequests(product, missingCurrencies, priceOriginalOrAny));
    }

    private Map<String, List<CurrencyConversionResponse>> getConvertedPrices(final List<CurrencyConversionRequest> pricesConversionRequests) {
        return priceConverter.convert(pricesConversionRequests)
                .stream()
                .peek(response -> {
                    final var id = response.getId();

                    // id needs to be split here to easily group all responses by product id
                    final var idFromCorrelationId = id.substring(0, id.indexOf(PriceConverter.CORRELATION_ID_SEPARATOR));
                    response.setId(idFromCorrelationId);
                })
                .collect(Collectors.groupingBy(CurrencyConversionResponse::getId));
    }

    private List<Product> getProductsWithUpdatedPrices(final Collection<Product> products, final Map<String, List<CurrencyConversionResponse>> convertedPrices, final Map<String, List<ProductPrice>> pricesByProductId) {
        return products.stream()
                .filter(product -> convertedPrices.containsKey(product.getId().toString()))
                .peek(product -> {
                    final var convertedPricesForProduct = convertedPrices.get(product.getId().toString());
                    final var newPrices = convertedPricesForProduct.stream()
                            .map(converted -> createNewPrice(product, converted))
                            .toList();

                    pricesByProductId.put(product.getId().toString(), newPrices);
                    product.addPrices(newPrices);
                })
                .toList();
    }

    private ProductPrice createNewPrice(final Product product, final CurrencyConversionResponse price) {
        return ProductPrice.builder()
                .value(BigDecimalWrapper.of(price.getTo().getValue()))
                .currency(CurrencyCode.getCurrencyFromString(price.getTo().getCode()))
                .date(price.getDate().toString())
                .isOriginal(false)
                .product(product)
                .createdAt(updateTime)
                .lastModifiedAt(updateTime)
                .isDeleted(product.isDeleted())
                .build();
    }

}
