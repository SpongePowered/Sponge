package org.spongepowered.common.accessor.tags;

import net.minecraft.tags.StaticTagHelper;
import net.minecraft.tags.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StaticTagHelper.class)
public interface StaticTagHelperAccessor<T> {

    @Accessor("wrappers")
    List<? extends Tag.Named<T>> accessor$wrappers();

}
