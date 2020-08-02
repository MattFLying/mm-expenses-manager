package mm.expenses.manager;

import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApiNotFoundException.class)
    ResponseEntity<ExceptionMessage> handleNotFoundException(final ApiNotFoundException notFound) {
        return message(notFound);
    }

    @ExceptionHandler(ApiBadRequestException.class)
    ResponseEntity<ExceptionMessage> handleBadRequestException(final ApiBadRequestException badRequest) {
        return message(badRequest);
    }

    @ExceptionHandler(ApiInternalErrorException.class)
    ResponseEntity<ExceptionMessage> handleBadRequestException(final ApiInternalErrorException internalError) {
        log.error("Internal server error occurred: ", internalError);
        return message(internalError);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ExceptionMessage> handleBadRequestException(final Exception internalError) {
        log.error("Unknown internal server error occurred: ", internalError);
        return message(internalError);
    }

    private ResponseEntity<ExceptionMessage> message(final ApiException exception) {
        return new ResponseEntity<>(ExceptionMessage.fromApiException(exception), exception.httpStatus());
    }

    private ResponseEntity<ExceptionMessage> message(final Exception exception) {
        return new ResponseEntity<>(ExceptionMessage.fromException(exception), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
