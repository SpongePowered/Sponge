package org.spongepowered.common.mixin.core.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.tags.TagWrapperBridge;

import java.util.function.Function;

@Mixin(targets = "net.minecraft.tags.StaticTagHelper$Wrapper")
public abstract class TagWrapperMixin<T> implements TagWrapperBridge<T> {
    @Shadow abstract void rebind(Function<ResourceLocation, Tag<T>> param0);

    @Override
    public void bridge$rebindTo(Tag<T> tag) {
        this.rebind(a -> tag);
    }
}
