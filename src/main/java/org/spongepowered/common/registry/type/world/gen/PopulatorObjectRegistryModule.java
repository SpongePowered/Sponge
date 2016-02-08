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
package org.spongepowered.common.registry.type.world.gen;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenDesertWells;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.common.interfaces.world.gen.IMixinWorldGenPopulatorObject;
import org.spongepowered.common.interfaces.world.gen.IWorldGenTrees;
import org.spongepowered.common.registry.type.MutableCatalogRegistryModule;

public class PopulatorObjectRegistryModule extends MutableCatalogRegistryModule<PopulatorObject> {

    @Override
    public void registerDefaults() {
        // Populators
        registerUnsafe(new WorldGenDesertWells(), "desert_well");

        // Trees
        registerUnsafe(new WorldGenTrees(false), "oak");
        registerUnsafe(new WorldGenBigTree(false), "mega_oak");
        registerUnsafe(new WorldGenForest(false, false), "birch");
        registerUnsafe(new WorldGenForest(false, true), "mega_birch");
        registerUnsafe(new WorldGenTaiga2(false), "tall_taiga");
        registerUnsafe(new WorldGenTaiga1(), "pointy_taiga");
        registerUnsafe(new WorldGenMegaPineTree(false, true), "mega_tall_taiga");
        registerUnsafe(new WorldGenMegaPineTree(false, false), "mega_pointy_taiga");
        IBlockState jlog = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
        IBlockState jleaf = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
        IBlockState leaf = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
        IWorldGenTrees trees = (IWorldGenTrees) new WorldGenTrees(false, 4, jlog, jleaf, true);
        trees.setMinHeight(VariableAmount.baseWithRandomAddition(4, 7));
        register(trees, "jungle");
        registerUnsafe(new WorldGenMegaJungle(false, 10, 20, jlog, jleaf), "mega_jungle");
        WorldGenShrub bush = new WorldGenShrub(jlog, leaf);
        registerUnsafe(bush, "jungle_bush");
        registerUnsafe(new WorldGenSavannaTree(false), "savanna");
        registerUnsafe(new WorldGenCanopyTree(false), "canopy");
        registerUnsafe(new WorldGenSwamp(), "swamp");

        // Mushrooms
        registerUnsafe(new WorldGenBigMushroom(Blocks.brown_mushroom), "brown");
        registerUnsafe(new WorldGenBigMushroom(Blocks.red_mushroom), "red");
    }

    @Override
    protected void register(PopulatorObject type, String... aliases) {
        checkNotNull(type, "type");
        checkNotNull(aliases, "aliases");
        if (type.getId() == null) {
            if (aliases.length > 0 && type instanceof IMixinWorldGenPopulatorObject) {
                ((IMixinWorldGenPopulatorObject) type).setId(aliases[0]);
            } else {
                throw new IllegalArgumentException("PopulatorObject " + type + " does not have an id!");
            }
        }
        super.register(type, aliases);
    }

}
