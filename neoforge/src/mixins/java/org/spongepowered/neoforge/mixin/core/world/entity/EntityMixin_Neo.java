package org.spongepowered.neoforge.mixin.core.world.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin_Neo {

    @Redirect(method = "thunderHit",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/DamageSources;lightningBolt()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource vanilla$ThrowDamageEventWithLightingSource(final DamageSources sources,
                                                                    final ServerLevel level, final LightningBolt lightningBolt
    ) {
        final var originalLightning = sources.lightningBolt();
        final var entitySource = org.spongepowered.api.event.cause.entity.damage.source.DamageSource.builder()
            .from((org.spongepowered.api.event.cause.entity.damage.source.DamageSource) originalLightning)
            .entity((org.spongepowered.api.entity.Entity) lightningBolt)
            .build();
        return (DamageSource) (Object) entitySource;
    }

}
