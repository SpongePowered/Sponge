package org.spongepowered.common.registry;

import java.util.Optional;

public interface TypeProvider<K, V> {

    Optional<V> get(K key);

    Optional<K> getKey(V value);

}
