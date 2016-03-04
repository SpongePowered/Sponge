package org.spongepowered.common.world.gen.populators;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeEndDecorator;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.util.VecHelper;

import java.util.Random;

public class EndSpikePopulator implements Populator {

    private final WorldGenSpikes spikeGen = new WorldGenSpikes();

    @Override
    public PopulatorType getType() {
        return null;
    }

    @Override
    public void populate(Chunk chunk, Random random) {
        World worldIn = (World) chunk.getWorld();
        WorldGenSpikes.EndSpike[] aworldgenspikes$endspike = BiomeEndDecorator.func_185426_a(worldIn);
        BlockPos pos = VecHelper.toBlockPos(chunk.getBlockMin());
        for (WorldGenSpikes.EndSpike worldgenspikes$endspike : aworldgenspikes$endspike) {
            if (worldgenspikes$endspike.func_186154_a(pos)) {
                this.spikeGen.func_186143_a(worldgenspikes$endspike);
                this.spikeGen.generate(worldIn, random,
                        new BlockPos(worldgenspikes$endspike.func_186151_a(), 45, worldgenspikes$endspike.func_186152_b()));
            }
        }
    }

}
