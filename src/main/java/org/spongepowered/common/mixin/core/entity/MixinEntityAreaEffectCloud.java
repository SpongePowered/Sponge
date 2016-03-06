package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.EntityAreaEffectCloud;
import org.spongepowered.api.entity.AreaEffectCloud;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityAreaEffectCloud.class)
public abstract class MixinEntityAreaEffectCloud extends MixinEntity implements AreaEffectCloud {

}
