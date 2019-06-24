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
package org.spongepowered.common.mixin.core.world.biome;

import net.minecraft.block.BlockDirt;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeTaiga;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.type.DoublePlantTypes;
import org.spongepowered.api.data.type.ShrubType;
import org.spongepowered.api.data.type.ShrubTypes;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.gen.populator.BlockBlob;
import org.spongepowered.api.world.gen.populator.DoublePlant;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Shrub;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.WorldGenConstants;

import java.util.Iterator;

@Mixin(BiomeTaiga.class)
public abstract class BiomeTaigaMixin extends BiomeMixin {

    @Shadow @Final private BiomeTaiga.Type type;

    @Override
    public void bridge$buildPopulators(World world, SpongeBiomeGenerationSettings gensettings) {
        if (this.type == BiomeTaiga.Type.MEGA || this.type == BiomeTaiga.Type.MEGA_SPRUCE) {
            BlockBlob blob = BlockBlob.builder()
                    .blobCount(VariableAmount.baseWithRandomAddition(0, 3))
                    .block((BlockState) Blocks.MOSSY_COBBLESTONE.getDefaultState())
                    .radius(VariableAmount.baseWithRandomAddition(0, 2))
                    .build();
            gensettings.getPopulators().add(blob);
        }

        DoublePlant fern = DoublePlant.builder()
                .type(DoublePlantTypes.FERN, 1)
                .perChunk(7 * 5)
                .build();
        gensettings.getPopulators().add(fern);
        super.bridge$buildPopulators(world, gensettings);
        if (this.type == BiomeTaiga.Type.MEGA || this.type == BiomeTaiga.Type.MEGA_SPRUCE) {
            gensettings.getGroundCoverLayers().clear();
            gensettings.getGroundCoverLayers().add(new GroundCoverLayer((Double seed) -> {
                if (seed > 1.75D) {
                    return (BlockState) Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
                } else if (seed > -0.95D) {
                    return (BlockState) Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL);
                }
                return (BlockState) Blocks.GRASS.getDefaultState();

            } , WorldGenConstants.GROUND_COVER_DEPTH));
            gensettings.getGroundCoverLayers().add(new GroundCoverLayer((BlockState) this.fillerBlock, WorldGenConstants.GROUND_COVER_DEPTH));

        }
        BiomeDecorator theBiomeDecorator = this.decorator;
        for (Iterator<Shrub> it = gensettings.getPopulators(Shrub.class).iterator(); it.hasNext();) {
            Shrub next = it.next();
            if (next.getTypes().size() == 1) {
                TableEntry<ShrubType> entry = next.getTypes().getEntries().get(0);
                if (entry instanceof WeightedObject && ((WeightedObject<ShrubType>) entry).get() == ShrubTypes.TALL_GRASS) {
                    it.remove();
                }
            }
        }
        Shrub grass = Shrub.builder()
                .perChunk(theBiomeDecorator.grassPerChunk * 128)
                .type(ShrubTypes.FERN, 4)
                .type(ShrubTypes.TALL_GRASS, 1)
                .build();
        gensettings.getPopulators().add(grass);
        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        Forest.Builder forest = Forest.builder();
        forest.perChunk(VariableAmount.baseWithOptionalAddition(theBiomeDecorator.treesPerChunk, 1, 0.1));
        if (this.type == BiomeTaiga.Type.MEGA || this.type == BiomeTaiga.Type.MEGA_SPRUCE) {
            if (this.type == BiomeTaiga.Type.MEGA) {
                forest.type(BiomeTreeTypes.POINTY_TAIGA.getLargePopulatorObject().get(), 1);
                forest.type(BiomeTreeTypes.TALL_TAIGA.getLargePopulatorObject().get(), 12);
            } else {
                forest.type(BiomeTreeTypes.TALL_TAIGA.getLargePopulatorObject().get(), 13);
            }
            forest.type(BiomeTreeTypes.POINTY_TAIGA.getPopulatorObject(), 26 / 3d);
            forest.type(BiomeTreeTypes.TALL_TAIGA.getPopulatorObject(), 52 / 3d);
        } else {
            forest.type(BiomeTreeTypes.POINTY_TAIGA.getPopulatorObject(), 1);
            forest.type(BiomeTreeTypes.TALL_TAIGA.getPopulatorObject(), 2);
        }
        gensettings.getPopulators().add(0, forest.build());
    }
}
