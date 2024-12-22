package mm.expenses.manager.order.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.*;
import mm.expenses.manager.order.config.DefaultInstantDeserializer;
import mm.expenses.manager.order.currency.Prices;

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

    private Prices price;

    private Prices priceSummary;

    public Prices getPriceSummary() {
        return Objects.nonNull(price)
                ? Prices.multiply(price, quantity)
                : new Prices();
    }

}
