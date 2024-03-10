package mm.expenses.manager.order.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mm.expenses.manager.order.config.DefaultInstantDeserializer;
import mm.expenses.manager.order.currency.Price;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class OrderedProduct {

    private UUID id;

    @JsonDeserialize(using = DefaultInstantDeserializer.class)
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;

    @JsonDeserialize(using = DefaultInstantDeserializer.class)
    @JsonSerialize(using = InstantSerializer.class)
    private Instant lastModifiedAt;

    private Double quantity;

    private Price price;

    private Price priceSummary;

    public Price getPriceSummary() {
        return Objects.nonNull(price)
                ? Price.multiply(price, quantity)
                : Price.empty();
    }

}
