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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockPlanks;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenForest;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTrees;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.type.BiomeTreeType;
import org.spongepowered.api.world.gen.type.BiomeTreeTypes;
import org.spongepowered.common.interfaces.world.gen.IWorldGenTrees;
import org.spongepowered.common.world.gen.type.SpongeBiomeTreeType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BiomeTreeTypeRegistryModule implements CatalogRegistryModule<BiomeTreeType> {

    @RegisterCatalog(BiomeTreeTypes.class)
    private final Map<String, BiomeTreeType> biomeTreeTypeMappings = new HashMap<>();

    @Override
    public Optional<BiomeTreeType> getById(String id) {
        return Optional.ofNullable(this.biomeTreeTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<BiomeTreeType> getAll() {
        return ImmutableList.copyOf(this.biomeTreeTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.biomeTreeTypeMappings.put("oak", new SpongeBiomeTreeType("oak", (PopulatorObject) new WorldGenTrees(false), (PopulatorObject) new WorldGenBigTree(false)));
        this.biomeTreeTypeMappings.put("birch", new SpongeBiomeTreeType("birch", (PopulatorObject) new WorldGenForest(false, false), (PopulatorObject) new WorldGenForest(false, true)));

        WorldGenMegaPineTree tall_megapine = new WorldGenMegaPineTree(false, true);
        WorldGenMegaPineTree megapine = new WorldGenMegaPineTree(false, false);

        this.biomeTreeTypeMappings.put("tall_taiga", new SpongeBiomeTreeType("tall_taiga", (PopulatorObject) new WorldGenTaiga2(false), (PopulatorObject) tall_megapine));
        this.biomeTreeTypeMappings.put("pointy_taiga",
                                       new SpongeBiomeTreeType("pointy_taiga", (PopulatorObject) new WorldGenTaiga1(), (PopulatorObject) megapine));

        IWorldGenTrees trees = (IWorldGenTrees) new WorldGenTrees(false, 4, BlockPlanks.EnumType.JUNGLE.getMetadata(), BlockPlanks.EnumType.JUNGLE.getMetadata(), true);
        trees.setMinHeight(VariableAmount.baseWithRandomAddition(4, 7));
        WorldGenMegaJungle mega = new WorldGenMegaJungle(false, 10, 20, BlockPlanks.EnumType.JUNGLE.getMetadata(), BlockPlanks.EnumType.JUNGLE.getMetadata());

        this.biomeTreeTypeMappings.put("jungle", new SpongeBiomeTreeType("jungle", (PopulatorObject) trees, (PopulatorObject) mega));

        WorldGenShrub bush = new WorldGenShrub(BlockPlanks.EnumType.JUNGLE.getMetadata(), BlockPlanks.EnumType.OAK.getMetadata());

        this.biomeTreeTypeMappings.put("jungle_bush", new SpongeBiomeTreeType("jungle_bush", (PopulatorObject) bush, null));
        this.biomeTreeTypeMappings.put("savanna", new SpongeBiomeTreeType("savanna", (PopulatorObject) new WorldGenSavannaTree(false), null));
        this.biomeTreeTypeMappings.put("canopy", new SpongeBiomeTreeType("canopy", (PopulatorObject) new WorldGenCanopyTree(false), null));
        this.biomeTreeTypeMappings.put("swamp", new SpongeBiomeTreeType("swamp", (PopulatorObject) new WorldGenSwamp(), null));
    }
}
