package org.spongepowered.common.data.util;

import net.minecraft.state.IProperty;
import net.minecraft.state.StateHolder;

import java.util.Map;

@SuppressWarnings("unchecked")
public class StateHelper {

    public static <O, S extends StateHolder<O, S>> S copyStatesFrom(S original, StateHolder<?,?> from) {
        for (final Map.Entry<IProperty<?>, Comparable<?>> entry : from.getValues().entrySet()) {
            if (original.has(entry.getKey())) {
                original = (S) original.with(entry.getKey(), (Comparable) entry.getValue());
            }
        }
        return original;
    }
}
