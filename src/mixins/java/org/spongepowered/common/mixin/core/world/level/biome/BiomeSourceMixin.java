package org.spongepowered.common.mixin.core.world.level.biome;

import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.world.biome.provider.OverworldBiomeSourceHelper;

@Mixin(BiomeSource.class)
public abstract class BiomeSourceMixin {

    @Redirect(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;"
        + "Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 3))
    private static <T> T register(final Registry<T> registry, final String key, final T original) {
        return Registry.register(registry, key, (T) (Object) OverworldBiomeSourceHelper.DIRECT_CODEC);
    }
}
