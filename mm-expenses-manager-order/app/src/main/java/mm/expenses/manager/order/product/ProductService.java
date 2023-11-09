package mm.expenses.manager.order.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.order.api.product.model.CreateNewProductRequest;
import mm.expenses.manager.order.api.product.model.UpdateProductRequest;
import mm.expenses.manager.order.product.exception.ProductCreationException;
import mm.expenses.manager.order.product.exception.ProductNotFoundException;
import mm.expenses.manager.order.product.exception.ProductUpdateException;
import mm.expenses.manager.order.product.exception.ProductValidationException;
import mm.expenses.manager.order.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private final ProductValidator validator;
    private final ProductMapper mapper;

    public Optional<Product> findById(final String id) {
        log.info("Looking for product of id: {}", id);
        final var found = repository.findById(id);
        if (found.isEmpty()) {
            log.info("Product of id: {} not found", id);
        } else {
            log.info("Product of id: {} found", id);
        }
        return found.map(mapper::map);
    }

    public List<Product> findAllByIds(final Set<String> ids) {
        log.info("Looking for products of given ids: {}", ids);
        final var products = repository.findByIdIn(ids);
        log.info("Found: {} products", products.size());
        return products.stream().map(mapper::map).collect(Collectors.toList());
    }

    Page<Product> findAll(final Pageable pageable) {
        log.info("Looking for all products");
        final var products = repository.findAll(pageable);
        log.info("Found: {} products", products.getTotalElements());
        return new PageImpl<>(products.stream().map(mapper::map).collect(Collectors.toList()), pageable, products.getTotalElements());
    }

    Page<Product> findByPriceRange(final BigDecimal priceMin, final BigDecimal priceMax, final Pageable pageable) {
        log.info("Looking for all products in price range: {} - {}", priceMin, priceMax);
        final var products = repository.findByPrice_amountBetween(priceMin.doubleValue(), priceMax.doubleValue(), pageable);
        log.info("Found: {} products in price range: {} - {}", products.getTotalElements(), priceMin, priceMax);
        return new PageImpl<>(products.stream().map(mapper::map).collect(Collectors.toList()), pageable, products.getTotalElements());
    }

    Page<Product> findByPriceGreater(final BigDecimal price, final Pageable pageable) {
        log.info("Looking for all products with price greater than: {}", price);
        final var products = repository.findByPrice_amountGreaterThan(price.doubleValue(), pageable);
        log.info("Found: {} products with price greater than: {}", products.getTotalElements(), price);
        return new PageImpl<>(products.stream().map(mapper::map).collect(Collectors.toList()), pageable, products.getTotalElements());
    }

    Page<Product> findByPriceLess(final BigDecimal price, final Pageable pageable) {
        log.info("Looking for all products with price less than: {}", price);
        final var products = repository.findByPrice_amountLessThan(price.doubleValue(), pageable);
        log.info("Found: {} products with price less than: {}", products.getTotalElements(), price);
        return new PageImpl<>(products.stream().map(mapper::map).collect(Collectors.toList()), pageable, products.getTotalElements());
    }

    Page<Product> findByName(final String name, final Pageable pageable) {
        log.info("Looking for all products with name pattern: {}", name);
        final var products = repository.findByName(name, pageable);
        log.info("Found: {} products with name pattern: {}", products.getTotalElements(), name);
        return new PageImpl<>(products.stream().map(mapper::map).collect(Collectors.toList()), pageable, products.getTotalElements());
    }

    Optional<Product> create(final CreateNewProductRequest newProduct) {
        log.info("Creating a new product");
        try {
            validator.checkIfObjectIsValid(validator.validateNew(newProduct), ProductValidationException.class);

            final var preparedData = mapper.map(newProduct, mapper.createInstantNow());
            final var saved = repository.save(mapper.map(preparedData));
            log.info("New product created with id: {}", saved.getId());

            return Optional.of(saved).map(mapper::map);
        } catch (final ProductValidationException exception) {
            log.warn("Product cannot be created because of validation failures: {}", exception.getMessage(), exception);
            throw new ProductCreationException("Product cannot be created because of validation failures: " + exception.getMessage(), exception);
        }
    }

    Optional<Product> update(final String id, final UpdateProductRequest updateProduct) {
        log.info("Updating product of id: {}", id);
        try {
            final var existedObject = repository.findById(id);
            if (existedObject.isEmpty()) {
                log.error("Product of id: {} does not exists and cannot be updated", id);
                throw new ProductNotFoundException("Object of id: " + id + " does not exists and cannot be updated.");
            }
            validator.checkIfObjectIsValid(validator.validateUpdate(updateProduct), ProductValidationException.class);

            final var existedEntity = existedObject.get();
            final var preparedData = mapper.map(updateProduct, existedEntity);
            final var saved = repository.save(mapper.map(preparedData));
            log.info("Product of id: {} has been updated", saved.getId());

            return Optional.of(saved).map(mapper::map);
        } catch (final ProductValidationException exception) {
            log.warn("Product of id: {} cannot be updated because of validation failures: {}", id, exception.getMessage(), exception);
            throw new ProductUpdateException("Product of id: " + id + " cannot be updated because of validation failures: " + exception.getMessage(), exception);
        }
    }

    void remove(final String id) {
        log.info("Deleting product of id: {}", id);
        if (!repository.existsById(id)) {
            log.info("Product of id: {} does not exists and cannot be removed", id);
            throw new ProductNotFoundException("Object of id: " + id + " does not exists and cannot be removed");
        }
        repository.deleteById(id);
        log.info("Product of id: {} has been removed", id);
    }

    void removeByIds(final Set<String> ids) {
        log.info("Deleting products of ids: {}", ids);
        final long removedCount = repository.deleteByIdIn(ids);
        log.info("{} products were removed", removedCount);
    }

}
