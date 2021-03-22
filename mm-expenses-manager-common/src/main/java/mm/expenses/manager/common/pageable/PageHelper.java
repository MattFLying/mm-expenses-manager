package mm.expenses.manager.common.pageable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.security.InvalidParameterException;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageHelper {

    public static Pageable getPageable(final Integer page, final Integer size) {
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size != 0)) {
            return PageRequest.of(page, size);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return PageRequest.of(0, Integer.MAX_VALUE);
        } else {
            throw new InvalidParameterException("Page number or size is invalid. Both values must be passed or none and size must not be 0. Page number: " + page + ", " + size);
        }
    }

}
