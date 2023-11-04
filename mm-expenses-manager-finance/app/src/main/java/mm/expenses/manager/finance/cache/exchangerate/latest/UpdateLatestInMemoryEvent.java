package mm.expenses.manager.finance.cache.exchangerate.latest;

import org.springframework.context.ApplicationEvent;

/**
 * Event published when latest rates in memory should be updated.
 */
public class UpdateLatestInMemoryEvent extends ApplicationEvent {
    public UpdateLatestInMemoryEvent(final Object source) {
        super(source);
    }
}
