package mm.expenses.manager.finance.management;

import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.finance.api.management.model.ExchangeRateTrailDto;
import mm.expenses.manager.finance.api.management.model.ExchangeRatesTrailsPage;
import mm.expenses.manager.finance.api.management.model.OperationType;
import mm.expenses.manager.finance.api.management.model.StateType;
import mm.expenses.manager.finance.config.MapperImplNaming;
import mm.expenses.manager.finance.exchangerate.trail.ExchangeRateTrail;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation;
import mm.expenses.manager.finance.exchangerate.trail.TrailOperation.State;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR, implementationName = MapperImplNaming.TRAIL_MAPPER)
public abstract class TrailMapper extends AbstractMapper {

    @Mapping(target = "operation", expression = "java(map(trail.getOperation()))")
    @Mapping(target = "state", expression = "java(map(trail.getState()))")
    @Mapping(target = "date", expression = "java(trail.getDate())")
    @Mapping(target = "affectedIds", expression = "java(map(trail.getAffectedIds()))")
    abstract ExchangeRateTrailDto map(ExchangeRateTrail trail);

    public List<String> map(final Collection<String> affectedIds) {
        return affectedIds.stream().toList();
    }

    public State map(final StateType state) {
        if (Objects.isNull(state)) {
            return null;
        }
        return switch (state) {
            case SUCCESS -> State.SUCCESS;
            case ERROR -> State.ERROR;
        };
    }

    public StateType map(final State state) {
        if (Objects.isNull(state)) {
            return null;
        }
        return switch (state) {
            case SUCCESS -> StateType.SUCCESS;
            case ERROR -> StateType.ERROR;
        };
    }

    public TrailOperation map(final OperationType operation) {
        if (Objects.isNull(operation)) {
            return null;
        }
        return switch (operation) {
            case CREATE_OR_UPDATE -> TrailOperation.CREATE_OR_UPDATE;
            case EXCHANGE_RATES_HISTORY_UPDATE -> TrailOperation.EXCHANGE_RATES_HISTORY_UPDATE;
            case LATEST_EXCHANGE_RATES_SYNCHRONIZATION -> TrailOperation.LATEST_EXCHANGE_RATES_SYNCHRONIZATION;
        };
    }

    public OperationType map(final TrailOperation operation) {
        if (Objects.isNull(operation)) {
            return null;
        }
        return switch (operation) {
            case CREATE_OR_UPDATE -> OperationType.CREATE_OR_UPDATE;
            case EXCHANGE_RATES_HISTORY_UPDATE -> OperationType.EXCHANGE_RATES_HISTORY_UPDATE;
            case LATEST_EXCHANGE_RATES_SYNCHRONIZATION -> OperationType.LATEST_EXCHANGE_RATES_SYNCHRONIZATION;
        };
    }

    public ExchangeRatesTrailsPage map(final Page<ExchangeRateTrail> trails) {
        var page = new ExchangeRatesTrailsPage();
        page.setContent(map(trails.getContent()));
        page.setTotalElements(trails.getTotalElements());
        page.setNumberOfElements(trails.getNumberOfElements());
        page.setFirst(trails.isFirst());
        page.setLast(trails.isLast());
        page.setTotalPages(trails.getTotalPages());
        page.setHasNext(trails.hasNext());

        return page;
    }

    public List<ExchangeRateTrailDto> map(List<ExchangeRateTrail> content) {
        return content.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

}

