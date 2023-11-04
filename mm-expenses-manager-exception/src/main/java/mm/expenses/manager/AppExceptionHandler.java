package mm.expenses.manager;

import jakarta.validation.Path;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.exception.EmAppException;
import mm.expenses.manager.exception.EmCheckedException;
import mm.expenses.manager.exception.EmUncheckedException;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.exception.api.*;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
    ResponseEntity<ExceptionMessage> handleValidationException(RuntimeException exception) {
        if (exception instanceof jakarta.validation.ConstraintViolationException violationException) {
            return messageValidationException(new ValidationException(ValidationExceptionMessage.VALIDATION_EXCEPTION, violationException));
        }
        return messageValidationException(exception);
    }

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
        final var badRequest = HttpStatus.BAD_REQUEST;
        if (exception instanceof mm.expenses.manager.exception.api.ValidationException validationException) {
            if (validationException.hasCause() && validationException.getValidationCause() instanceof jakarta.validation.ConstraintViolationException constraintException) {
                final var message = constraintException.getConstraintViolations()
                        .stream()
                        .map(jakarta.validation.ConstraintViolation::getMessageTemplate)
                        .collect(Collectors.joining(" "));
                final var code = constraintException.getConstraintViolations()
                        .stream()
                        .map(jakarta.validation.ConstraintViolation::getPropertyPath)
                        .findAny()
                        .map(path -> extractValidationCode(path, badRequest))
                        .orElse(badRequest.getReasonPhrase());

                return new ResponseEntity<>(of(code, message, badRequest), badRequest);
            }
        }
        return new ResponseEntity<>(fromException(exception), badRequest);
    }

    private String extractValidationCode(Path path, HttpStatus statusAsDefault) {
        if (path instanceof PathImpl pathImpl) {
            return pathImpl.getLeafNode().getName();
        }
        return statusAsDefault.getReasonPhrase();
    }

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
