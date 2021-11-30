package org.spongepowered.common.bridge.world.level.biome;

import net.minecraft.world.level.chunk.ChunkGenerator;

public interface BiomeSourceBridge {

    ChunkGenerator bridge$chunkGenerator();

    void bridge$setChunkGenerator(ChunkGenerator generator);
}
