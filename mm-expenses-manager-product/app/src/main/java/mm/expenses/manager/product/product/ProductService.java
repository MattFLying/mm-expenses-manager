package mm.expenses.manager.product.product;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.async.AsyncMessageProducer;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.common.postgresql.filter.EntityFilter;
import mm.expenses.manager.common.postgresql.specification.criteria.AdditionalCriteriaParameter;
import mm.expenses.manager.common.utils.i18n.CurrencyCode;
import mm.expenses.manager.product.ProductCommonValidation;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.api.product.model.ProductResponse;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.ProductPriceService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductFilterViewRepository productFilterViewRepository;
    private final AsyncMessageProducer producer;
    private final ProductPriceService priceService;
    private final ProductMapper mapper;
    private final ProductFilterViewSpecificationHandler productFilterViewSpecificationHandler;
    private final PriceConverter priceConverter;

    @Transactional
    public ProductResponse create(final CreateProductRequest request) {
        val newProduct = Product.builder()
                .name(request.getName())
                .details(request.getDetails())
                .build();

        val newPrice = priceService.create(request);
        newProduct.setPrice(List.of(newPrice));

        val saved = saveProduct(newProduct, AsyncKafkaOperation.CREATE);

        return mapper.mapProductResponse(saved, newPrice);
    }

    @Transactional
    public Product update(final UUID id, final UpdateProductRequest request) {
        var existedProduct = repository.findByIdAndIsDeleted(id, false)
                .orElseThrow(() -> new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(id)));

        val updatedTime = Instant.now();
        var updated = false;
        updated = updateProductName(request, existedProduct, updated);
        updated = updateProductDetails(request, existedProduct, updated);
        updated = updateProductPrice(request, existedProduct, updatedTime, updated);
        if (updated) {
            existedProduct.setLastModifiedAt(updatedTime);
            existedProduct = saveProduct(existedProduct, AsyncKafkaOperation.UPDATE);
        }
        return existedProduct;
    }

    public void delete(final UUID productId) {
        repository.findByIdAndIsDeleted(productId, false)
                .ifPresentOrElse(
                        product -> {
                            product.setDeleted(true);
                            saveProduct(product, AsyncKafkaOperation.DELETE);
                        },
                        () -> {
                            throw new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId));
                        });
    }

    public void delete(final Collection<UUID> productIds) {
        repository.deleteByIdIn(productIds);
    }

    public Product findById(final UUID productId, final boolean isDeleted) {
        return repository.findByIdAndIsDeleted(productId, isDeleted)
                .orElseThrow(() -> new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId)));
    }

    public Page<Product> findDeleted(final Pageable pageable) {
        return repository.findAllByIsDeletedTrue(pageable);
    }

    public Page<ProductFilterView> filterProducts(final EntityFilter queryFilter) {
        Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

        val defaultCurrency = priceConverter.getDefaultCurrency();
        val filterParameters = queryFilter.buildQueryParams();
        val hasCurrencyRequested = ((ProductFilter) queryFilter).isProductsPriceCurrencyOriented();

        val additionalCriteriaParameters = new ArrayList<AdditionalCriteriaParameter>();

        // if there are no other expectations return the original price
        // at the end the expected product's price is the specific currency or the original price currency
        additionalCriteriaParameters.add(AdditionalCriteriaParameter.of(ProductFilterView.IS_PRICE_ORIGINAL_FIELD_NAME, true));
        if (!hasCurrencyRequested) {
            // but if specific currency is not requested then return the default currency
            additionalCriteriaParameters.add(AdditionalCriteriaParameter.of(ProductFilterView.PRICE_CURRENCY_FIELD_NAME, defaultCurrency));
        }

        val specificationResult = productFilterViewSpecificationHandler.handle(
                filterParameters,
                additionalCriteriaParameters.toArray(new AdditionalCriteriaParameter[0])
        );
        val pagedOrders = productFilterViewRepository.findAll(specificationResult.specification(), specificationResult.pageable());

        val isCurrencyConversionRequired = queryFilter.shouldConvertCurrenciesToDefault();
        if (isCurrencyConversionRequired) {
            // calculate prices if there are different currencies than default
            convertCurrencies(pagedOrders, defaultCurrency);
        }

        // in case if currency conversion is needed and sorting by price value is expected then it is required to sort it again here
        val priceValueSortingOrder = ((ProductFilter) queryFilter).getPriceValueSortingOrder();
        if (isCurrencyConversionRequired && priceValueSortingOrder.isPresent()) {
            val direction = priceValueSortingOrder.get().getDirection();
            val content = pagedOrders.getContent();

            return new PageImpl<>(sortFilteredProductsByPriceValue(direction, content), pagedOrders.getPageable(), pagedOrders.getTotalElements());
            // in case if currency conversion is not needed but sorting by price value is expected then sorting is required again to keep the expected currency first
        } else if (priceValueSortingOrder.isPresent()) {
            val direction = priceValueSortingOrder.get().getDirection();
            val content = pagedOrders.getContent();

            return new PageImpl<>(sortFilteredProductsByPriceCurrencyAndValue(direction, content, defaultCurrency), pagedOrders.getPageable(), pagedOrders.getTotalElements());
        }
        return pagedOrders;
    }

    private void convertCurrencies(final Page<ProductFilterView> pagedOrders, final CurrencyCode defaultCurrency) {
        val isCurrencyConversionNeeded = pagedOrders.getContent()
                .stream()
                .anyMatch(product -> !Objects.equals(product.getPriceCurrency(), defaultCurrency));
        if (isCurrencyConversionNeeded) {
            val convertedProducts = priceConverter.convertPrices(pagedOrders.getContent())
                    .stream()
                    .collect(Collectors.toMap(ProductFilterView::getProductId, Function.identity()));

            pagedOrders.getContent()
                    .forEach(product -> {
                        val convertedProduct = convertedProducts.get(product.getProductId());
                        if (Objects.isNull(convertedProduct)) {
                            return;
                        }
                        product.setPriceValue(convertedProduct.getPriceValue());
                        product.setPriceCurrency(convertedProduct.getPriceCurrency());
                    });
        }
    }

    private List<ProductFilterView> sortFilteredProductsByPriceValue(final Sort.Direction direction, final List<ProductFilterView> content) {
        val comparator = direction.isAscending()
                ? Comparator.comparing(ProductFilterView::getPriceValue)
                : Comparator.comparing(ProductFilterView::getPriceValue).reversed();

        return content.stream()
                .sorted(comparator)
                .toList();
    }

    private List<ProductFilterView> sortFilteredProductsByPriceCurrencyAndValue(final Sort.Direction direction, final List<ProductFilterView> content, final CurrencyCode expectedCurrencyCode) {
        val comparator = direction.isAscending()
                ? Comparator.<ProductFilterView, Boolean>comparing(product -> !Objects.equals(expectedCurrencyCode, product.getPriceCurrency())).thenComparing(ProductFilterView::getPriceCurrency).thenComparing(ProductFilterView::getPriceValue)
                : Comparator.<ProductFilterView, Boolean>comparing(product -> Objects.equals(expectedCurrencyCode, product.getPriceCurrency())).thenComparing(ProductFilterView::getPriceCurrency).thenComparing(ProductFilterView::getPriceValue).reversed();

        return content.stream()
                .sorted(comparator)
                .toList();
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
        val oldDetails = existedProduct.getDetails();
        val newDetails = request.getDetails();
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

    private boolean updateProductPrice(final UpdateProductRequest request, final Product existedProduct, Instant updatedTime, boolean updated) {
        if (Objects.nonNull(request.getPrice())) {
            existedProduct.setPrice(priceService.update(existedProduct, request.getPrice(), updatedTime));
            updated = true;
        }
        return updated;
    }

    private Product saveProduct(final Product product, final AsyncKafkaOperation operation) {
        final var savedProduct = repository.save(product);
        producer.send(mapper.map(savedProduct, operation));
        return savedProduct;
    }

}
