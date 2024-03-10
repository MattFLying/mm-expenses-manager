package mm.expenses.manager.common.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.web.api.WebApi;
import mm.expenses.manager.common.web.api.WebHttpMethod;
import mm.expenses.manager.common.web.exception.ApiException;
import mm.expenses.manager.common.web.exception.ApiInternalErrorException;
import mm.expenses.manager.common.web.exception.ApiMethodNotAllowed;
import mm.expenses.manager.common.web.exception.WebExceptionMessage;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * API interceptor for every Web Api calls.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class WebInterceptor {

    private final HttpServletRequest request;

    /**
     * Process specific request.
     *
     * @param requestProcessor - action to be processed for current request
     * @param context          - current request's context
     * @return {@link ResponseEntity} of current request
     */
    public ResponseEntity processRequest(final RequestProcessor requestProcessor, final WebContext context) {
        validateProcessRequestInput(requestProcessor, context);

        final var requestPath = context.getWebApi();
        final var method = WebHttpMethod.of(request.getMethod());
        validateHttpMethod(requestPath, method);
        validateParameters(request, context);
        validateHeaders(request, context);

        log.debug("Web API call to: {} {}. ApiContext: {}", method.name(), request.getRequestURI(), context);
        try {
            final var result = requestProcessor.process(context);
            log.debug("Result of {} {} and ApiContext: {} is: {}", method.name(), request.getRequestURI(), context, result);

            return switch (method) {
                case GET, POST, PUT, PATCH -> Objects.nonNull(result)
                        ? ResponseEntity.status(requestPath.getExpectedSuccessfulStatus()).body(result)
                        : ResponseEntity.status(requestPath.getExpectedSuccessfulStatus()).contentType(context.contentType()).build();

                case DELETE -> ResponseEntity.status(HttpStatus.NO_CONTENT).build();

                // For any not handled Http methods defined in WebHttpMethod
                default -> ResponseEntity.ofNullable(null);
            };
        } catch (final ApiException exception) {
            log.debug("Error occurred during API call to: {} {}", method.name(), request.getRequestURI(), exception);
            throw exception;
        } catch (final Exception exception) {
            log.debug("Unknown error occurred during API call to: {} {}", method.name(), request.getRequestURI(), exception);
            throw exception;
        }
    }

    /**
     * Validates if {@link RequestProcessor} and {@link WebContext} are not nulls.
     *
     * @param requestProcessor - action to be processed for current request
     * @param context          - current request's context
     */
    private void validateProcessRequestInput(final RequestProcessor requestProcessor, final WebContext context) {
        var isRequestProcessorNull = Objects.isNull(requestProcessor);
        var isContextNull = Objects.isNull(context);

        var message = StringUtils.EMPTY;
        if (isRequestProcessorNull) {
            message = String.format("%sRequest Processor is null. ", message);
        }
        if (isContextNull) {
            message = String.format("%sContext is null.", message);
        }

        if (isRequestProcessorNull || isContextNull) {
            log.debug("{}. Request Processor: {}, Context: {}", message.trim(), requestProcessor, context);
            throw new ApiInternalErrorException(WebExceptionMessage.REQUIRED_REQUEST_PROCESSOR_AND_CONTEXT.withParameters(message.trim()));
        }
    }

    /**
     * Validates if current request Http method is equal to expected Http method for called endpoint.
     *
     * @param requestPath - expected url web path
     * @param method      - http method taken directly from {@link HttpServletRequest} for current request
     */
    private void validateHttpMethod(final WebApi requestPath, final WebHttpMethod method) {
        if (!requestPath.getMethod().equals(method.getHttpMethod())) {
            throw new ApiMethodNotAllowed(WebExceptionMessage.METHOD_NOT_ALLOWED.withParameters(String.format("%s %s", method.name(), requestPath.getFullPathUrl())));
        }
    }

    /**
     * Validates if there are any url parameters and set them in current web context.
     *
     * @param request - current {@link HttpServletRequest} request
     * @param context - current request's context
     */
    private void validateParameters(final HttpServletRequest request, final WebContext context) {
        final var parameters = request.getParameterMap();
        if (MapUtils.isNotEmpty(parameters)) {
            final var webParameters = new WebUrlParameters(parameters);
            context.parameters(webParameters);
        }
    }

    /**
     * Validates if there are any headers and set them in current web context.
     *
     * @param request - current {@link HttpServletRequest} request
     * @param context - current request's context
     */
    private void validateHeaders(final HttpServletRequest request, final WebContext context) {
        final var headers = request.getHeaderNames();
        if (Objects.nonNull(headers)) {
            context.headersMap(
                    Collections.list(headers)
                            .stream()
                            .collect(Collectors.toMap(header -> header, request::getHeader))
            );
        }
    }

}
