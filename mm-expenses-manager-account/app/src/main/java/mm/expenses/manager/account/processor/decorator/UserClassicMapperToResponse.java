package mm.expenses.manager.account.processor.decorator;

import lombok.RequiredArgsConstructor;
import mm.expenses.manager.account.api.user.model.UserResponse;
import mm.expenses.manager.account.core.user.User;
import mm.expenses.manager.account.core.user.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

/**
 * Classic mapper from {@link User} to {@link UserResponse}.
 */
@RequiredArgsConstructor
public class UserClassicMapperToResponse extends UserMappingStrategy {

    private final UserMapper mapper;
    private boolean isSystemCall = false;

    public UserClassicMapperToResponse(final UserMapper mapper, final boolean isSystemCall) {
        this(mapper);
        this.isSystemCall = isSystemCall;
    }

    @Override
    public UserResponse decorate(final User user) {
        if (isSystemCall) {
            return mapper.mapToResponseWithPermissions(user);
        }
        return mapper.mapToResponse(user);
    }

    @Override
    public Page<UserResponse> decorate(final Page<User> page) {
        final var content = page.getContent()
                .stream()
                .map(this::decorate)
                .toList();
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

}
