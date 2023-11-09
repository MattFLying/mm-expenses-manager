package mm.expenses.manager.order.order.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderExceptionCode {
    NOT_FOUND("order-not-found"),
    NEW_ORDER_VALIDATION("order-new-validation"),
    UPDATE_ORDER_VALIDATION("order-update-validation");

    private final String code;

}
