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

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorSettings;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.feature.MineshaftStructure;
import net.minecraft.world.gen.feature.OceanMonumentStructure;
import net.minecraft.world.gen.feature.ScatteredStructure;
import net.minecraft.world.gen.feature.StrongholdStructure;
import net.minecraft.world.gen.feature.VillageStructure;
import net.minecraft.world.gen.feature.WoodlandMansionStructure;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
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
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.gen.ChunkGeneratorOverworldBridge;
import org.spongepowered.common.bridge.world.gen.PopulatorProviderBridge;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.util.gen.ObjectArrayMutableBiomeBuffer;
import org.spongepowered.common.world.gen.SpongeChunkGenerator;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.gen.populators.AnimalPopulator;
import org.spongepowered.common.world.gen.populators.FilteredPopulator;
import org.spongepowered.common.world.gen.populators.SnowPopulator;

import java.util.Random;

import javax.annotation.Nullable;

@Mixin(OverworldChunkGenerator.class)
public abstract class ChunkGeneratorOverworldMixin implements PopulatorProviderBridge, ChunkGeneratorOverworldBridge {

    @Shadow @Final private boolean mapFeaturesEnabled;
    @Shadow @Final private net.minecraft.world.World world;
    @Shadow @Nullable private ChunkGeneratorSettings settings;
    @Shadow @Final private MapGenBase caveGenerator;
    @Shadow @Final private StrongholdStructure strongholdGenerator;
    @Shadow @Final private VillageStructure villageGenerator;
    @Shadow @Final private MineshaftStructure mineshaftGenerator;
    @Shadow @Final private ScatteredStructure scatteredFeatureGenerator;
    @Shadow @Final private MapGenBase ravineGenerator;
    @Shadow @Final private OceanMonumentStructure oceanMonumentGenerator;
    @Shadow @Final private WoodlandMansionStructure woodlandMansionGenerator;
    @Shadow private Biome[] biomesForGeneration;

    @Nullable private BiomeGenerator impl$biomegen;
    private boolean impl$isVanilla = WorldGenConstants.isValid((ChunkGenerator) this, GenerationPopulator.class);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setSettings(final net.minecraft.world.World worldIn, final long p_i45636_2_, final boolean p_i45636_4_, final String p_i45636_5_, final CallbackInfo ci) {
        if (this.settings == null) {
            this.settings = new ChunkGeneratorSettings.Factory().func_177864_b();
        }
    }

    @Override
    public void bridge$setBiomeGenerator(final BiomeGenerator biomes) {
        this.impl$biomegen = biomes;
    }

