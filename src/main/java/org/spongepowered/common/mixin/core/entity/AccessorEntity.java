package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Random;

@Mixin(Entity.class)
public interface AccessorEntity {

    @Accessor("rand") Random spongeBridge$getRandom();

}
