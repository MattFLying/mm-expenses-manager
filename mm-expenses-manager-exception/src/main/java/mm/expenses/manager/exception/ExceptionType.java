package mm.expenses.manager.exception;

import java.io.Serializable;

/**
 * Interface represents available exception codes and message in specific services.
 */
public interface ExceptionType extends Serializable {

    /**
     * @return specific exception code
     */
    String getCode();

    /**
     * @return specific exception message
     */
    String getMessage();

    /**
     * If exception message requires some parameters to prepare whole message then this methods handle them
     * to be available to use in exceptino message.
     */
    ExceptionType withParameters(final Object... params);

}
