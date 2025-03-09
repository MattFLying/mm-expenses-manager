package mm.expenses.manager.product.product;

import lombok.RequiredArgsConstructor;
import lombok.val;
import mm.expenses.manager.common.async.AsyncMessageProducer;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.common.exceptions.api.ApiValidationException;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.product.ProductCommonValidation;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.currency.PriceConverter;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.price.PriceService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final AsyncMessageProducer producer;
    private final PriceService priceService;
    private final ProductMapper mapper;
    private final ProductSpecificationHandler specificationHandler;
    private final PriceConverter priceConverter;

    public Product create(final CreateProductRequest request) {
        final var newPrice = priceService.create(request.getPrice());
        final var newProduct = Product.builder()
                .name(request.getName())
                .price(newPrice)
                .details(request.getDetails())
                .build();

        return saveProduct(newProduct, AsyncKafkaOperation.CREATE);
    }

    public Product update(final UUID id, final UpdateProductRequest request) {
        var existedProduct = repository.findById(id)
                .orElseThrow(() -> new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(id)));

        final var newName = request.getName();
        if (Objects.nonNull(request.getName())) {
            if (ProductCommonValidation.isNameEmpty(newName)) {
                throw new ApiValidationException(ProductExceptionMessage.PRODUCT_NAME_NOT_VALID.withParameters(newName));
            }
            existedProduct.setName(newName);
        }

        final var newDetails = request.getDetails();
        if (MapUtils.isNotEmpty(newDetails)) {
            existedProduct.setDetails(newDetails);
        }

        final var newPrice = request.getPrice();
        if (Objects.nonNull(newPrice)) {
            existedProduct.setPrice(priceService.update(existedProduct.getPrice(), newPrice));
        }

        return saveProduct(existedProduct, AsyncKafkaOperation.UPDATE);
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

    private Product saveProduct(final Product product, final AsyncKafkaOperation operation) {
        final var savedProduct = repository.save(product);
        producer.send(mapper.map(savedProduct, operation));
        return savedProduct;
    }

    public Page<Product> findProducts(final ProductFilter queryFilter) {
        Objects.requireNonNull(queryFilter, "Query filter cannot be null.");

        val filterParameters = queryFilter.buildQueryParams();
        val specificationResult = specificationHandler.handle(filterParameters);
        val pagedOrders = repository.findAll(specificationResult.specification(), specificationResult.pageable());
        if (queryFilter.shouldConvertPricesToDefault()) {
            // calculate prices if there are different currencies than default
            val isCurrencyConversionNeeded = pagedOrders.getContent()
                    .stream()
                    .anyMatch(product -> !product.getPrice().hasCurrency(priceConverter.getDefaultCurrency()));
            if (isCurrencyConversionNeeded) {
                val convertedProducts = priceConverter.convertPrices(pagedOrders.getContent())
                        .stream()
                        .collect(Collectors.toMap(Product::getId, Function.identity()));

                pagedOrders.getContent()
                        .forEach(product -> {
                            val convertedProduct = convertedProducts.get(product.getId());
                            if (Objects.isNull(convertedProduct)) {
                                return;
                            }
                            product.setPrice(convertedProduct.getPrice());
                        });
            }
        }
        return pagedOrders;
    }

}
