package mm.expenses.manager.common.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceType {
    NO_SPECIFIED(0),
    COMMON(1),
    EXCEPTION(2),
    VALIDATOR(3),
    FINANCE(4),
    ORDER(5);

    private static final String PARENT_NAME = "mm-expenses-manager";
    private final Integer id;

    public String getServiceName() {
        return this.equals(NO_SPECIFIED)
                ? String.format("%s".toLowerCase(), this)
                : String.format("%s-%s".toLowerCase(), PARENT_NAME, this);
    }

}
