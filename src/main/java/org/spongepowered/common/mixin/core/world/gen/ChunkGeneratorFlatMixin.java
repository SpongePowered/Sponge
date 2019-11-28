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

import net.minecraft.block.Blocks;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGeneratorInfo;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.feature.Structure;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.biome.BiomeGenerationSettings;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.GenerationPopulator;
import org.spongepowered.api.world.gen.Populator;
import org.spongepowered.api.world.gen.WorldGenerator;
import org.spongepowered.api.world.gen.populator.Dungeon;
import org.spongepowered.api.world.gen.populator.Lake;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.gen.PopulatorProviderBridge;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.gen.populators.FilteredPopulator;

import java.util.Map;

@Mixin(FlatChunkGenerator.class)
public class ChunkGeneratorFlatMixin implements  PopulatorProviderBridge {

    @Shadow @Final private net.minecraft.block.BlockState[] cachedBlockIDs;
    @Shadow @Final private Map<String, Structure> structureGenerators;
    @Shadow @Final private boolean hasDecoration;
    @Shadow @Final private boolean hasDungeons;
    @Shadow @Final private FlatGeneratorInfo flatWorldGenInfo;

    @Override
    public void bridge$addPopulators(final WorldGenerator generator) {
        for (final Object o : this.structureGenerators.values()) {
            if (o instanceof MapGenBase) {
                generator.getGenerationPopulators().add((GenerationPopulator) o);
                if (o instanceof Structure) {
                    generator.getPopulators().add((Populator) o);
                }
            }
        }

        if (this.flatWorldGenInfo.func_82644_b().containsKey("lake")) {
            final Lake lake = Lake.builder()
                    .chance(1 / 4d)
                    .liquidType((BlockState) Blocks.field_150355_j.func_176223_P())
                    .height(VariableAmount.baseWithRandomAddition(0, 256))
                    .build();
            final FilteredPopulator filtered = new FilteredPopulator(lake);
            filtered.setRequiredFlags(WorldGenConstants.VILLAGE_FLAG);
            generator.getPopulators().add(lake);
        }

        if (this.flatWorldGenInfo.func_82644_b().containsKey("lava_lake")) {
            final Lake lake = Lake.builder()
                    .chance(1 / 8d)
                    .liquidType((BlockState) Blocks.field_150355_j.func_176223_P())
                    .height(VariableAmount.baseWithVariance(0,
                            VariableAmount.baseWithRandomAddition(8, VariableAmount.baseWithOptionalAddition(55, 193, 0.1))))
                    .build();
            final FilteredPopulator filtered = new FilteredPopulator(lake);
            filtered.setRequiredFlags(WorldGenConstants.VILLAGE_FLAG);
            generator.getPopulators().add(filtered);
        }

        if (this.hasDungeons) {
            final Dungeon dungeon = Dungeon.builder()
                    .attempts(8)
                    .build();
            generator.getPopulators().add(dungeon);
        }

        for (final BiomeType type : Sponge.getRegistry().getAllOf(BiomeType.class)) {
            final BiomeGenerationSettings settings = generator.getBiomeSettings(type);
            settings.getGroundCoverLayers().clear();
            if (!this.hasDecoration) {
                settings.getPopulators().clear();
                settings.getGenerationPopulators().clear();
            }
        }
    }

}
