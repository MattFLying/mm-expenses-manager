package mm.expenses.manager.common.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Collection;
import java.util.Set;

/**
 * Common configuration for {@link ObjectMapper} to be shared between components.
 */
public final class ObjectMapperConfig {

    public static ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();

        objectMapper.registerModules(modules());

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return objectMapper;
    }

    private static Collection<com.fasterxml.jackson.databind.Module> modules() {
        return Set.of(new Jdk8Module(), new JavaTimeModule());
    }

}
