package mm.expenses.manager.finance.exchangerate.trail;

import lombok.Getter;
import lombok.Setter;

public enum TrailOperation {
    EXCHANGE_RATES_HISTORY_UPDATE,
    LATEST_EXCHANGE_RATES_SYNCHRONIZATION,
    CREATE_OR_UPDATE;

    @Getter
    @Setter
    private State state;

    public TrailOperation withStatus(final State status) {
        this.state = status;
        return this;
    }

    public enum State {
        SUCCESS, ERROR
    }

}
