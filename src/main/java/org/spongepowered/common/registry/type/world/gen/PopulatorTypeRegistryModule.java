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
import com.google.common.collect.Maps;
import net.minecraft.world.gen.feature.WorldGenerator;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.registry.util.CustomCatalogRegistration;
import org.spongepowered.common.registry.util.RegisterCatalog;
import org.spongepowered.common.world.gen.SpongePopulatorType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class PopulatorTypeRegistryModule implements ExtraClassCatalogRegistryModule<PopulatorType, WorldGenerator> {

    public final Map<Class<? extends net.minecraft.world.gen.feature.WorldGenerator>, PopulatorType> populatorClassToTypeMappings = Maps.newHashMap();

    @RegisterCatalog(PopulatorTypes.class)
    protected final Map<String, PopulatorType> populatorTypeMappings = Maps.newHashMap();

    @Override
    public Optional<PopulatorType> getById(String id) {
        return Optional.ofNullable(this.populatorTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<PopulatorType> getAll() {
        return ImmutableList.copyOf(this.populatorTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        this.populatorTypeMappings
            .put("big_mushroom", new SpongePopulatorType("big_mushroom", net.minecraft.world.gen.feature.WorldGenBigMushroom.class));
        this.populatorTypeMappings.put("big_tree", new SpongePopulatorType("big_tree", net.minecraft.world.gen.feature.WorldGenBigTree.class));
        this.populatorTypeMappings.put("birch_tree", new SpongePopulatorType("birch_tree", net.minecraft.world.gen.feature.WorldGenForest.class));
        this.populatorTypeMappings.put("block_blob", new SpongePopulatorType("block_blob", net.minecraft.world.gen.feature.WorldGenBlockBlob.class));
        this.populatorTypeMappings
            .put("bonus_chest", new SpongePopulatorType("bonus_chest", net.minecraft.world.gen.feature.WorldGeneratorBonusChest.class));
        this.populatorTypeMappings.put("bush", new SpongePopulatorType("bush", net.minecraft.world.gen.GeneratorBushFeature.class));
        this.populatorTypeMappings.put("cactus", new SpongePopulatorType("cactus", net.minecraft.world.gen.feature.WorldGenCactus.class));
        this.populatorTypeMappings
            .put("canopy_tree", new SpongePopulatorType("canopy_tree", net.minecraft.world.gen.feature.WorldGenCanopyTree.class));
        this.populatorTypeMappings.put("clay", new SpongePopulatorType("clay", net.minecraft.world.gen.feature.WorldGenClay.class));
        this.populatorTypeMappings.put("dead_bush", new SpongePopulatorType("dead_bush", net.minecraft.world.gen.feature.WorldGenDeadBush.class));
        this.populatorTypeMappings
            .put("desert_well", new SpongePopulatorType("desert_well", net.minecraft.world.gen.feature.WorldGenDesertWells.class));
        this.populatorTypeMappings
            .put("double_plant", new SpongePopulatorType("double_plant", net.minecraft.world.gen.feature.WorldGenBigMushroom.class));
        this.populatorTypeMappings.put("dungeon", new SpongePopulatorType("dungeon", net.minecraft.world.gen.feature.WorldGenDungeons.class));
        this.populatorTypeMappings
            .put("ender_crystal_platform", new SpongePopulatorType("ender_crystal_platform", net.minecraft.world.gen.feature.WorldGenSpikes.class));
        this.populatorTypeMappings.put("fire", new SpongePopulatorType("fire", net.minecraft.world.gen.feature.WorldGenFire.class));
        this.populatorTypeMappings.put("flower", new SpongePopulatorType("flower", net.minecraft.world.gen.feature.WorldGenFlowers.class));
        this.populatorTypeMappings.put("glowstone", new SpongePopulatorType("glowstone", net.minecraft.world.gen.feature.WorldGenGlowStone1.class));
        this.populatorTypeMappings.put("glowstone2", new SpongePopulatorType("glowstone2", net.minecraft.world.gen.feature.WorldGenGlowStone2.class));
        this.populatorTypeMappings.put("ice_path", new SpongePopulatorType("ice_path", net.minecraft.world.gen.feature.WorldGenIcePath.class));
        this.populatorTypeMappings.put("ice_spike", new SpongePopulatorType("ice_spike", net.minecraft.world.gen.feature.WorldGenIceSpike.class));
        this.populatorTypeMappings
            .put("jungle_bush_tree", new SpongePopulatorType("jungle_bush_tree", net.minecraft.world.gen.feature.WorldGenShrub.class));
        this.populatorTypeMappings.put("lake", new SpongePopulatorType("lake", net.minecraft.world.gen.feature.WorldGenLakes.class));
        this.populatorTypeMappings.put("lava", new SpongePopulatorType("lava", net.minecraft.world.gen.feature.WorldGenHellLava.class));
        this.populatorTypeMappings.put("liquid", new SpongePopulatorType("liquid", net.minecraft.world.gen.feature.WorldGenLiquids.class));
        this.populatorTypeMappings
            .put("mega_jungle_tree", new SpongePopulatorType("mega_jungle_tree", net.minecraft.world.gen.feature.WorldGenMegaJungle.class));
        this.populatorTypeMappings
            .put("mega_pine_tree", new SpongePopulatorType("mega_pinge_tree", net.minecraft.world.gen.feature.WorldGenMegaPineTree.class));
        this.populatorTypeMappings.put("melon", new SpongePopulatorType("melon", net.minecraft.world.gen.feature.WorldGenMelon.class));
        this.populatorTypeMappings.put("ore", new SpongePopulatorType("ore", net.minecraft.world.gen.feature.WorldGenMinable.class));
        this.populatorTypeMappings
            .put("pointy_taiga_tree", new SpongePopulatorType("pointy_taiga_tree", net.minecraft.world.gen.feature.WorldGenTaiga1.class));
        this.populatorTypeMappings.put("pumpkin", new SpongePopulatorType("pumpkin", net.minecraft.world.gen.feature.WorldGenPumpkin.class));
        this.populatorTypeMappings.put("reed", new SpongePopulatorType("reed", net.minecraft.world.gen.feature.WorldGenReed.class));
        this.populatorTypeMappings.put("sand", new SpongePopulatorType("sand", net.minecraft.world.gen.feature.WorldGenSand.class));
        this.populatorTypeMappings
            .put("savanna_tree", new SpongePopulatorType("savanna_tree", net.minecraft.world.gen.feature.WorldGenSavannaTree.class));
        this.populatorTypeMappings.put("shrub", new SpongePopulatorType("shrub", net.minecraft.world.gen.feature.WorldGenTallGrass.class));
        this.populatorTypeMappings.put("swamp_tree", new SpongePopulatorType("swamp_tree", net.minecraft.world.gen.feature.WorldGenSwamp.class));
        this.populatorTypeMappings
            .put("tall_taiga_tree", new SpongePopulatorType("tall_taiga_tree", net.minecraft.world.gen.feature.WorldGenTaiga2.class));
        this.populatorTypeMappings.put("tree", new SpongePopulatorType("tree", net.minecraft.world.gen.feature.WorldGenTrees.class));
        this.populatorTypeMappings.put("vine", new SpongePopulatorType("vine", net.minecraft.world.gen.feature.WorldGenVines.class));
        this.populatorTypeMappings.put("water_lily", new SpongePopulatorType("water_lily", net.minecraft.world.gen.feature.WorldGenWaterlily.class));
    }

    @CustomCatalogRegistration
    public void onRegistration() {
        registerDefaults();
        RegistryHelper.mapFields(PopulatorTypes.class, fieldName -> {
            PopulatorType populatorType = this.populatorTypeMappings.get(fieldName.toLowerCase());
            if (populatorType != null) {
                this.populatorClassToTypeMappings.put(((SpongePopulatorType) populatorType).populatorClass, populatorType);
                // remove old mapping
                this.populatorTypeMappings.remove(fieldName.toLowerCase());
                // add new mapping with minecraft id
                this.populatorTypeMappings.put(((SpongePopulatorType) populatorType).getId(), populatorType);
            } else {
                SpongeImpl.getLogger().error("A populator is null at the moment! Populator: " + fieldName);
            }
            return populatorType;
        });
    }

    @Override
    public void registerAdditionalCatalog(PopulatorType extraCatalog) {
        this.populatorTypeMappings.put(extraCatalog.getId(), extraCatalog);
        this.populatorClassToTypeMappings.put(((SpongePopulatorType) extraCatalog).populatorClass, extraCatalog);
    }

    @Override
    public boolean hasRegistrationFor(Class<? extends WorldGenerator> mappedClass) {
        return this.populatorClassToTypeMappings.containsKey(mappedClass);
    }

    @Override
    public PopulatorType getForClass(Class<? extends WorldGenerator> clazz) {
        return this.populatorClassToTypeMappings.get(clazz);
    }
}
