package org.spongepowered.common.mixin.timings;

import co.aikar.timings.WorldTimingsHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldServerMulti;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldServerMulti.class)
public abstract class MixinWorldServerMulti extends MixinWorldServer {

    @Inject(method = "init", at = @At("HEAD"))
    public void onInit(CallbackInfoReturnable<World> cir) {
        this.timings = new WorldTimingsHandler((World) (Object) this);
    }
}
