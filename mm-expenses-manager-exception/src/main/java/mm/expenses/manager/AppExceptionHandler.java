package mm.expenses.manager;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.exception.EmAppException;
import mm.expenses.manager.exception.EmCheckedException;
import mm.expenses.manager.exception.EmUncheckedException;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.exception.api.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.stream.Collectors;

import static mm.expenses.manager.exception.ExceptionMessage.fromApiException;
import static mm.expenses.manager.exception.ExceptionMessage.fromCustomException;
import static mm.expenses.manager.exception.ExceptionMessage.fromException;
import static mm.expenses.manager.exception.ExceptionMessage.of;

@Slf4j
@ControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(ApiBadRequestException.class)
    ResponseEntity<ExceptionMessage> handleBadRequestException(final ApiBadRequestException badRequest) {
        return messageApiException(badRequest);
    }

    @ExceptionHandler(ApiInternalErrorException.class)
    ResponseEntity<ExceptionMessage> handleInternalErrorException(final ApiInternalErrorException internalError) {
        log.error("Internal server error occurred: ", internalError);
        return messageApiException(internalError);
    }

    @ExceptionHandler(ApiNotFoundException.class)
    ResponseEntity<ExceptionMessage> handleNotFoundException(final ApiNotFoundException notFound) {
        return messageApiException(notFound);
    }

    @ExceptionHandler(ApiConflictException.class)
    ResponseEntity<ExceptionMessage> handleConflictException(final ApiConflictException conflict) {
        return messageApiException(conflict);
    }

    @ExceptionHandler(EmCheckedException.class)
    ResponseEntity<ExceptionMessage> handleEmCheckedException(final EmCheckedException checkedException) {
        return messageEmAppException(checkedException);
    }

    @ExceptionHandler(EmUncheckedException.class)
    ResponseEntity<ExceptionMessage> handleEmUncheckedException(final EmUncheckedException uncheckedException) {
        return messageEmAppException(uncheckedException);
    }

    @ExceptionHandler({ValidationException.class, jakarta.validation.ValidationException.class})
    ResponseEntity<ExceptionMessage> handleValidationException(RuntimeException validationException) {
        if (validationException instanceof ConstraintViolationException) {
            var vE = new mm.expenses.manager.exception.api.ValidationException(ValidationExceptionMessage.VALIDATION_EXCEPTION, (ValidationException) validationException);

            return messageValidationException(vE);
            //return messageValidationException((ValidationException) validationException);
        }
        if (validationException instanceof jakarta.validation.ConstraintViolationException) {
            var vE = new mm.expenses.manager.exception.api.ValidationException(ValidationExceptionMessage.VALIDATION_EXCEPTION, (jakarta.validation.ValidationException) validationException);

            return messageValidationException(vE);
            //return messageValidationException((jakarta.validation.ValidationException) validationException);
        }
        return messageValidationException(validationException);
    }

    /*@ExceptionHandler(ValidationException.class)
    ResponseEntity<ExceptionMessage> handleValidationException(final ValidationException validationException) {
        return messageValidationException(validationException);
    }

    @ExceptionHandler(jakarta.validation.ValidationException.class)
    ResponseEntity<ExceptionMessage> handleJakartaValidationException(final jakarta.validation.ValidationException validationException) {
        return messageValidationException(validationException);
    }*/

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ExceptionMessage> handleMethodArgumentNotValidException(final MethodArgumentNotValidException methodArgumentNotValidException) {
        return messageMethodArgumentNotValidException(methodArgumentNotValidException);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ExceptionMessage> handleException(final Exception internalError) {
        log.error("Unknown internal server error occurred: ", internalError);
        return message(internalError);
    }

    private ResponseEntity<ExceptionMessage> messageApiException(final ApiException exception) {
        return new ResponseEntity<>(fromApiException(exception), exception.httpStatus());
    }

    private ResponseEntity<ExceptionMessage> messageValidationException(final RuntimeException exception) {
        if (exception instanceof mm.expenses.manager.exception.api.ValidationException e) {
            if (e.getValidationCause() instanceof ConstraintViolationException) {
                final var constraintException = (ConstraintViolationException) exception.getCause();
                final var message = constraintException.getConstraintViolations()
                        .stream()
                        .map(ConstraintViolation::getMessageTemplate)
                        .collect(Collectors.joining(" "));

                return new ResponseEntity<>(of(message, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            }
            if (e.getValidationCause() instanceof jakarta.validation.ConstraintViolationException) {
                final var constraintException = (jakarta.validation.ConstraintViolationException) exception.getCause();
                final var message = constraintException.getConstraintViolations()
                        .stream()
                        .map(jakarta.validation.ConstraintViolation::getMessageTemplate)
                        .collect(Collectors.joining(" "));

                return new ResponseEntity<>(of(message, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            }






            /*if (exception.getCause() instanceof ConstraintViolationException) {
                final var constraintException = (ConstraintViolationException) exception.getCause();
                final var message = constraintException.getConstraintViolations()
                        .stream()
                        .map(ConstraintViolation::getMessageTemplate)
                        .collect(Collectors.joining(" "));

                return new ResponseEntity<>(of(message, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            }
            if (exception.getCause() instanceof jakarta.validation.ConstraintViolationException) {
                final var constraintException = (jakarta.validation.ConstraintViolationException) exception.getCause();
                final var message = constraintException.getConstraintViolations()
                        .stream()
                        .map(jakarta.validation.ConstraintViolation::getMessageTemplate)
                        .collect(Collectors.joining(" "));

                return new ResponseEntity<>(of(message, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            }*/
        }




        /*if (exception instanceof ConstraintViolationException) {
            final var constraintException = (ConstraintViolationException) exception;
            final var message = constraintException.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessageTemplate)
                    .collect(Collectors.joining(" "));

            return new ResponseEntity<>(of(message, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
        if (exception instanceof jakarta.validation.ConstraintViolationException) {
            final var constraintException = (jakarta.validation.ConstraintViolationException) exception;
            final var message = constraintException.getConstraintViolations()
                    .stream()
                    .map(jakarta.validation.ConstraintViolation::getMessageTemplate)
                    .collect(Collectors.joining(" "));

            return new ResponseEntity<>(of(message, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }*/
        return new ResponseEntity<>(fromException(exception), HttpStatus.BAD_REQUEST);
    }

    /*private ResponseEntity<ExceptionMessage> messageValidationException(final ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            final var constraintException = (ConstraintViolationException) exception;
            final var message = constraintException.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessageTemplate)
                    .collect(Collectors.joining(" "));

            return new ResponseEntity<>(of(message, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(fromException(exception), HttpStatus.BAD_REQUEST);
    }*/

    private ResponseEntity<ExceptionMessage> messageMethodArgumentNotValidException(final MethodArgumentNotValidException methodArgumentNotValidException) {
        final var message = methodArgumentNotValidException.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(" "));
        return new ResponseEntity<>(of(message, HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ExceptionMessage> messageEmAppException(final EmAppException exception) {
        return new ResponseEntity<>(fromCustomException(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ExceptionMessage> message(final Exception exception) {
        return new ResponseEntity<>(fromException(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
