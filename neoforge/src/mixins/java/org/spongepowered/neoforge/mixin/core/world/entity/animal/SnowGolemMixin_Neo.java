package org.spongepowered.neoforge.mixin.core.world.entity.animal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.entity.GrieferBridge;

@Mixin(SnowGolem.class)
public abstract class SnowGolemMixin_Neo {

    @Redirect(method = "aiStep()V", at = @At(
        value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;canEntityGrief(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean impl$checkCanGrief(ServerLevel level, Entity entity) {
        if (!((GrieferBridge) this).bridge$canGrief()) {
            return false;
        }
        return EventHooks.canEntityGrief(level, entity);
    }
}
