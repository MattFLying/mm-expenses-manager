package mm.expenses.manager.common.utils.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface AbstractMapper {

    String COMPONENT_MODEL = "spring";

}

