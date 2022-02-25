package org.spongepowered.collections.multimap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multiset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassInheritanceOrderedMultimap<C, V> extends AbstractMultimap<Class<? extends C>, V> implements ListMultimap<Class<? extends C>, V> {

    private final Map<Class<? extends C>, List<V>> classMap = new ConcurrentHashMap<>();
    private final Class<? extends C> baseClass;
    private final Multimap<Class<? extends C>, V> registrations = ArrayListMultimap.create();

    public static <Clazz, Value> ClassInheritanceOrderedMultimap<Clazz, Value> create(final Class<? extends Clazz> baseClass) {
        return new ClassInheritanceOrderedMultimap<>(baseClass);
    }

    private ClassInheritanceOrderedMultimap(final Class<? extends C> baseClass) {
        this.baseClass = baseClass;
    }

    @Override
    public List<V> get(final Class<? extends C> key) {
        return this.classMap.computeIfAbsent(key, k -> {
            final List<V> matchingEntries = new ArrayList<>();
            this.registrations.entries().forEach(e -> {
                final Class<? extends C> clazz = e.getKey();
                final Set<Class<?>> visited = new HashSet<>();
                for (Class<?> parent = k; parent != Object.class && parent != this.baseClass.getSuperclass(); parent = parent.getSuperclass()) {
                    if (!visited.add(parent)) {
                        continue;
                    }
                    if (clazz.isAssignableFrom(parent)) {
                        matchingEntries.add(e.getValue());
                    }
                }
            });
            return ImmutableList.copyOf(matchingEntries);
        });
    }

    @Override
    public Set<Class<? extends C>> keySet() {
        return null;
    }

    @Override
    public Multiset<Class<? extends C>> keys() {
        return null;
    }

    @Override
    public Collection<V> values() {
        return null;
    }

    @Override
    public Collection<Map.Entry<Class<? extends C>, V>> entries() {
        return null;
    }

    @Override
    public List<V> removeAll(final Object key) {
        return null;
    }

    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean containsKey(final Object key) {
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        return false;
    }

    @Override
    public boolean containsEntry(final Object key, final Object value) {
        return false;
    }

    @Override
    public boolean put(final Class<? extends C> key, final V value) {
        Objects.requireNonNull(key, "Class key cannot be null");
        Objects.requireNonNull(value, "Value cannot be null");
        return this.registrations.put(key, value);
    }

    @Override
    public boolean remove(final Object key, final Object value) {
        return false;
    }

    @Override
    public boolean putAll(final Class<? extends C> key, final Iterable<? extends V> values) {
        return false;
    }

    @Override
    public boolean putAll(final Multimap<? extends Class<? extends C>, ? extends V> multimap) {
        return false;
    }

    @Override
    public List<V> replaceValues(final Class<? extends C> key, final Iterable<? extends V> values) {
        return null;
    }

    @Override
    public Map<Class<? extends C>, Collection<V>> asMap() {
        return null;
    }
}
