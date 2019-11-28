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

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.gen.feature.BigTreeFeature;
import net.minecraft.world.gen.feature.BirchTreeFeature;
import net.minecraft.world.gen.feature.CanopyTreeFeature;
import net.minecraft.world.gen.feature.DesertWellsFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.MegaJungleFeature;
import net.minecraft.world.gen.feature.MegaPineTree;
import net.minecraft.world.gen.feature.PointyTaigaTreeFeature;
import net.minecraft.world.gen.feature.SavannaTreeFeature;
import net.minecraft.world.gen.feature.ShrubFeature;
import net.minecraft.world.gen.feature.SwampTreeFeature;
import net.minecraft.world.gen.feature.TallTaigaTreeFeature;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.util.weighted.VariableAmount;
import org.spongepowered.api.world.gen.PopulatorObject;
import org.spongepowered.api.world.gen.PopulatorObjects;
import org.spongepowered.common.bridge.world.gen.WorldGenTreesBridge;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

@RegisterCatalog(PopulatorObjects.class)
public class PopulatorObjectRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<PopulatorObject>
    implements AlternateCatalogRegistryModule<PopulatorObject>, AdditionalCatalogRegistryModule<PopulatorObject> {


    public PopulatorObjectRegistryModule() {
        super("minecraft");
    }

    @Override
    public void registerAdditionalCatalog(PopulatorObject extraCatalog) {
        checkNotNull(extraCatalog, "CatalogType cannot be null");
        checkArgument(!extraCatalog.getId().isEmpty(), "Id cannot be empty");
        checkArgument(!this.catalogTypeMap.containsKey(extraCatalog.getId()), "Duplicate Id");
        this.catalogTypeMap.put(extraCatalog.getId(), extraCatalog);
    }

    @Override
    public void registerDefaults() {
        // Populators
        register(new DesertWellsFeature());

        // Trees
        register(new TreeFeature(false));
        register(new BigTreeFeature(false));
        register(new BirchTreeFeature(false, false));
        register(new BirchTreeFeature(false, true));
        register(new TallTaigaTreeFeature(false));
        register(new PointyTaigaTreeFeature());
        register(new MegaPineTree(false, true));
        register(new MegaPineTree(false, false));
        IBlockState jlog = Blocks.field_150364_r.func_176223_P().func_177226_a(BlockOldLog.field_176301_b, BlockPlanks.EnumType.JUNGLE);
        IBlockState jleaf = Blocks.field_150362_t.func_176223_P().func_177226_a(BlockOldLeaf.field_176239_P, BlockPlanks.EnumType.JUNGLE).func_177226_a(BlockLeaves.field_176236_b, Boolean.valueOf(false));
        IBlockState leaf = Blocks.field_150362_t.func_176223_P().func_177226_a(BlockOldLeaf.field_176239_P, BlockPlanks.EnumType.JUNGLE).func_177226_a(BlockLeaves.field_176236_b, Boolean.valueOf(false));
        WorldGenTreesBridge trees = (WorldGenTreesBridge) new TreeFeature(false, 4, jlog, jleaf, true);
        trees.bridge$setId("minecraft:jungle");
        trees.bridge$setName("Jungle tree");
        trees.bridge$setMinHeight(VariableAmount.baseWithRandomAddition(4, 7));
        register((TreeFeature) trees);
        register(new MegaJungleFeature(false, 10, 20, jlog, jleaf));
        ShrubFeature bush = new ShrubFeature(jlog, leaf);
        register(bush);
        register( new SavannaTreeFeature(false));
        register(new CanopyTreeFeature(false));
        register(new SwampTreeFeature());

        // Mushrooms
        register(new WorldGenBigMushroom(Blocks.field_150420_aW));
        register(new WorldGenBigMushroom(Blocks.field_150419_aX));
    }

    private void register(Feature worldGenerator) {
        register((PopulatorObject) worldGenerator);
    }

}
