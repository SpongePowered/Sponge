package org.spongepowered.common.mixin.optimization;

import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.mixin.optimization.explosion.IMixinExplosion;

@Mixin(Explosion.class)
public class MixinExplosion_Explosion {


    @ModifyConstant(method = "doExplosionB", constant = @Constant(intValue = 3))
    private int getBlockUpdateFlag(int flag) {
        return 2;
    }

    @Redirect(method = "doExplosionB", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private void onSpawnParticles(World world, EnumParticleTypes particleTypes, double x, double y, double z, double deltaX, double deltaY, double deltaZ, int[] args) {
        if (world instanceof WorldServer) {
            ((WorldServer) world).spawnParticle(particleTypes, x, y, z, 1, deltaX, deltaY, deltaZ, 0.1D, args);
        } else {
            world.spawnParticle(particleTypes, x, y, z, deltaX, deltaY, deltaZ, args);
        }
    }

}
