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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;

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
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorObjects;
import org.spongepowered.common.interfaces.world.gen.IWorldGenTrees;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PopulatorObjectRegistryModule implements AdditionalCatalogRegistryModule<PopulatorObject> {

    @RegisterCatalog(PopulatorObjects.class) private final Map<String, PopulatorObject> populatorObjectMappings = new HashMap<>();

    @Override
    public Optional<PopulatorObject> getById(String id) {
        return Optional.ofNullable(this.populatorObjectMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<PopulatorObject> getAll() {
        return ImmutableList.copyOf(this.populatorObjectMappings.values());
    }

    @Override
    public void registerAdditionalCatalog(PopulatorObject extraCatalog) {
        checkNotNull(extraCatalog, "CatalogType cannot be null");
        checkArgument(!extraCatalog.getId().isEmpty(), "Id cannot be empty");
        checkArgument(!this.populatorObjectMappings.containsKey(extraCatalog.getId()), "Duplicate Id");
        this.populatorObjectMappings.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        // Populators
        this.populatorObjectMappings.put("desert_well", (PopulatorObject) new WorldGenDesertWells());

        // Trees
        this.populatorObjectMappings.put("oak", (PopulatorObject) new WorldGenTrees(false));
        this.populatorObjectMappings.put("mega_oak", (PopulatorObject) new WorldGenBigTree(false));
        this.populatorObjectMappings.put("birch", (PopulatorObject) new WorldGenForest(false, false));
        this.populatorObjectMappings.put("mega_birch", (PopulatorObject) new WorldGenForest(false, true));
        this.populatorObjectMappings.put("tall_taiga", (PopulatorObject) new WorldGenTaiga2(false));
        this.populatorObjectMappings.put("pointy_taiga", (PopulatorObject) new WorldGenTaiga1());
        this.populatorObjectMappings.put("mega_tall_taiga", (PopulatorObject) new WorldGenMegaPineTree(false, true));
        this.populatorObjectMappings.put("mega_pointy_taiga", (PopulatorObject) new WorldGenMegaPineTree(false, false));
        IBlockState jlog = Blocks.log.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
        IBlockState jleaf = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
        IBlockState leaf = Blocks.leaves.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
        IWorldGenTrees trees = (IWorldGenTrees) new WorldGenTrees(false, 4, jlog, jleaf, true);
        trees.setMinHeight(VariableAmount.baseWithRandomAddition(4, 7));
        this.populatorObjectMappings.put("jungle", (PopulatorObject) trees);
        this.populatorObjectMappings.put("mega_jungle", (PopulatorObject) new WorldGenMegaJungle(false, 10, 20, jlog, jleaf));
        WorldGenShrub bush = new WorldGenShrub(jlog, leaf);
        this.populatorObjectMappings.put("jungle_bush", (PopulatorObject) bush);
        this.populatorObjectMappings.put("savanna", (PopulatorObject) new WorldGenSavannaTree(false));
        this.populatorObjectMappings.put("canopy", (PopulatorObject) new WorldGenCanopyTree(false));
        this.populatorObjectMappings.put("swamp", (PopulatorObject) new WorldGenSwamp());

        // Mushrooms
        this.populatorObjectMappings.put("brown", (PopulatorObject) new WorldGenBigMushroom(Blocks.brown_mushroom));
        this.populatorObjectMappings.put("red", (PopulatorObject) new WorldGenBigMushroom(Blocks.red_mushroom));
    }
}
