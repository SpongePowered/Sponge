package org.spongepowered.common.bridge.tags;

public interface TagWrapperBridge<T> {
    void bridge$rebindTo(net.minecraft.tags.Tag<T> tag);
}
