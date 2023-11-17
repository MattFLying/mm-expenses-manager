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

import java.util.Objects;
import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        implementationName = MapperImplNaming.TRAIL_MAPPER,
        imports = {Collectors.class}
)
public interface TrailMapper extends AbstractMapper {

    @Mapping(target = "operation", expression = "java(map(trail.getOperation()))")
    @Mapping(target = "state", expression = "java(map(trail.getState()))")
    @Mapping(target = "affectedIds", expression = "java(trail.getAffectedIds().stream().toList())")
    ExchangeRateTrailDto mapToResponse(final ExchangeRateTrail trail);

    @Mapping(target = "content", expression = "java(trailsPage.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))")
    @Mapping(target = "hasNext", expression = "java(trailsPage.hasNext())")
    ExchangeRatesTrailsPage mapToPageResponse(final Page<ExchangeRateTrail> trailsPage);

    default State map(final StateType state) {
        if (Objects.isNull(state)) {
            return null;
        }
        return switch (state) {
            case SUCCESS -> State.SUCCESS;
            case ERROR -> State.ERROR;
        };
    }

    default StateType map(final State state) {
        if (Objects.isNull(state)) {
            return null;
        }
        return switch (state) {
            case SUCCESS -> StateType.SUCCESS;
            case ERROR -> StateType.ERROR;
        };
    }

    default TrailOperation map(final OperationType operation) {
        if (Objects.isNull(operation)) {
            return null;
        }
        return switch (operation) {
            case CREATE_OR_UPDATE -> TrailOperation.CREATE_OR_UPDATE;
            case EXCHANGE_RATES_HISTORY_UPDATE -> TrailOperation.EXCHANGE_RATES_HISTORY_UPDATE;
            case LATEST_EXCHANGE_RATES_SYNCHRONIZATION -> TrailOperation.LATEST_EXCHANGE_RATES_SYNCHRONIZATION;
        };
    }

    default OperationType map(final TrailOperation operation) {
        if (Objects.isNull(operation)) {
            return null;
        }
        return switch (operation) {
            case CREATE_OR_UPDATE -> OperationType.CREATE_OR_UPDATE;
            case EXCHANGE_RATES_HISTORY_UPDATE -> OperationType.EXCHANGE_RATES_HISTORY_UPDATE;
            case LATEST_EXCHANGE_RATES_SYNCHRONIZATION -> OperationType.LATEST_EXCHANGE_RATES_SYNCHRONIZATION;
        };
    }

}

