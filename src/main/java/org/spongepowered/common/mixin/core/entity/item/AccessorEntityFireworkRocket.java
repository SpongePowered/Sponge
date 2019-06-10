package org.spongepowered.common.mixin.core.entity.item;

import net.minecraft.entity.item.EntityFireworkRocket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityFireworkRocket.class)
public interface AccessorEntityFireworkRocket {

    @Accessor("lifetime")
    int spongeImpl$getLifetime();

    @Accessor("lifetime")
    void spongeImpl$setLifeTime(int lifetime);

}
