package mm.expenses.manager.common.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class AbstractMapper {

    @Named("createInstantNow")
    public Instant createInstantNow() {
        return Instant.now();
    }

    @Named("fromStringToLocalDate")
    public LocalDate fromStringToLocalDate(final String date) {
        return LocalDate.parse(date);
    }

    @Named("fromLongToLocalDate")
    public LocalDate fromLongToLocalDate(final Long date) {
        return LocalDate.ofEpochDay(date);
    }

    @Named("fromLocalDateToLong")
    public Long fromLocalDateToLong(final LocalDate date) {
        return date.toEpochDay();
    }

    @Named("fromLocalDateToInstant")
    public Instant fromLocalDateToInstant(final LocalDate date) {
        return Instant.from(date.atStartOfDay(ZoneId.of("UTC")));
    }

    @Named("fromInstantToLocalDate")
    public LocalDate fromInstantToLocalDate(final Instant date) {
        return LocalDate.ofInstant(date, ZoneId.of("UTC"));
    }

    @Named("fromLocalDateToString")
    public String fromLocalDateToString(final LocalDate date) {
        return date.toString();
    }

    @Named("trimString")
    protected String trimString(final String value) {
        return value.trim();
    }

    @Named("generateId")
    protected String generateId() {
        return UUID.randomUUID().toString();
    }

}

