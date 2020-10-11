package org.spongepowered.common.mixin.api.mcp.world.chunk;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.accessor.world.biome.BiomeContainerAccessor;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class ChunkMixin_API implements Chunk {

    @Shadow private BiomeContainer blockBiomeArray;

    @Override
    public boolean setBiome(int x, int y, int z, BiomeType biome) {

        final Biome[] biomes = ((BiomeContainerAccessor) this.blockBiomeArray).accessor$getBiomes();

        int maskedX = x & BiomeContainer.HORIZONTAL_MASK;
        int maskedY = MathHelper.clamp(y, 0, BiomeContainer.VERTICAL_MASK);
        int maskedZ = z & BiomeContainer.HORIZONTAL_MASK;

        final int WIDTH_BITS = BiomeContainerAccessor.accessor$WIDTH_BITS();
        final int posKey = maskedY << WIDTH_BITS + WIDTH_BITS | maskedZ << WIDTH_BITS | maskedX;
        biomes[posKey] = (Biome) biome;

        return true;
    }

    // TODO implement the rest of it
}
