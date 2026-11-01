package mm.expenses.manager.account.core.user;

import mm.expenses.manager.account.api.user.model.UserPage;
import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.account.api.user.model.UserResponseWithPermissions;
import mm.expenses.manager.common.utils.mapper.AbstractMapper;
import mm.expenses.manager.common.utils.util.DateUtils;
import mm.expenses.manager.common.utils.util.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.stream.Collectors;

@Mapper(
        componentModel = AbstractMapper.COMPONENT_MODEL, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        imports = {StringUtils.class, Collectors.class, DateUtils.class, IdUtils.class}
)
public interface UserMapper extends AbstractMapper {

    UserResponse mapToResponse(final User user);

    UserResponseWithPermissions mapToResponseWithPermissions(final User user);

    @Mapping(target = "content", expression = "java(userPage.getContent())")
    @Mapping(target = "hasNext", expression = "java(userPage.hasNext())")
    @Mapping(target = "elements", source = "userPage.numberOfElements")
    @Mapping(target = "page", source = "userPage.number")
    @Mapping(target = "size", source = "userPage.size")
    @Mapping(target = "totalElements", source = "userPage.totalElements")
    @Mapping(target = "totalPages", source = "userPage.totalPages")
    UserPage mapToPageResponse(final Page<UserResponse> userPage);

}

