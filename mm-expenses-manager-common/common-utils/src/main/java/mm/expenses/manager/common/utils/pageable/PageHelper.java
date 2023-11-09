package mm.expenses.manager.common.utils.pageable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.security.InvalidParameterException;
import java.util.Objects;

import static java.lang.String.format;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageHelper {

    public static Pageable getPageable(final Integer page, final Integer size, final Integer maxPageSize) {
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size > 0)) {
            return pageRequestOf(page, size);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return pageRequestOf(0, maxPageSize);
        } else {
            throw new InvalidParameterException(format("Incorrect page number: %s or/and page size: %s", page, size));
        }
    }

    public static PageRequest getPageRequest(final Integer page, final Integer size, final Integer maxPageSize) {
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size > 0)) {
            return pageRequestOf(page, size);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return pageRequestOf(0, maxPageSize);
        } else {
            throw new InvalidParameterException(format("Incorrect page number: %s or/and page size: %s", page, size));
        }
    }

    public static PageRequest getPageRequest(final Integer page, final Integer size, final Sort sort, final Integer maxPageSize) {
        if (Objects.isNull(sort)) {
            return getPageRequest(page, size, maxPageSize);
        }
        if ((Objects.nonNull(page)) && (Objects.nonNull(size)) && (size > 0)) {
            return PageRequest.of(page, size, sort);
        } else if ((Objects.isNull(page)) && (Objects.isNull(size))) {
            return PageRequest.of(0, maxPageSize, sort);
        } else {
            throw new InvalidParameterException(format("Incorrect page number: %s or/and page size: %s or/and sorting: %s", page, size, sort));
        }
    }

    private static PageRequest pageRequestOf(final int number, final int size) {
        return PageRequest.of(number, size);
    }

}
