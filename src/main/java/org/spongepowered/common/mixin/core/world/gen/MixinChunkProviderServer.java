package org.spongepowered.common.mixin.core.world.gen;

import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;

import javax.annotation.Nullable;

@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer implements IMixinChunkProviderServer {

    @Shadow public WorldServer worldObj;
    @Shadow private LongHashMap<Chunk> id2ChunkMap;

    @Shadow public abstract Chunk provideChunk(int x, int z);

    @Nullable
    @Override
    public Chunk getChunkIfLoaded(int x, int z) {
        return this.id2ChunkMap.getValueByKey(ChunkCoordIntPair.chunkXZ2Int(x, z));
    }

}
