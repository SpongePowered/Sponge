package org.spongepowered.common.mixin.core.server;

import net.minecraft.server.CustomServerBossInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.mixin.core.world.ServerBossInfoMixin;

@Mixin(CustomServerBossInfo.class)
public abstract class CustomServerBossInfoMixin extends ServerBossInfoMixin {
    @Shadow private int max;

    @Redirect(method = {"getValue", "write"},
        at = @At(value = "FIELD", target = "Lnet/minecraft/server/CustomServerBossInfo;value:I"))
    private int valueRead(final CustomServerBossInfo $this) {
        return (int) (this.bridge$asAdventure().percent() * this.max);
    }

    // Value writes already update the percent field of superclasses, so we don't need to redirect
}
