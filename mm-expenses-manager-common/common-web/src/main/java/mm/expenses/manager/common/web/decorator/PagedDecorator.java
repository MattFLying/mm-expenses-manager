package mm.expenses.manager.common.web.decorator;

import mm.expenses.manager.common.utils.decorator.GenericDecorator;
import org.springframework.data.domain.Page;

/**
 * Generic decorator type to be implemented by specific decorator with its implementation of how to
 * decorate specific paged objects.
 *
 * @param <From> object that has to be decorated
 * @param <To>   object has to be decorated to this object type
 */
public abstract class PagedDecorator<From, To> extends GenericDecorator<From, To> {

    /**
     * @return Specific decorated paged objects
     */
    public abstract Page<To> decorate(final Page<From> from);

}
