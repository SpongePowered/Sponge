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

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeHell;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.weighted.ChanceTable;
import org.spongepowered.api.util.weighted.EmptyObject;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.world.gen.populator.Glowstone;
import org.spongepowered.api.world.gen.populator.NetherFire;
import org.spongepowered.api.world.gen.populator.Ore;
import org.spongepowered.api.world.gen.populator.RandomBlock;
import org.spongepowered.api.world.gen.type.MushroomType;
import org.spongepowered.api.world.gen.type.MushroomTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.world.biome.SpongeBiomeGenerationSettings;
import org.spongepowered.common.world.gen.WorldGenConstants;
import org.spongepowered.common.world.gen.populators.HellMushroomPopulator;

@Mixin(BiomeHell.class)
public abstract class BiomeHellMixin extends BiomeMixin {

    @Override
    public void bridge$buildPopulators(World world, SpongeBiomeGenerationSettings gensettings) {
        RandomBlock lava1 = RandomBlock.builder()
                .block((BlockState) Blocks.FLOWING_LAVA.getDefaultState())
                .perChunk(8)
                .height(VariableAmount.baseWithRandomAddition(4, 120))
                .placementTarget(WorldGenConstants.HELL_LAVA)
                .build();
        gensettings.getPopulators().add(lava1);

        NetherFire fire = NetherFire.builder()
                .perChunk(VariableAmount.baseWithRandomAddition(1, VariableAmount.baseWithRandomAddition(1, 10)))
                .perCluster(64)
                .build();
        gensettings.getPopulators().add(fire);

        Glowstone glowstone1 = Glowstone.builder()
                .blocksPerCluster(1500)
                .clusterHeight(VariableAmount.baseWithRandomAddition(-11, 12))
                .perChunk(VariableAmount.baseWithRandomAddition(0, VariableAmount.baseWithRandomAddition(1, 10)))
                .height(VariableAmount.baseWithRandomAddition(4, 120))
                .build();
        gensettings.getPopulators().add(glowstone1);

        Glowstone glowstone2 = Glowstone.builder()
                .blocksPerCluster(1500)
                .clusterHeight(VariableAmount.baseWithRandomAddition(0, 12))
                .perChunk(10)
                .height(VariableAmount.baseWithRandomAddition(0, 128))
                .build();
        gensettings.getPopulators().add(glowstone2);

        ChanceTable<MushroomType> types = new ChanceTable<>();
        types.add(new WeightedObject<>(MushroomTypes.BROWN, 1));
        types.add(new EmptyObject<>(1));
        HellMushroomPopulator smallMushroom = new HellMushroomPopulator();
        smallMushroom.setMushroomsPerChunk(1);
        smallMushroom.getTypes().addAll(types);
        gensettings.getPopulators().add(smallMushroom);

        ChanceTable<MushroomType> types2 = new ChanceTable<>();
        types.add(new WeightedObject<>(MushroomTypes.RED, 1));
        types.add(new EmptyObject<>(1));
        HellMushroomPopulator smallMushroom2 = new HellMushroomPopulator();
        smallMushroom2.setMushroomsPerChunk(1);
        smallMushroom2.getTypes().addAll(types2);
        gensettings.getPopulators().add(smallMushroom2);

        Ore quartz = Ore.builder()
                .height(VariableAmount.baseWithRandomAddition(10, 108))
                .ore(BlockTypes.QUARTZ_ORE.getDefaultState())
                .perChunk(16)
                .placementCondition((o) -> o != null && o.getType() == BlockTypes.NETHERRACK)
                .size(14)
                .build();
        gensettings.getPopulators().add(quartz);

        int halfSeaLevel = world.getSeaLevel() / 2 + 1;
        Ore magma = Ore.builder()
                .height(VariableAmount.baseWithRandomAddition(halfSeaLevel - 5, 10))
                .ore(BlockTypes.MAGMA.getDefaultState())
                .perChunk(4)
                .placementCondition((o) -> o != null && o.getType() == BlockTypes.NETHERRACK)
                .size(33)
                .build();
        gensettings.getPopulators().add(magma);

        RandomBlock lava2 = RandomBlock.builder()
                .block((BlockState) Blocks.FLOWING_LAVA.getDefaultState())
                .perChunk(16)
                .height(VariableAmount.baseWithRandomAddition(10, 108))
                .placementTarget(WorldGenConstants.HELL_LAVA_ENCLOSED)
                .build();
        gensettings.getPopulators().add(lava2);
    }
}
