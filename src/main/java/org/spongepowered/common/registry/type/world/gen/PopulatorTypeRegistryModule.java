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

import com.google.common.collect.Maps;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenBigTree;
import net.minecraft.world.gen.feature.WorldGenBirchTree;
import net.minecraft.world.gen.feature.WorldGenBlockBlob;
import net.minecraft.world.gen.feature.WorldGeneratorBonusChest;
import net.minecraft.world.gen.feature.WorldGenBush;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenDesertWells;
import net.minecraft.world.gen.feature.WorldGenDoublePlant;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenEndIsland;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenFossils;
import net.minecraft.world.gen.feature.WorldGenGlowStone1;
import net.minecraft.world.gen.feature.WorldGenGlowStone2;
import net.minecraft.world.gen.feature.WorldGenHellLava;
import net.minecraft.world.gen.feature.WorldGenHugeTrees;
import net.minecraft.world.gen.feature.WorldGenIcePath;
import net.minecraft.world.gen.feature.WorldGenIceSpike;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.feature.WorldGenLiquids;
import net.minecraft.world.gen.feature.WorldGenMegaJungle;
import net.minecraft.world.gen.feature.WorldGenMegaPineTree;
import net.minecraft.world.gen.feature.WorldGenMelon;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenSand;
import net.minecraft.world.gen.feature.WorldGenSavannaTree;
import net.minecraft.world.gen.feature.WorldGenShrub;
import net.minecraft.world.gen.feature.WorldGenSpikes;
import net.minecraft.world.gen.feature.WorldGenSwamp;
import net.minecraft.world.gen.feature.WorldGenTaiga1;
import net.minecraft.world.gen.feature.WorldGenTaiga2;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenTrees;
import net.minecraft.world.gen.feature.WorldGenVines;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.registry.util.CustomCatalogRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.api.world.gen.PopulatorTypes;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.RegistryHelper;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;
import org.spongepowered.common.world.gen.SpongePopulatorType;

import java.util.Map;
import java.util.function.Function;

@RegisterCatalog(PopulatorTypes.class)
public final class PopulatorTypeRegistryModule extends AbstractCatalogRegistryModule<PopulatorType> implements AdditionalCatalogRegistryModule<PopulatorType> {

    public static PopulatorTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public final Map<Class<?>, PopulatorType> populatorClassToTypeMappings = Maps.newHashMap();

    public Function<Class<?>, PopulatorType> customTypeFunction;

    PopulatorTypeRegistryModule() {
        this.customTypeFunction = (type) -> {
            return new SpongePopulatorType(type.getSimpleName(), type.getName().contains("net.minecraft.") ? "minecraft" : "unknown");
        };
    }

    private void register(final PopulatorType populator, final Class<?>... classes) {
        this.register(populator);
        for (final Class<?> klass : classes) {
            this.populatorClassToTypeMappings.put(klass, populator);
        }
    }

