package mm.expenses.manager.common.web.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.web.exception.handler.AppExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ErrorHandlingConfig {

    private final ObjectMapper objectMapper;

    @Bean
    AppExceptionHandler apiExceptionHandler() {
        return new AppExceptionHandler();
    }

    @Bean
    FeignClientErrorDecoder errorDecoder() {
        return new FeignClientErrorDecoder(errorDecoderObjectMapper());
    }

    private ObjectMapper errorDecoderObjectMapper() {
        return objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

}
