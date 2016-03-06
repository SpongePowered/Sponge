package org.spongepowered.common.mixin.core.entity.monster;

import net.minecraft.entity.monster.EntityShulker;
import org.spongepowered.api.entity.living.golem.Shulker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityShulker.class)
public abstract class MixinEntityShulker implements Shulker {

}