    @Override
    public void registerDefaults() {
        this.register(new SpongePopulatorType("big_mushroom"), WorldGenBigMushroom.class);
        this.register(new SpongePopulatorType("block_blob"), WorldGenBlockBlob.class);
        this.register(new SpongePopulatorType("cactus"), WorldGenCactus.class);
        this.register(new SpongePopulatorType("chorus_flower"));
        this.register(new SpongePopulatorType("dead_bush"), WorldGenDeadBush.class);
        this.register(new SpongePopulatorType("desert_well"), WorldGenDesertWells.class);
        this.register(new SpongePopulatorType("double_plant"), WorldGenDoublePlant.class);
        this.register(new SpongePopulatorType("dungeon"), WorldGenDungeons.class);
        this.register(new SpongePopulatorType("end_island"), WorldGenEndIsland.class);
        this.register(new SpongePopulatorType("flower"), WorldGenFlowers.class);
        this.register(new SpongePopulatorType("forest"), WorldGenBigTree.class, WorldGenBirchTree.class, WorldGenHugeTrees.class, WorldGenMegaJungle.class, WorldGenMegaPineTree.class, WorldGenSavannaTree.class,
                WorldGenShrub.class, WorldGenSwamp.class, WorldGenTaiga1.class, WorldGenTaiga2.class, WorldGenTrees.class);
        this.register(new SpongePopulatorType("fossil"), WorldGenFossils.class);
        this.register(new SpongePopulatorType("glowstone"), WorldGenGlowStone1.class, WorldGenGlowStone2.class);
        this.register(new SpongePopulatorType("ice_path"), WorldGenIcePath.class);
        this.register(new SpongePopulatorType("ice_spike"), WorldGenIceSpike.class);
        this.register(new SpongePopulatorType("lake"), WorldGenLakes.class);
        this.register(new SpongePopulatorType("melon"), WorldGenMelon.class);
        this.register(new SpongePopulatorType("mushroom"), WorldGenBush.class);
        this.register(new SpongePopulatorType("nether_fire"));
        this.register(new SpongePopulatorType("ore"), WorldGenMinable.class);
        this.register(new SpongePopulatorType("pumpkin"), WorldGenPumpkin.class);
        this.register(new SpongePopulatorType("generic_block"), WorldGenFire.class, WorldGenHellLava.class, WorldGenLiquids.class);
        this.register(new SpongePopulatorType("generic_object"));
        this.register(new SpongePopulatorType("reed"), WorldGenReed.class);
        this.register(new SpongePopulatorType("sea_floor"), WorldGenClay.class, WorldGenSand.class);
        this.register(new SpongePopulatorType("shrub"), WorldGenTallGrass.class);
        this.register(new SpongePopulatorType("vine"), WorldGenVines.class);
        this.register(new SpongePopulatorType("water_lily"), WorldGenWaterlily.class);

        // internal
        this.register(new SpongePopulatorType("animal"));
        this.register(new SpongePopulatorType("bonus_chest"), WorldGeneratorBonusChest.class);
        this.register(new SpongePopulatorType("end_spike"));
        this.register(new SpongePopulatorType("ender_dragon"));
        this.register(new SpongePopulatorType("plains_grass"));
        this.register(new SpongePopulatorType("snow"));
        this.register(new SpongePopulatorType("structure"));
        this.register(new SpongePopulatorType("unknown"));

        this.populatorClassToTypeMappings.put(WorldGenSpikes.class, this.map.get(CatalogKey.minecraft("ender_crystal_platform")));
    }

    @Override
    public void registerAdditionalCatalog(PopulatorType extraCatalog) {
        checkNotNull(extraCatalog, "CatalogType cannot be null");
        checkArgument(!extraCatalog.getId().isEmpty(), "Id cannot be empty");
        this.register(extraCatalog);
    }

    public void registerClassMapping(Class<? extends net.minecraft.world.gen.feature.WorldGenerator> generator, PopulatorType type) {
        this.populatorClassToTypeMappings.put(generator, type);
    }

    @CustomCatalogRegistration
    public void registerCatalogs() {
        registerDefaults();

        final Map<String, PopulatorType> map = this.map.forCatalogRegistration();
        RegistryHelper.mapFields(PopulatorTypes.class, map);
        RegistryHelper.mapFields(InternalPopulatorTypes.class, map);
    }

    public boolean hasRegistrationFor(Class<?> cls) {
        return this.populatorClassToTypeMappings.containsKey(cls);
    }

    public PopulatorType getForClass(Class<?> cls) {
        return this.populatorClassToTypeMappings.get(cls);
    }

    public PopulatorType getOrCreateForType(Class<?> cls) {
        if (hasRegistrationFor(cls)) {
            return getForClass(cls);
        }
        PopulatorType type = this.customTypeFunction.apply(cls);
        if (type == null) {
            return InternalPopulatorTypes.UNKNOWN;
        }
        registerAdditionalCatalog(type);
        this.populatorClassToTypeMappings.put(cls, type);
        return type;
    }

    private static final class Holder {

        static final PopulatorTypeRegistryModule INSTANCE = new PopulatorTypeRegistryModule();
    }
}
