package org.spongepowered.forge.mixin.core.world.entity.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.config.SpongeGameConfigs;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin_Forge {

    // @formatter:off
    @Shadow public int lifespan;
    // @formatter:on

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void forge$setLifespanFromConfig(EntityType<? extends ItemEntity> type, Level level, CallbackInfo ci) {
        this.lifespan = SpongeGameConfigs.getForWorld(level).get().entity.item.despawnRate;
    }
}
