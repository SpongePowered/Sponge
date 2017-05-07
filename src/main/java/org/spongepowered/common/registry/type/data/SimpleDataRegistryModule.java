package org.spongepowered.common.registry.type.data;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Maps;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.SimpleCustomData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.registry.RegistryModule;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class SimpleDataRegistryModule implements RegistryModule {

    private final Map<Key<?>, SimpleCustomData<?>> dataMap = Maps.newHashMap();

    public <T> void register(SimpleCustomData<T> data) {
        checkState(!dataMap.containsKey(data.getKey()), "simple data key already registered!");
        this.dataMap.put(data.getKey(), data);
    }

    public Map<Key<?>, SimpleCustomData<?>> getSimpleCustomData() {
        return Collections.unmodifiableMap(dataMap);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<SimpleCustomData<T>> getSimpleCustomData(Key<Value<T>> key) {
        return Optional.ofNullable((SimpleCustomData<T>) this.dataMap.get(key));
    }

    public boolean isRegistered(Key<?> key) {
        return dataMap.containsKey(key);
    }

    public static SimpleDataRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private SimpleDataRegistryModule() {}

    private static class Holder {
        private static SimpleDataRegistryModule INSTANCE = new SimpleDataRegistryModule();
    }

}
