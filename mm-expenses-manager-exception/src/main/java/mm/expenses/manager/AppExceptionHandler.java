package mm.expenses.manager;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.exception.EmAppException;
import mm.expenses.manager.exception.EmCheckedException;
import mm.expenses.manager.exception.EmUncheckedException;
import mm.expenses.manager.exception.ExceptionMessage;
import mm.expenses.manager.exception.api.ApiBadRequestException;
import mm.expenses.manager.exception.api.ApiException;
import mm.expenses.manager.exception.api.ApiInternalErrorException;
import mm.expenses.manager.exception.api.ApiNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static mm.expenses.manager.exception.ExceptionMessage.*;

@Slf4j
@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

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

    @ExceptionHandler(EmCheckedException.class)
    ResponseEntity<ExceptionMessage> handleEmCheckedException(final EmCheckedException checkedException) {
        return messageEmAppException(checkedException);
    }

    @ExceptionHandler(EmUncheckedException.class)
    ResponseEntity<ExceptionMessage> handleEmUncheckedException(final EmUncheckedException uncheckedException) {
        return messageEmAppException(uncheckedException);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ExceptionMessage> handleException(final Exception internalError) {
        log.error("Unknown internal server error occurred: ", internalError);
        return message(internalError);
    }

    private ResponseEntity<ExceptionMessage> messageApiException(final ApiException exception) {
        return new ResponseEntity<>(fromApiException(exception), exception.httpStatus());
    }

    private ResponseEntity<ExceptionMessage> messageEmAppException(final EmAppException exception) {
        return new ResponseEntity<>(fromCustomException(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ExceptionMessage> message(final Exception exception) {
        return new ResponseEntity<>(fromException(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
