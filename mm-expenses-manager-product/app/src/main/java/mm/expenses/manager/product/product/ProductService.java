package mm.expenses.manager.product.product;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.beans.async.AsyncMessageProducer;
import mm.expenses.manager.common.kafka.AsyncKafkaOperation;
import mm.expenses.manager.product.ProductCommonValidation;
import mm.expenses.manager.product.api.product.model.CreateProductRequest;
import mm.expenses.manager.product.api.product.model.UpdateProductRequest;
import mm.expenses.manager.product.async.message.ProductManagementProducerMessage;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.exception.ProductNotFoundException;
import mm.expenses.manager.product.exception.ProductValidationException;
import mm.expenses.manager.product.price.PriceService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final AsyncMessageProducer producer;
    private final PriceService priceService;

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
                .orElseThrow(() -> new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(id)));

        final var newName = request.getName();
        if (Objects.nonNull(request.getName())) {
            if (ProductCommonValidation.isNameEmpty(newName)) {
                throw new ProductValidationException(ProductExceptionMessage.PRODUCT_NAME_NOT_VALID.withParameters(newName));
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
                            throw new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId));
                        });
    }

    public void delete(final Collection<UUID> productIds) {
        repository.deleteByIdIn(productIds);
    }

    public Product findById(final UUID productId, final boolean isDeleted) {
        return repository.findByIdAndIsDeleted(productId, isDeleted)
                .orElseThrow(() -> new ProductNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(productId)));
    }

    public Page<Product> findProducts(final ProductQueryFilter queryFilter, final PageRequest pageable) {
        final var filter = queryFilter.findFilter();
        return switch (filter) {
            case NAME -> repository.findByNameAndIsDeletedFalse(queryFilter.name(), pageable);
            case PRICE -> repository.findByPrice_valueAndIsDeletedFalse(queryFilter.price(), pageable);
            case NAME_PRICE ->
                    repository.findByNameAndPrice_valueAndIsDeletedFalse(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_LESS_THAN ->
                    repository.findByNameAndPrice_valueLessThanAndIsDeletedFalse(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_GREATER_THAN ->
                    repository.findByNameAndPrice_valueGreaterThanAndIsDeletedFalse(queryFilter.name(), queryFilter.price(), pageable);
            case NAME_PRICE_MIN_PRICE_MAX ->
                    repository.findByNameAndPrice_valueBetweenAndIsDeletedFalse(queryFilter.name(), queryFilter.priceMin(), queryFilter.priceMax(), pageable);
            case PRICE_LESS_THAN ->
                    repository.findByPrice_valueLessThanAndIsDeletedFalse(queryFilter.price(), pageable);
            case PRICE_GREATER_THAN ->
                    repository.findByPrice_valueGreaterThanAndIsDeletedFalse(queryFilter.price(), pageable);
            case PRICE_MIN_PRICE_MAX ->
                    repository.findByPrice_valueBetweenAndIsDeletedFalse(queryFilter.priceMin(), queryFilter.priceMax(), pageable);
            default -> repository.findAllByIsDeletedFalse(pageable);
        };
    }

    public Page<Product> findDeleted(final Pageable pageable) {
        return repository.findAllByIsDeletedTrue(pageable);
    }

    private Product saveProduct(final Product product, final AsyncKafkaOperation operation) {
        final var savedProduct = repository.save(product);
        producer.send(ProductManagementProducerMessage.of(savedProduct, operation));
        return savedProduct;
    }

}
