package mm.expenses.manager.common.utils.decorator;

/**
 * Generic decorator type to be implemented by specific decorator with its implementation of how to
 * decorate specific object.
 *
 * @param <From> object that has to be decorated
 * @param <To>   object has to be decorated to this object type
 */
public abstract class GenericDecorator<From, To> {

    /**
     * @return Specific decorated object
     */
    public abstract To decorate(final From from);

}
