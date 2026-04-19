package mm.expenses.manager.product.processor.find;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.api.ApiConflictException;
import mm.expenses.manager.common.exceptions.api.ApiException;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.common.utils.chain.ChainCommandExecution;
import mm.expenses.manager.product.exception.ProductExceptionMessage;
import mm.expenses.manager.product.processor.Product;
import mm.expenses.manager.product.processor.ProductRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Step in chain of find single {@link Product}.
 * Finds requested product by its id.
 */
@Slf4j
@RequiredArgsConstructor
final class FindProductById extends FindProductByIdChain {

    private final ProductRepository repository;
    private final Boolean isDeleted;

    @Override
    public void setNextHandler(final ChainCommandExecution<UUID, Product> nextStep) {
        super.setNextHandler(null);
    }

    @Override
    public Product handleRequest(final UUID id) {
        try {
            final var isDeletedFlag = Objects.nonNull(isDeleted) ? isDeleted : false;
            final var found = repository.findByIdAndIsDeleted(id, isDeletedFlag)
                    .orElseThrow(() -> new ApiNotFoundException(ProductExceptionMessage.PRODUCT_NOT_FOUND.withParameters(id)));

            if (hasNext()) {
                // This step should not ever happen but if there is any possibility to do anything after save in database
                // then there is a proper place to do it.
                return next.handleRequest(id);
            } else {
                return found;
            }
        } catch (final ApiException exception) {
            throw exception;
        } catch (final Exception exception) {
            log.error("Unknown find single product error occurred.", exception);
            throw new ApiConflictException(ProductExceptionMessage.PRODUCT_CANNOT_BE_FOUND.withParameters(id), exception);
        }
    }

}
