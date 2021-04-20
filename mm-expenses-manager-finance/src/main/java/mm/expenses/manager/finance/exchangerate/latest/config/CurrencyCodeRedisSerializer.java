package mm.expenses.manager.finance.exchangerate.latest.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import mm.expenses.manager.common.i18n.CurrencyCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Objects;

@Generated
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.configuration", name = "latest-rates-cache-type", havingValue = "redis")
class CurrencyCodeRedisSerializer implements RedisSerializer<CurrencyCode> {

    private final ObjectMapper objectMapper;

    @Override
    public byte[] serialize(final CurrencyCode currencyCode) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(currencyCode);
        } catch (final JsonProcessingException exception) {
            throw new SerializationException(exception.getMessage(), exception);
        }
    }

    @Override
    public CurrencyCode deserialize(final byte[] bytes) throws SerializationException {
        try {
            if (Objects.isNull(bytes)) {
                return CurrencyCode.UNDEFINED;
            }
            return objectMapper.readValue(bytes, CurrencyCode.class);
        } catch (final Exception exception) {
            throw new SerializationException(exception.getMessage(), exception);
        }
    }

    @Override
    public Class<?> getTargetType() {
        return CurrencyCode.class;
    }

}
