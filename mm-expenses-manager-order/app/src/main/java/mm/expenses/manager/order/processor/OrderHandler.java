package mm.expenses.manager.order.processor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import mm.expenses.manager.common.utils.processor.ProcessorHandler;
import mm.expenses.manager.common.utils.processor.ProcessorType;
import mm.expenses.manager.order.core.Order;
import mm.expenses.manager.order.core.OrderMapper;
import mm.expenses.manager.order.core.OrderRepository;
import mm.expenses.manager.order.api.order.model.OrderPage;
import mm.expenses.manager.order.price.PriceConverter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Objects;

/**
 * Default handler definition to process specific handlers.
 */
@RequiredArgsConstructor
public abstract class OrderHandler extends ProcessorHandler {

    protected final OrderRepository repository;
    protected final OrderMapper mapper;
    protected final PriceConverter priceConverter;

    @SuperBuilder
    public static class Request extends ProcessorHandler.Request {

        private Boolean isDeleted;

        private Boolean shouldConvertCurrency;

        public boolean isShouldConvertCurrency() {
            return Objects.nonNull(shouldConvertCurrency) && shouldConvertCurrency;
        }

        public boolean isDeleted() {
            return Objects.nonNull(isDeleted) && isDeleted;
        }

    }

    @Getter
    @SuperBuilder
    public static class Response extends ProcessorHandler.Response {

        private Page<Order> pagedResponse;

        private List<Order> listedResponse;

        private OrderPage decoratedPagedResponse;

    }

    /**
     * Possible handler types to be implemented and to be used.
     */
    public enum Type implements ProcessorType {
        CREATE_ORDER,
        UPDATE_ORDER,
        DELETE_SINGLE_ORDER,
        DELETE_MANY_ORDERS,
        FIND_ORDER,
        SEARCH_ORDERS
    }

}
