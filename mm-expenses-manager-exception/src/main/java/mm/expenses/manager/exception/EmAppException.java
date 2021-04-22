package mm.expenses.manager.exception;

import java.io.Serializable;

/**
 * Interface for custom app exceptions.
 */
public interface EmAppException extends Serializable {

    /**
     * @return exception message
     */
    String getMessage();

    /**
     * @return exception type with available code and message
     */
    ExceptionType getType();

}
