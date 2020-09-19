package mm.expenses.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.exception.ApiFeignClientException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class FeignClientErrorDecoder implements ErrorDecoder {

    private final ObjectMapper mapper;

    @Override
    public Exception decode(final String methodKey, final Response response) {
        final var responseBody = getResponseBody(response);
        return ApiFeignClientException.builder()
                .status(HttpStatus.resolve(response.status()))
                .headers(response.headers())
                .message(responseBody.map(ResponseContent::getContent).orElse("Unknown error occurred"))
                .methodKey(methodKey)
                .errorResponse(responseBody.map(ResponseContent::getExceptionMessage).orElse(null))
                .build();
    }

    private Optional<ResponseContent> getResponseBody(final Response response) {
        if (Objects.isNull(response.body())) {
            log.debug("Received an empty body response for request: {}", response.request());
            return Optional.empty();
        }
        try (final Reader reader = response.body().asReader()) {
            return Optional.of(getContentFromResponse(reader, response));
        } catch (final Exception e) {
            return Optional.empty();
        }
    }

    private ResponseContent getContentFromResponse(final Reader reader, final Response response) throws IOException {
        final var content = String.join(StringUtils.EMPTY, readLines(reader));
        if (isJSONValid(content)) {
            try {
                ExceptionMessage errorResponse = this.mapper.readValue(content, ExceptionMessage.class);
                return new ResponseContent(content, errorResponse);
            } catch (final JsonProcessingException exception) {
                log.debug("Received response content: {} is not a valid json", content);
                return ResponseContent.of(content, response);
            }
        }
        return new ResponseContent(content, null);
    }

    private static List<String> readLines(final Reader input) throws IOException {
        final var reader = new BufferedReader(input);
        final var list = new ArrayList<String>();
        var line = reader.readLine();
        while (Objects.nonNull(line)) {
            list.add(line);
            line = reader.readLine();
        }
        return list;
    }

    private boolean isJSONValid(final String input) {
        try {
            mapper.readTree(input);
            return true;
        } catch (IOException e) {
            log.debug("Received content is not a valid json: {}", input);
            return false;
        }
    }

    @Data
    @RequiredArgsConstructor
    private static class ResponseContent {
        private final String content;
        private final ExceptionMessage exceptionMessage;

        static ResponseContent of(final String content, final Response response) {
            return new ResponseContent(
                    content,
                    new ExceptionMessage(
                            "api-exception",
                            ExceptionMessage.formatStatus(HttpStatus.valueOf(response.status())),
                            response.reason(),
                            Instant.now()
                    )
            );
        }
    }

}
