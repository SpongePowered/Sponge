package org.spongepowered.collections.multimap;

import com.google.common.collect.Multimap;

public abstract class AbstractMultimap<K, V> implements Multimap<K, V> {

    @Override
    public boolean isEmpty() {
        return this.size() != 0;
    }
}
