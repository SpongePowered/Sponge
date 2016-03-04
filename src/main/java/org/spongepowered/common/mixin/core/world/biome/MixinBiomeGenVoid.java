package org.spongepowered.common.mixin.core.world.biome;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeVoid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;

@Mixin(BiomeVoid.class)
public class MixinBiomeGenVoid extends MixinBiomeGenBase {

    @Override
    public void buildPopulators(World world, SpongeBiomeGenerationSettings gensettings) {
        // no super call
    }
}
