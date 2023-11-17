package mm.expenses.manager.common.utils.util;

import mm.expenses.manager.common.exceptions.date.InvalidDateTimeException;
import mm.expenses.manager.common.utils.BaseInitTest;
import mm.expenses.manager.common.utils.exception.CommonUtilsExceptionMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateUtilsTest extends BaseInitTest {

    @Test
    void zoneOf_test() throws Exception {
        final var zone = DateUtils.zoneOf(DateUtils.DEFAULT_ZONE);

        assertThat(zone).isEqualTo(ZoneId.of(DateUtils.DEFAULT_ZONE));
    }

    @Test
    void zoneOf_zoneIsNull_throwsInvalidDateTimeException_test() throws Exception {
        assertThatThrownBy(() -> DateUtils.zoneOf(null))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.ZONE_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void nowAsLocalDate_test() throws Exception {
        final var now = DateUtils.nowAsLocalDate();

        assertThat(now).isNotNull().isEqualTo(DateUtils.nowAsLocalDate());
    }

    @Test
    void nowAsInstant_test() throws Exception {
        final var now = DateUtils.nowAsInstant();

        assertThat(now).isNotNull().isBefore(DateUtils.nowAsInstant().plusSeconds(100));
    }

    @Test
    void beginningOfTheYear_test() throws Exception {
        final var expectedYear = 2023;
        final var expectedDate = LocalDate.of(expectedYear, 1, 1);
        final var beginningOfTheYear = DateUtils.beginningOfTheYear(expectedYear);

        assertThat(beginningOfTheYear).isNotNull().isEqualTo(expectedDate);
        assertThat(expectedDate.getYear()).isEqualTo(expectedYear);
    }

    @Test
    void instantToLocalDate_test() throws Exception {
        final var instant = DateUtils.nowAsInstant();
        final var localDate = DateUtils.nowAsLocalDate();

        final var zone = DateUtils.ZONE_UTC;
        final var now = DateUtils.instantToLocalDate(instant, zone);

        assertThat(now).isNotNull().isEqualTo(localDate);
    }

    @Test
    void instantToLocalDate_zoneIsNull_throwsInvalidDateTimeException_test() throws Exception {
        final var instant = DateUtils.nowAsInstant();

        assertThatThrownBy(() -> DateUtils.instantToLocalDate(instant, null))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.ZONE_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void instantToLocalDate_instantIsNull_throwsInvalidDateTimeException_test() throws Exception {
        final var zone = DateUtils.ZONE_UTC;

        assertThatThrownBy(() -> DateUtils.instantToLocalDate(null, zone))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.INSTANT_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void fromStringToLocalDate_test() throws Exception {
        final var localDate = DateUtils.nowAsLocalDate();
        final var localDateAsString = localDate.toString();

        final var now = DateUtils.fromStringToLocalDate(localDateAsString);

        assertThat(now).isNotNull().isEqualTo(localDate);
    }

    @Test
    void fromStringToLocalDate_throwsInvalidDateTimeException_dateIsNull_test() throws Exception {
        assertThatThrownBy(() -> DateUtils.fromStringToLocalDate(null))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.DATE_STRING_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void fromStringToLocalDate_throwsInvalidDateTimeException_dateCannotBeParsed_test() throws Exception {
        assertThatThrownBy(() -> DateUtils.fromStringToLocalDate("test"))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.DATE_STRING_CANNOT_BE_PARSED.getMessage());
    }

    @Test
    void fromLongToLocalDate_test() throws Exception {
        final var localDate = DateUtils.nowAsLocalDate();
        final var localDateAsLong = localDate.toEpochDay();

        final var now = DateUtils.fromLongToLocalDate(localDateAsLong);

        assertThat(now).isNotNull().isEqualTo(localDate);
    }

    @Test
    void fromLongToLocalDate_throwsInvalidDateTimeException_dateIsNull_test() throws Exception {
        assertThatThrownBy(() -> DateUtils.fromLongToLocalDate(null))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.DATE_LONG_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void localDateToInstant_test() throws Exception {
        final var localDate = DateUtils.nowAsLocalDate();
        final var instant = DateUtils.nowAsInstant();

        final var zone = DateUtils.ZONE_UTC;
        final var now = DateUtils.localDateToInstant(localDate, zone);

        assertThat(now).isNotNull();
        assertThat(now.getEpochSecond()).isLessThan(instant.getEpochSecond());
    }

    @Test
    void localDateToInstant_zoneIsNull_throwsInvalidDateTimeException_test() throws Exception {
        final var localDate = DateUtils.nowAsLocalDate();

        assertThatThrownBy(() -> DateUtils.localDateToInstant(localDate, null))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.ZONE_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void localDateToInstant_localDateIsNull_throwsInvalidDateTimeException_test() throws Exception {
        final var zone = DateUtils.ZONE_UTC;

        assertThatThrownBy(() -> DateUtils.localDateToInstant(null, zone))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.LOCAL_DATE_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void daysBetween_test() throws Exception {
        final var localDate_first = DateUtils.nowAsLocalDate();
        final var localDate_second = localDate_first.plusDays(10);

        final var expectedDays = localDate_first.getDayOfMonth() - localDate_second.getDayOfMonth();

        final var daysBetween = DateUtils.daysBetween(localDate_second, localDate_first);

        assertThat(daysBetween).isEqualTo(expectedDays);
    }

    @Test
    void daysBetween_firstDateIsNull_throwsInvalidDateTimeException_test() throws Exception {
        final var localDate_first = DateUtils.nowAsLocalDate();

        assertThatThrownBy(() -> DateUtils.daysBetween(null, localDate_first))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.LOCAL_DATE_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void daysBetween_secondDateIsNull_throwsInvalidDateTimeException_test() throws Exception {
        final var localDate_first = DateUtils.nowAsLocalDate();

        assertThatThrownBy(() -> DateUtils.daysBetween(localDate_first, null))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.LOCAL_DATE_NULL_NOT_ALLOWED.getMessage());
    }

    @Test
    void daysBetween_bothDatesIsNull_throwsInvalidDateTimeException_test() throws Exception {
        assertThatThrownBy(() -> DateUtils.daysBetween(null, null))
                .isInstanceOf(InvalidDateTimeException.class)
                .hasMessage(CommonUtilsExceptionMessage.LOCAL_DATE_NULL_NOT_ALLOWED.getMessage());
    }

}