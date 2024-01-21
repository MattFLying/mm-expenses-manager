package mm.expenses.manager.common.beans.pagination.sort;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.NullHandling;

/**
 * Sorting order object to be used for JsonB objects in JPA.
 */
public class JsonBSortOrder extends Sort.Order {

    private JsonBSortOrder(final String property, final Direction direction) {
        super(direction, property);
    }

    public static JsonBSortOrder by(final String property, final Direction direction) {
        return new JsonBSortOrder(property, direction);
    }

    @Override
    public String toString() {
        var result = String.format("%s %s", getProperty(), getDirection());
        if (!NullHandling.NATIVE.equals(getNullHandling())) {
            result += ", " + getNullHandling();
        }
        if (isIgnoreCase()) {
            result += ", ignoring case";
        }
        return result;
    }


}
