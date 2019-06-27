package org.spongepowered.common.mixin.core.world.biome;

import net.minecraft.world.biome.BiomeDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeDecorator.class)
public interface BiomeDecoratorAccessor {

    @Accessor("sandPatchesPerChunk") int accessor$getSandPerChunk();

    @Accessor("sandPatchesPerChunk") void accessor$setSandPerChunk(int patches);

    @Accessor("clayPerChunk") int accessor$getClayPerChunk();

    @Accessor("clayPerChunk") void accessor$setClayPerChunk(int patches);

    @Accessor("gravelPatchesPerChunk") int accessor$getGravelPerChunk();

    @Accessor("gravelPatchesPerChunk") void accessor$setGravelPerChunk(int patches);

    @Accessor("treesPerChunk") int accessor$getTreesPerChunk();

    @Accessor("treesPerChunk") void accessor$setTreesPerChunk(int patches);

    @Accessor("cactiPerChunk") int accessor$getCactiPerChunk();

    @Accessor("cactiPerChunk") void accessor$setCactiPerChunk(int patches);

    @Accessor("reedsPerChunk") int accessor$getReedsPerChunk();

    @Accessor("reedsPerChunk") void accessor$setReedsPerChunk(int patches);

    @Accessor("bigMushroomsPerChunk") int accessor$getBigMushroomsPerChunk();

    @Accessor("bigMushroomsPerChunk") void accessor$setBigMushroomsPerChunk(int patches);

    @Accessor("flowersPerChunk") int accessor$getFlowersPerChunk();

    @Accessor("flowersPerChunk") void accessor$setFlowersPerChunk(int patches);

    @Accessor("grassPerChunk") int accessor$getGrassPerChunk();

    @Accessor("grassPerChunk") void accessor$setGrassPerChunk(int patches);

    @Accessor("deadBushPerChunk") int accessor$getDeadBushPerChunk();

    @Accessor("deadBushPerChunk") void accessor$setDeadBushPerChunk(int patches);

    @Accessor("waterlilyPerChunk") int accessor$getWaterLilyPerChunk();

    @Accessor("waterlilyPerChunk") void accessor$setWaterLilyPerChunk(int patches);

    @Accessor("mushroomsPerChunk") int accessor$getMushroomsPerChunk();

    @Accessor("mushroomsPerChunk") void accessor$setMushroomsPerChunk(int patches);


}
