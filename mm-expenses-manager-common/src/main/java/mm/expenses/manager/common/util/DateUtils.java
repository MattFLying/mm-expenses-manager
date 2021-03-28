package mm.expenses.manager.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    public static final ZoneId ZONE_UTC = ZoneId.of("UTC");

    public static LocalDate instantToLocalDateUTC(final Instant date) {
        return LocalDate.ofInstant(date, ZONE_UTC);
    }

    public static LocalDate beginningOfTheYear(final int year) {
        return LocalDate.of(year, 1, 1);
    }

    public static Instant now() {
        return Instant.now();
    }

    public static long daysBetween(final LocalDate firstDate, final LocalDate secondDate) {
        return ChronoUnit.DAYS.between(firstDate, secondDate);
    }

}
