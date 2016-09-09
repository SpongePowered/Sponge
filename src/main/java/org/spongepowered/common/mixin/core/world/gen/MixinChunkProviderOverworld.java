/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.world.gen;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderOverworld;
import net.minecraft.world.gen.ChunkProviderSettings;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraft.world.gen.structure.StructureOceanMonument;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.biome.VirtualBiomeType;
import org.spongepowered.api.world.extent.ImmutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBiomeArea;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.gen.BiomeGenerator;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.world.gen.IChunkProviderOverworld;
import org.spongepowered.common.interfaces.world.gen.IPopulatorProvider;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.util.gen.ChunkBufferPrimer;
import org.spongepowered.common.util.gen.VirtualMutableBiomeBuffer;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.gen.populators.AnimalPopulator;
import org.spongepowered.common.world.gen.populators.FilteredPopulator;
import org.spongepowered.common.world.gen.populators.SnowPopulator;

import java.util.Random;

@Mixin(ChunkProviderOverworld.class)
public abstract class MixinChunkProviderOverworld implements IChunkProvider, GenerationPopulator, IPopulatorProvider, IChunkProviderOverworld {

    @Shadow @Final private double[] heightMap;
    @Shadow @Final private boolean mapFeaturesEnabled;
    @Shadow @Final private net.minecraft.world.World worldObj;
    @Shadow private ChunkProviderSettings settings;
    @Shadow @Final private Random rand;

    @Shadow private MapGenBase caveGenerator;
    @Shadow private MapGenStronghold strongholdGenerator;
    @Shadow private MapGenVillage villageGenerator;
    @Shadow private MapGenMineshaft mineshaftGenerator;
    @Shadow private MapGenScatteredFeature scatteredFeatureGenerator;
    @Shadow private MapGenBase ravineGenerator;
    @Shadow private StructureOceanMonument oceanMonumentGenerator;
    @Shadow private Biome[] biomesForGeneration;

    @Shadow public abstract void setBlocksInChunk(int p_180518_1_, int p_180518_2_, ChunkPrimer p_180518_3_);

    private Cause cause = Cause.source(SpongeImpl.getPlugin()).build();
    private BiomeGenerator biomegen;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(net.minecraft.world.World worldIn, long p_i45636_2_, boolean p_i45636_4_, String p_i45636_5_, CallbackInfo ci) {
        if (this.settings == null) {
            this.settings = new ChunkProviderSettings.Factory().build();
        }
    }

    @Override
    public void setBiomeGenerator(BiomeGenerator biomes) {
        this.biomegen = biomes;
    }

