package mm.expenses.manager.common.utils.security;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * User permissions concurrent map where as a key is the permission group name
 */
public class UserPermissions extends ConcurrentHashMap<String, Set<Permission>> {

    private boolean hasBeenChanged = false;

    public UserPermissions() {
        super();
    }

    public UserPermissions(final Map<? extends String, ? extends Set<Permission>> permissions) {
        super(permissions);
    }

    public boolean hasBeenChanged() {
        return hasBeenChanged;
    }

    @Override
    public Set<Permission> put(final String key, final Set<Permission> value) {
        final var result = super.put(key, value);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public void putAll(final Map<? extends String, ? extends Set<Permission>> m) {
        super.putAll(m);
        this.hasBeenChanged = true;
    }

    @Override
    public Set<Permission> remove(final Object key) {
        final var result = super.remove(key);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public void clear() {
        super.clear();
        this.hasBeenChanged = true;
    }

    @Override
    public Set<Permission> putIfAbsent(final String key, final Set<Permission> value) {
        final var result = super.putIfAbsent(key, value);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        final var result = super.remove(key, value);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public boolean replace(final String key, final Set<Permission> oldValue, final Set<Permission> newValue) {
        final var result = super.replace(key, oldValue, newValue);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public Set<Permission> replace(final String key, final Set<Permission> value) {
        final var result = super.replace(key, value);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public void replaceAll(final BiFunction<? super String, ? super Set<Permission>, ? extends Set<Permission>> function) {
        super.replaceAll(function);
        this.hasBeenChanged = true;
    }

    @Override
    public Set<Permission> computeIfAbsent(final String key, final Function<? super String, ? extends Set<Permission>> mappingFunction) {
        final var result = super.computeIfAbsent(key, mappingFunction);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public Set<Permission> computeIfPresent(final String key, final BiFunction<? super String, ? super Set<Permission>, ? extends Set<Permission>> remappingFunction) {
        final var result = super.computeIfPresent(key, remappingFunction);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public Set<Permission> compute(final String key, final BiFunction<? super String, ? super Set<Permission>, ? extends Set<Permission>> remappingFunction) {
        final var result = super.compute(key, remappingFunction);
        this.hasBeenChanged = true;

        return result;
    }

    @Override
    public Set<Permission> merge(final String key, final Set<Permission> value, final BiFunction<? super Set<Permission>, ? super Set<Permission>, ? extends Set<Permission>> remappingFunction) {
        final var result = super.merge(key, value, remappingFunction);
        this.hasBeenChanged = true;

        return result;
    }

}
