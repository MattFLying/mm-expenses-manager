package mm.expenses.manager.product.processor.conversion;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

/**
 * Step in chain of currencies conversion for {@link Product}s with missing currencies.
 * Finds products that have missing currencies.
 */
@Slf4j
@RequiredArgsConstructor
final class FindProductsWithMissingPriceCurrencies extends ProductsCurrenciesConversionChain<Pageable, Page<Product>> {

    private final ProductRepository repository;

    @Getter
    private Collection<Product> products;

    @Override
    public Page<Product> handleRequest(final Pageable pageable) {
        try {
            final var productsWithMissingCurrencies = repository.findAllWithCurrenciesLessThan(CurrencyCode.available().size(), pageable);
            products = productsWithMissingCurrencies.getContent();

            if (hasNext()) {
                return next.handleRequest(pageable);
            }
            return productsWithMissingCurrencies;
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown products prices conversion error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCTS_PRICES_CANNOT_BE_CONVERTED, exception);
        }
    }

}