    @Override
    public void bridge$addPopulators(final WorldGenerator generator) {
        if (this.settings.field_177839_r) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.caveGenerator);
        }

        if (this.settings.field_177850_z) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.ravineGenerator);
        }

        // Structures are both generation populators and populators as they are
        // placed in a two phase system

        if (this.settings.field_177829_w && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.mineshaftGenerator);
            generator.getPopulators().add((Populator) this.mineshaftGenerator);
        }

        if (this.settings.field_177831_v && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.villageGenerator);
            generator.getPopulators().add((Populator) this.villageGenerator);
        }

        if (this.settings.field_177833_u && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.strongholdGenerator);
            generator.getPopulators().add((Populator) this.strongholdGenerator);
        }

        if (this.settings.field_177854_x && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.scatteredFeatureGenerator);
            generator.getPopulators().add((Populator) this.scatteredFeatureGenerator);
        }

        if (this.settings.field_177852_y && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.oceanMonumentGenerator);
            generator.getPopulators().add((Populator) this.oceanMonumentGenerator);
        }

        if (this.settings.field_191077_z && this.mapFeaturesEnabled) {
            generator.getGenerationPopulators().add((GenerationPopulator) this.woodlandMansionGenerator);
            generator.getPopulators().add((Populator) this.woodlandMansionGenerator);
        }

        if (this.settings.field_177781_A) {
            final Lake lake = Lake.builder()
                    .chance(1d / this.settings.field_177782_B)
                    .liquidType((BlockState) Blocks.WATER.getDefaultState())
                    .height(VariableAmount.baseWithRandomAddition(0, 256))
                    .build();
            final FilteredPopulator filtered = new FilteredPopulator(lake, (c) -> {
                final Biome biomegenbase = this.world.getBiome(VecHelper.toBlockPos(c.getBlockMin()).add(16, 0, 16));
                return biomegenbase != Biomes.DESERT && biomegenbase != Biomes.DESERT_HILLS;
            });
            filtered.setRequiredFlags(WorldGenConstants.VILLAGE_FLAG);
            generator.getPopulators().add(filtered);
        }

        if (this.settings.field_177783_C) {
            final Lake lake = Lake.builder()
                    .chance(1d / this.settings.field_177777_D)
                    .liquidType((BlockState) Blocks.WATER.getDefaultState())
                    .height(VariableAmount.baseWithVariance(0,
                            VariableAmount.baseWithRandomAddition(8, VariableAmount.baseWithOptionalAddition(55, 193, 0.1))))
                    .build();
            final FilteredPopulator filtered = new FilteredPopulator(lake);
            filtered.setRequiredFlags(WorldGenConstants.VILLAGE_FLAG);
            generator.getPopulators().add(filtered);
        }

        if (this.settings.field_177837_s) {
            final Dungeon dungeon = Dungeon.builder()
                    // this is actually a count, terrible naming
                    .attempts(this.settings.field_177835_t)
                    .build();
            generator.getPopulators().add(dungeon);
        }

        generator.getPopulators().add(new AnimalPopulator());
        generator.getPopulators().add(new SnowPopulator());

    }

    @Override
    public Biome[] bridge$getBiomesForGeneration(final int x, final int z) {
        if (this.impl$biomegen instanceof BiomeProvider) {
            return ((BiomeProvider) this.impl$biomegen).func_76937_a(this.biomesForGeneration, x * 4 - 2, z * 4 - 2, 10, 10);
        }
        // If its not a WorldChunkManager then we have to perform a reverse of
        // the voronoi zoom biome generation layer to get a zoomed out version
        // of the biomes that the terrain generator expects. While not an exact
        // reverse of the algorithm this should be accurate 99.997% of the time
        // (based on testing).
        final ObjectArrayMutableBiomeBuffer buffer = new ObjectArrayMutableBiomeBuffer(new Vector3i(x * 16 - 6, 0, z * 16 - 6), new Vector3i(37, 1, 37));
        this.impl$biomegen.generateBiomes(buffer);
        if (this.biomesForGeneration == null || this.biomesForGeneration.length < 100) {
            this.biomesForGeneration = new Biome[100];
        }
        for (int bx = 0; bx < 40; bx += 4) {
            final int absX = bx + x * 16 - 6;
            for (int bz = 0; bz < 40; bz += 4) {
                final int absZ = bz + z * 16 - 6;
                final Biome type = buffer.getNativeBiome(absX, 0, absZ);
                this.biomesForGeneration[(bx / 4) + (bz / 4) * 10] = type;
            }
        }
        return this.biomesForGeneration;
    }

    /**
     * @author gabizou - February 1st, 2016
     * @author blood - February 6th, 2017 - Only redirect if vanilla generator. 
     *   This fixes the FuturePack mod as it extends the ChunkProviderOverworld generator.
     * @author gabizou - September 18th, 2018 - Updated modification to redirect for vanilla
     *   generators would re-apply the same modified math for the x and z values.
     * @author JBYoshi - 
     *
     * Redirects this method call to just simply return the current biomes, as
     * necessitated by @Deamon's changes. This avoids an overwrite entirely.
     *
     * @param provider The provider
     * @param biomes The biomes field
     * @param x The modified x position (the x is already multiplied by 4 subtracted by 2)
     * @param z The modified z position (the z is already multiplied by 4 subtracted by 2)
     * @param width The defined width (already 10)
     * @param height The defined height (already 10)
     */
    @Redirect(method = "setBlocksInChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/BiomeProvider;getBiomesForGeneration([Lnet/minecraft/world/biome/Biome;IIII)[Lnet/minecraft/world/biome/Biome;"))
    private Biome[] onSetBlocksGetBiomesIgnore(
        final BiomeProvider provider, final Biome[] biomes, final int x, final int z, final int width, final int height) {
        if (this.impl$isVanilla) {
            // SpongeChunkGenerator will directly call the bridge method if it's being used.
            if (biomes == null || !(this.world.getChunkProvider() instanceof SpongeChunkGenerator)) {
                // construct the biomes array according to the method above, allows api biome pops to be used
                return bridge$getBiomesForGeneration((x + 2) / 4, (z + 2) / 4); // Undo the math modification
            }
            return biomes;
        }
        // Re-use the same values passed in since the biomes parameter
        return provider.func_76937_a(biomes, x, z, width, height);
    }

    /**
     * @author gabizou - September 18th, 2018
     * @reason An attempt at isolating an NPE that is occurring with
     * {@code WoodlandMansion.Start#create(net.minecraft.world.World, ChunkGeneratorOverworld, Random, int, int)}
     * and the subsequent
     * {@link ChunkGeneratorOverworld#setBlocksInChunk(int, int, ChunkPrimer)}.
     * Since line numbers cannot be trusted between the crash reports and the targeted bytecode,
     * I've elected to mitigate the issue entirely by redirecting the biome for generation array usage
     * to eliminate possible null values stored in the array during said world generation. The issue is
     * that since the biome array is constructed in the redirect above, or other places (sponge world gen),
     * it is not quite clear to determine from the stacktraces alone whether the chunk generator is vanilla,
     * not vanilla, extended, sponge managed, mod modified, or simply something else in the massive target method
     * that is producing an NPE.
     * {@see https://github.com/SpongePowered/SpongeForge/issues/1946}
     *
     * @param array The biome array being accessed (equivalent to this.biomesForGeneration)
     * @param index The index calculated for the biome in search
     * @return The biome, guaranteed not to be null
     */
    @Redirect(
        method = "generateHeightmap",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/gen/ChunkGeneratorOverworld;biomesForGeneration:[Lnet/minecraft/world/biome/Biome;",
            args = "array=get"
        ),
        slice = @Slice(
            from = @At( // Target the upper most field modification that isn't duplicated (like maxnoisewhatever)
                value = "FIELD",
                target = "Lnet/minecraft/world/gen/ChunkGeneratorSettings;heightScale:F"
            ),
            to = @At(
                value = "FIELD", // And then target the second field access right after the second array access that we encounter.
                target = "Lnet/minecraft/world/gen/ChunkGeneratorSettings;biomeDepthOffSet:F"
            )
        )
    )
    private Biome impl$ensureNonNullBiomes(final Biome[] array, final int index) {
        Biome biome = array[index];
        if (biome == null) {
            // TODO - maybe log the issue to better find out who is storing nulls?
            // Only problem with this being set like this is the possibility of setting different biomes
            // than the biomes for the neighboring blocks. Of course, that point is invalidated by the fact that
            // the biome retrieved from the generated biome array is null.
            biome = array[index] = Biomes.PLAINS; // At the very least, avoid future nullability issues, strange world generation modifications that are occuring if this is reached.
        }
        return biome;
    }

}
