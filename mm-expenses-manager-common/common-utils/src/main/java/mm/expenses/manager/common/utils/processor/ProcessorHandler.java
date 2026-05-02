package mm.expenses.manager.common.utils.processor;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Abstraction for processors handlers.
 */
@Slf4j
public abstract class ProcessorHandler {

    /**
     * Handler type
     */
    public abstract ProcessorType getType();

    /**
     * Handles specific request to processed with specific response expected.
     */
    public abstract Response handle(final Request request);

    /**
     * Decorates handled response.
     */
    public abstract Response handleDecorated(final Request request);

    /**
     * Request representation for processor handler.
     */
    @Getter
    @SuperBuilder
    public static class Request {

        private UUID id;

        private Object request;

    }

    /**
     * Response representation for processor handler.
     */
    @Getter
    @SuperBuilder
    public static class Response {

        private Object response;

        private Object decoratedResponse;

        /**
         * Maps default response object to specific type.
         *
         * @param clazz class type to be cast
         * @return response of specific type
         */
        public <T> T mapResponse(final Class<T> clazz) {
            return cast(clazz, response);
        }

        /**
         * Maps default decorated response object to specific type.
         *
         * @param clazz class type to be cast
         * @return decorated response of specific type
         */
        public <T> T mapDecoratedResponse(final Class<T> clazz) {
            return cast(clazz, decoratedResponse);
        }

        /**
         * Maps specific object to specific type.
         *
         * @param clazz  class type to be cast
         * @param object object that has to be cast
         * @return desired object type
         */
        protected <T> T cast(final Class<T> clazz, final Object object) {
            try {
                return clazz.cast(object);
            } catch (final ClassCastException exception) {
                log.error("Cannot cast=[{}] to=[{}] type.", object, clazz, exception);
                throw exception;
            }
        }

    }

    /**
     * Default processor handler exception.
     */
    public static class ProcessorHandlerException extends RuntimeException {

        public ProcessorHandlerException(final String message) {
            super(message);
        }

    }

}
