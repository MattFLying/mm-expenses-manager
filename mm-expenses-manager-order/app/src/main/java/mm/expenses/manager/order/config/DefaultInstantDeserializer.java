package mm.expenses.manager.order.config;

import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Default {@link Instant} deserializer.
 */
public class DefaultInstantDeserializer extends InstantDeserializer<Instant> {

    public DefaultInstantDeserializer() {
        super(
                Instant.class, DateTimeFormatter.ISO_INSTANT,
                Instant::from,
                fromIntegerArguments -> Instant.ofEpochMilli(fromIntegerArguments.value),
                fromDecimalArguments -> Instant.ofEpochSecond(fromDecimalArguments.integer, fromDecimalArguments.fraction),
                null,
                true // yes, replace zero offset with Z
        );
    }

}
