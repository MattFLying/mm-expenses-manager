package mm.expenses.manager.common.pageable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.InvalidParameterException;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageHelper {

    public static Pageable getPageable(final Integer page, final Integer size) {
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size != 0)) {
            return pageRequestOf(page, size);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return pageRequestOf(0, Integer.MAX_VALUE);
        } else {
            throw new InvalidParameterException("Page number or size is invalid. Both values must be passed or none and size must not be 0. Page number: " + page + ", size: " + size);
        }
    }

    public static PageRequest getPageRequest(final Integer page, final Integer size) {
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size != 0)) {
            return pageRequestOf(page, size);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return pageRequestOf(0, Integer.MAX_VALUE);
        } else {
            throw new InvalidParameterException("Page number or size is invalid. Both values must be passed or none and size must not be 0. Page number: " + page + ", size: " + size);
        }
    }

    public static PageRequest getPageRequest(final Integer page, final Integer size, final Sort sort) {
        if (Objects.isNull(sort)) {
            return getPageRequest(page, size);
        }
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size != 0)) {
            return PageRequest.of(page, size, sort);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return PageRequest.of(0, Integer.MAX_VALUE, sort);
        } else {
            throw new InvalidParameterException("Page number or size or sort is invalid. All values must be passed or none and size must not be 0. Page number: " + page + ", size: " + size + ", sort: " + sort);
        }
    }

    private static PageRequest pageRequestOf(final int number, final int size) {
        return PageRequest.of(number, size);
    }

}
