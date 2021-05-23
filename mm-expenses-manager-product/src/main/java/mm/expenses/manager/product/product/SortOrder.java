package mm.expenses.manager.product.product;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
enum SortOrder {
    NAME(List.of("name")),
    PRICE_VALUE(List.of("price.value")),
    PRICE_CURRENCY(List.of("price.currency"));

    private static final Direction DEFAULT_DIRECTION = Direction.ASC;
    private final Collection<String> properties;
    private Direction direction;

    /**
     * Set ordering direction to descending or ascending will be used as default.
     *
     * @param isDescending should be descending or not
     * @return ordering with direction
     */
    public SortOrder withDirectionAscOrDesc(final Boolean isDescending) {
        this.direction = Optional.ofNullable(isDescending).filter(iscDesc -> iscDesc).map(asc -> Direction.DESC).orElse(DEFAULT_DIRECTION);
        return this;
    }

    public List<Order> getOrders() {
        return CollectionUtils.emptyIfNull(properties).stream()
                .map(property -> Order.by(property).with(Objects.nonNull(direction) ? direction : DEFAULT_DIRECTION))
                .collect(Collectors.toList());
    }

}