    @Override
    public void addPopulators(WorldGenerator generator) {
        if (this.settings.useCaves) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.caveGenerator);
        }

        if (this.settings.useRavines) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.ravineGenerator);
        }

        // Structures are both generation populators and populators as they are
        // placed in a two phase system

        if (this.settings.useMineShafts && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.mineshaftGenerator);
            generator.getPopulators().add((Populator) this.mineshaftGenerator);
        }

        if (this.settings.useVillages && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.villageGenerator);
            generator.getPopulators().add((Populator) this.villageGenerator);
        }

        if (this.settings.useStrongholds && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.strongholdGenerator);
            generator.getPopulators().add((Populator) this.strongholdGenerator);
        }

        if (this.settings.useTemples && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.scatteredFeatureGenerator);
            generator.getPopulators().add((Populator) this.scatteredFeatureGenerator);
        }

        if (this.settings.useMonuments && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.oceanMonumentGenerator);
            generator.getPopulators().add((Populator) this.oceanMonumentGenerator);
        }

        if (this.settings.useWaterLakes) {
            Lake lake = Lake.builder()
                    .chance(1d / this.settings.waterLakeChance)
                    .liquidType((BlockState) Blocks.WATER.getDefaultState())
                    .height(VariableAmount.baseWithRandomAddition(0, 256))
                    .build();
            FilteredPopulator filtered = new FilteredPopulator(lake, (c) -> {
                Biome biomegenbase = this.worldObj.getBiome(VecHelper.toBlockPos(c.getBlockMin()).add(16, 0, 16));
                return biomegenbase != Biomes.DESERT && biomegenbase != Biomes.DESERT_HILLS;
            });
            filtered.setRequiredFlags(WorldGenConstants.VILLAGE_FLAG);
            generator.getPopulators().add(filtered);
        }

        if (this.settings.useLavaLakes) {
            Lake lake = Lake.builder()
                    .chance(1d / this.settings.waterLakeChance)
                    .liquidType((BlockState) Blocks.WATER.getDefaultState())
                    .height(VariableAmount.baseWithVariance(0,
                            VariableAmount.baseWithRandomAddition(8, VariableAmount.baseWithOptionalAddition(55, 193, 0.1))))
                    .build();
            FilteredPopulator filtered = new FilteredPopulator(lake);
            filtered.setRequiredFlags(WorldGenConstants.VILLAGE_FLAG);
            generator.getPopulators().add(filtered);
        }

        if (this.settings.useDungeons) {
            Dungeon dungeon = Dungeon.builder()
                    // this is actually a count, terrible naming
                    .attempts(this.settings.dungeonChance)
                    .build();
            generator.getPopulators().add(dungeon);
        }

        generator.getPopulators().add(new AnimalPopulator());
        generator.getPopulators().add(new SnowPopulator());

    }

    @Override
    public void populate(World world, MutableBlockVolume buffer, ImmutableBiomeArea biomes) {
        int x = GenericMath.floor(buffer.getBlockMin().getX() / 16f);
        int z = GenericMath.floor(buffer.getBlockMin().getZ() / 16f);
        this.rand.setSeed((long) x * 341873128712L + (long) z * 132897987541L);
        this.biomesForGeneration = getBiomesFromGenerator(x, z);
        ChunkPrimer chunkprimer = new ChunkBufferPrimer(buffer);
        this.setBlocksInChunk(x, z, chunkprimer);
        setBedrock(buffer);
    }

    private void setBedrock(MutableBlockVolume buffer) {
        Vector3i min = buffer.getBlockMin();
        for (int x = 0; x < 16; x++) {
            int x0 = min.getX() + x;
            for (int z = 0; z < 16; z++) {
                int z0 = min.getZ() + z;
                for (int y = 0; y < 6; y++) {
                    int y0 = min.getY() + y;
                    if (y <= this.rand.nextInt(5)) {
                        buffer.setBlock(x0, y0, z0, BlockTypes.BEDROCK.getDefaultState(), this.cause);
                    }
                }
            }
        }
    }

    private Biome[] getBiomesFromGenerator(int x, int z) {
        if (this.biomegen instanceof BiomeProvider) {
            return ((BiomeProvider) this.biomegen).getBiomesForGeneration(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        }
        // If its not a WorldChunkManager then we have to perform a reverse of
        // the voronoi zoom biome generation layer to get a zoomed out version
        // of the biomes that the terrain generator expects. While not an exact
        // reverse of the algorithm this should be accurate 99.997% of the time
        // (based on testing).
        MutableBiomeArea buffer = new VirtualMutableBiomeBuffer(new Vector2i(x * 16 - 6, z * 16 - 6), new Vector2i(37, 37));
        this.biomegen.generateBiomes(buffer);
        if (this.biomesForGeneration == null || this.biomesForGeneration.length < 100) {
            this.biomesForGeneration = new Biome[100];
        }
        for (int bx = 0; bx < 40; bx += 4) {
            int absX = bx + x * 16 - 6;
            for (int bz = 0; bz < 40; bz += 4) {
                int absZ = bz + z * 16 - 6;
                BiomeType type = buffer.getBiome(absX, absZ);
                if (type instanceof VirtualBiomeType) {
                    type = ((VirtualBiomeType) type).getPersistedType();
                }
                this.biomesForGeneration[(bx / 4) + (bz / 4) * 10] = (Biome) type;
            }
        }
        return this.biomesForGeneration;
    }

    /**
     * @author gabizou - February 1st, 2016
     *
     * Redirects this method call to just simply return the current bimoes, as
     * necessitated by @Deamon's changes. This avoids an overwrite entirely.
     */
    @Redirect(method = "setBlocksInChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/BiomeProvider;getBiomesForGeneration([Lnet/minecraft/world/biome/Biome;IIII)[Lnet/minecraft/world/biome/Biome;"))
    private Biome[] onSetBlocksGetBiomesIgnore(BiomeProvider manager, Biome[] biomes, int x, int z, int width, int height) {
        return biomes;
    }

}
