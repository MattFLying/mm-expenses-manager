package mm.expenses.manager.common.utils.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mm.expenses.manager.common.exceptions.date.InvalidDateTimeException;
import mm.expenses.manager.common.utils.exception.CommonUtilsExceptionMessage;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

    public static final String DEFAULT_ZONE = "UTC";

    public static final ZoneId ZONE_UTC = zoneOf(DEFAULT_ZONE);

    // Zone
    public static ZoneId zoneOf(final String zoneId) {
        try {
            return ZoneId.of(zoneId);
        } catch (final Exception exception) {
            log.warn("Passed zone is null", exception);
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.ZONE_NULL_NOT_ALLOWED, exception);
        }
    }

    // LocalDate
    public static LocalDate nowAsLocalDate() {
        return LocalDate.now();
    }

    public static LocalDate beginningOfTheYear(final int year) {
        return LocalDate.of(year, 1, 1);
    }

    public static LocalDate instantToLocalDate(final Instant date, final ZoneId zoneId) {
        if (Objects.isNull(zoneId)) {
            log.warn("Passed zone is null");
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.ZONE_NULL_NOT_ALLOWED);
        }
        if (Objects.isNull(date)) {
            log.warn("Passed instant is null");
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.INSTANT_NULL_NOT_ALLOWED);
        }
        try {
            return LocalDate.ofInstant(date, zoneId);
        } catch (final Exception exception) {
            log.warn("Instant cannot be converted to LocalDate", exception);
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.INSTANT_TO_LOCAL_DATE_ERROR, exception);
        }
    }

    public static LocalDate instantToLocalDate(final Instant date) {
        return instantToLocalDate(date, ZONE_UTC);
    }

    public static LocalDate fromStringToLocalDate(final String date) {
        if (StringUtils.isEmpty(date)) {
            log.warn("Passed date string is null");
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.DATE_STRING_NULL_NOT_ALLOWED);
        }
        try {
            return LocalDate.parse(date);
        } catch (final Exception exception) {
            log.warn("Instant cannot be converted to LocalDate", exception);
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.DATE_STRING_CANNOT_BE_PARSED, exception);
        }
    }

    public static LocalDate fromLongToLocalDate(final Long date) {
        if (Objects.isNull(date)) {
            log.warn("Passed date string is null");
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.DATE_LONG_NULL_NOT_ALLOWED);
        }
        return LocalDate.ofEpochDay(date);
    }

    // Instant
    public static Instant nowAsInstant() {
        return Instant.now();
    }

    public static Instant localDateToInstant(final LocalDate date) {
        return localDateToInstant(date, ZONE_UTC);
    }

    public static Instant localDateToInstant(final LocalDate date, final ZoneId zoneId) {
        if (Objects.isNull(zoneId)) {
            log.warn("Passed zone is null");
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.ZONE_NULL_NOT_ALLOWED);
        }
        if (Objects.isNull(date)) {
            log.warn("Passed local date is null");
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.LOCAL_DATE_NULL_NOT_ALLOWED);
        }
        try {
            return Instant.from(date.atStartOfDay(zoneId));
        } catch (final Exception exception) {
            log.warn("LocalDate cannot be converted to Instant", exception);
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.LOCAL_DATE_TO_INSTANT_ERROR, exception);
        }
    }

    // Long
    public static long daysBetween(final LocalDate firstDate, final LocalDate secondDate) {
        if (Objects.isNull(firstDate) || Objects.isNull(secondDate)) {
            log.warn("One or both passed local dates are null: firstDate: {}, secondDate: {}", firstDate, secondDate);
            throw new InvalidDateTimeException(CommonUtilsExceptionMessage.LOCAL_DATE_NULL_NOT_ALLOWED);
        }
        return ChronoUnit.DAYS.between(firstDate, secondDate);
    }

}
