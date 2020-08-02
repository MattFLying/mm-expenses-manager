package mm.expenses.manager.exception;

import com.netflix.hystrix.exception.ExceptionNotWrappedByHystrix;
import lombok.Builder;
import lombok.Data;
import mm.expenses.manager.ExceptionMessage;
import org.springframework.http.HttpStatus;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
public class ApiFeignClientException extends Exception implements ExceptionNotWrappedByHystrix {

    private final String message;
    private final String methodKey;
    private final HttpStatus status;
    private final Map<String, Collection<String>> headers;
    private final ExceptionMessage errorResponse;

    public Optional<ExceptionMessage> getErrorResponse() {
        return Optional.ofNullable(errorResponse);
    }

}
