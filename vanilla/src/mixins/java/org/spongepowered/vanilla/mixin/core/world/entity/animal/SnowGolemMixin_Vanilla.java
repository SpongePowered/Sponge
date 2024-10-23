package org.spongepowered.vanilla.mixin.core.world.entity.animal;


import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.entity.GrieferBridge;

@Mixin(SnowGolem.class)
public abstract class SnowGolemMixin_Vanilla {

    @Redirect(method = "aiStep()V", at = @At(
        value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean impl$checkCanGrief(final GameRules gameRules, final GameRules.Key<GameRules.BooleanValue> key) {
        return gameRules.getBoolean(key) && ((GrieferBridge) this).bridge$canGrief();
    }
}
