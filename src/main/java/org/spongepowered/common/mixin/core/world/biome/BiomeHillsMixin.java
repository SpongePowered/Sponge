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

import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeHills;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.util.weighted.SeededVariableAmount;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.biome.GroundCoverLayer;
import org.spongepowered.api.world.gen.populator.Forest;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.WorldGenConstants;

@Mixin(BiomeHills.class)
public abstract class BiomeHillsMixin extends BiomeMixin {

    @Shadow @Final private BiomeHills.Type type;

    @Override
    public void bridge$buildPopulators(World world, SpongeBiomeGenerationSettings gensettings) {
        super.bridge$buildPopulators(world, gensettings);
        gensettings.getGroundCoverLayers().clear();
        gensettings.getGroundCoverLayers().add(new GroundCoverLayer((stoneNoise) -> {
            IBlockState result = Blocks.GRASS.getDefaultState();
            if ((stoneNoise < -1.0D || stoneNoise > 2.0D) && this.type == BiomeHills.Type.MUTATED) {
                result = Blocks.GRAVEL.getDefaultState();
            } else if (stoneNoise > 1.0D && this.type != BiomeHills.Type.EXTRA_TREES) {
                result = Blocks.STONE.getDefaultState();
            }
            return (BlockState) result;
        } , SeededVariableAmount.fixed(1)));
        gensettings.getGroundCoverLayers().add(new GroundCoverLayer((stoneNoise) -> {
            IBlockState result = Blocks.DIRT.getDefaultState();
            if ((stoneNoise < -1.0D || stoneNoise > 2.0D) && this.type == BiomeHills.Type.MUTATED) {
                result = Blocks.GRAVEL.getDefaultState();
            } else if (stoneNoise > 1.0D && this.type != BiomeHills.Type.EXTRA_TREES) {
                result = Blocks.STONE.getDefaultState();
            }
            return (BlockState) result;
        } , WorldGenConstants.GROUND_COVER_DEPTH));

        BiomeDecorator theBiomeDecorator = this.decorator;
        RandomBlock emerald = RandomBlock.builder()
                .block((BlockState) Blocks.EMERALD_ORE.getDefaultState())
                .placementTarget(WorldGenConstants.STONE_LOCATION)
                .perChunk(VariableAmount.baseWithRandomAddition(3, 6))
                .height(VariableAmount.baseWithRandomAddition(4, 28))
                .build();
        gensettings.getPopulators().add(emerald);

        Ore silverfish = Ore.builder()
                .ore((BlockState) Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE))
                .perChunk(7)
                .height(VariableAmount.baseWithRandomAddition(0, 64))
                .size(9)
                .build();
        gensettings.getPopulators().add(silverfish);

        gensettings.getPopulators().removeAll(gensettings.getPopulators(Forest.class));
        Forest.Builder forest = Forest.builder();
        forest.perChunk(VariableAmount.baseWithOptionalAddition(theBiomeDecorator.treesPerChunk, 1, 0.1));
        forest.type(BiomeTreeTypes.TALL_TAIGA.getPopulatorObject(), 20);
        forest.type(BiomeTreeTypes.OAK.getPopulatorObject(), 9);
        forest.type(BiomeTreeTypes.OAK.getLargePopulatorObject().get(), 1);
        gensettings.getPopulators().add(0, forest.build());
    }
}
