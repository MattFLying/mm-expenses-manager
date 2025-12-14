package mm.expenses.manager.validator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import mm.expenses.manager.common.utils.wrapper.BigDecimalWrapper;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Serializes price value to common format.
 */
public class PriceValueSerializer extends JsonSerializer<BigDecimal> {

    @Override
    public void serialize(final BigDecimal value, final JsonGenerator generator, final SerializerProvider provider) throws IOException, JsonProcessingException {
        generator.writeNumber(BigDecimalWrapper.of(value));
    }

}
