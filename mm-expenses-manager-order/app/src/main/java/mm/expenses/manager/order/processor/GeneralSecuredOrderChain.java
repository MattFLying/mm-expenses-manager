package mm.expenses.manager.order.processor;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.exceptions.api.ApiNotFoundException;
import mm.expenses.manager.common.utils.security.Permission;
import mm.expenses.manager.common.web.chain.SecuredChainCommandExecution;
import mm.expenses.manager.common.web.exception.security.ApiForbiddenException;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.exception.OrderExceptionMessage;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * General secured order's chain to be used by chain commands that requires some common security actions.
 */
@RequiredArgsConstructor
public abstract class GeneralSecuredOrderChain<Request, Response> extends SecuredChainCommandExecution<Request, Response> {

    protected final OrderRepository repository;

    protected Permission fullPermission;
    protected Permission ownPermission;

    protected Order findOrderById(final UUID id, final boolean isDeletedFlag) {
        if (isSecured()) {
            final var permissions = getPermissions();
            if (permissions.contains(fullPermission)) {
                return findById(id, isDeletedFlag);
            }
            if (permissions.contains(ownPermission)) {
                final var createdBy = getUserId();
                return repository.findByIdAndCreatedByAndIsDeleted(id, createdBy, isDeletedFlag).orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id)));
            }
            throw new ApiForbiddenException(OrderExceptionMessage.ORDER_NO_PERMISSION);
        }
        return findById(id, isDeletedFlag);
    }

    protected List<Order> findOrdersByIds(final Set<UUID> ids, final boolean isDeletedFlag) {
        if (isSecured()) {
            final var permissions = getPermissions();
            if (permissions.contains(Permission.deleteOrder)) {
                return findByIds(ids, isDeletedFlag);
            }
            if (permissions.contains(Permission.deleteOwnOrder)) {
                final var createdBy = getUserId();
                return repository.findAllByIdInAndCreatedByAndIsDeleted(ids, createdBy, isDeletedFlag);
            }
            throw new ApiForbiddenException(OrderExceptionMessage.ORDER_NO_PERMISSION);
        }
        return findByIds(ids, isDeletedFlag);
    }

    private Order findById(final UUID id, final boolean isDeletedFlag) {
        return repository.findByIdAndIsDeleted(id, isDeletedFlag).orElseThrow(() -> new ApiNotFoundException(OrderExceptionMessage.ORDER_NOT_FOUND.withParameters(id)));
    }

    private List<Order> findByIds(final Set<UUID> ids, final boolean isDeletedFlag) {
        return repository.findAllByIdInAndIsDeleted(ids, isDeletedFlag);
    }

}
