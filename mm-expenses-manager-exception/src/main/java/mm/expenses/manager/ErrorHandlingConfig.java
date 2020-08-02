package mm.expenses.manager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ErrorHandlingConfig {

    private final ObjectMapper objectMapper;

    @Bean
    ApiExceptionHandler apiExceptionHandler() {
        return new ApiExceptionHandler();
    }

    @Bean
    FeignClientErrorDecoder errorDecoder() {
        return new FeignClientErrorDecoder(errorDecoderObjectMapper());
    }

    private ObjectMapper errorDecoderObjectMapper() {
        return objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

}
