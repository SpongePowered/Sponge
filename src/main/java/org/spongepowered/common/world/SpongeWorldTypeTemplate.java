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
package org.spongepowered.common.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKeyCodec;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.biome.IBiomeMagnifier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.world.biome.BiomeFinder;
import org.spongepowered.api.world.biome.BiomeFinders;
import org.spongepowered.api.world.WorldTypeEffect;
import org.spongepowered.api.world.WorldTypeEffects;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.accessor.world.DimensionTypeAccessor;
import org.spongepowered.common.registry.provider.BiomeFinderProvider;
import org.spongepowered.common.registry.provider.DimensionEffectProvider;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.common.util.SpongeMinecraftDayTime;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;

public final class SpongeWorldTypeTemplate extends AbstractResourceKeyed implements WorldTypeTemplate {

    public final WorldTypeEffect effect;
    public final BiomeFinder biomeFinder;
    @Nullable public final MinecraftDayTime fixedTime;
    public final ResourceKey infiniburn;

    public final boolean ultraWarm, natural, skylight, ceiling, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, createDragonFight;
    public final float ambientLight;
    public final int logicalHeight;
    public final double coordinateScale;

    private static final Codec<SpongeDataSection> SPONGE_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            ResourceLocation.CODEC.optionalFieldOf("biome_finder", new ResourceLocation("sponge", "default")).forGetter(v -> v.biomeFinder),
                            Codec.BOOL.optionalFieldOf("create_dragon_fight", Boolean.FALSE).forGetter(v -> v.createDragonFight)
                    )
                    .apply(r, SpongeDataSection::new)
            );

    public static final Codec<DimensionType> DIRECT_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            Codec.LONG.optionalFieldOf("fixed_time")
                                    .xmap(
                                            v -> v.map(OptionalLong::of).orElseGet(OptionalLong::empty),
                                            v -> v.isPresent() ? Optional.of(v.getAsLong()) : Optional.empty()
                                    )
                                    .forGetter(v -> ((DimensionTypeAccessor) v).accessor$fixedTime())
                            ,
                            Codec.BOOL.fieldOf("has_skylight").forGetter(DimensionType::hasSkyLight),
                            Codec.BOOL.fieldOf("has_ceiling").forGetter(DimensionType::hasCeiling),
                            Codec.BOOL.fieldOf("ultrawarm").forGetter(DimensionType::ultraWarm),
                            Codec.BOOL.fieldOf("natural").forGetter(DimensionType::natural),
                            Codec.doubleRange(1.0E-5F, 3.0E7D).fieldOf("coordinate_scale").forGetter(DimensionType::coordinateScale),
                            Codec.BOOL.fieldOf("piglin_safe").forGetter(DimensionType::piglinSafe),
                            Codec.BOOL.fieldOf("bed_works").forGetter(DimensionType::bedWorks),
                            Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(DimensionType::respawnAnchorWorks),
                            Codec.BOOL.fieldOf("has_raids").forGetter(DimensionType::hasRaids),
                            Codec.intRange(0, 256).fieldOf("logical_height").forGetter(DimensionType::logicalHeight),
                            ResourceLocation.CODEC.fieldOf("infiniburn").forGetter(v -> ((ITag.INamedTag<Block>)v.infiniburn()).getName()),
                            ResourceLocation.CODEC.fieldOf("effects").orElse((ResourceLocation) (Object) WorldTypeEffects.OVERWORLD.getKey()).forGetter(v -> ((DimensionTypeAccessor) v).accessor$effectsLocation()),
                            Codec.FLOAT.fieldOf("ambient_light").forGetter(v -> ((DimensionTypeAccessor) v).accessor$ambientLight()),
                            Codec.optionalField("_sponge", SpongeWorldTypeTemplate.SPONGE_CODEC).forGetter(v -> Optional.of(new SpongeDataSection((ResourceLocation) (Object) BiomeFinderProvider.INSTANCE.get((BiomeFinder) v.getBiomeZoomer()), v.createDragonFight())))
                    )
                    // *Chuckles* I'm in danger
                    .apply(r, (f1, f2, f3, f4, f5, f6, f7, f8, f9, f10, f11, f12, f13, f14, f15) -> {
                        final IBiomeMagnifier biomeMagnifier = f15.isPresent() ? (IBiomeMagnifier) BiomeFinderProvider.INSTANCE.get((ResourceKey) (Object) f15.get().biomeFinder) : ColumnFuzzedBiomeMagnifier.INSTANCE;
                        final boolean createDragonFight = f15.isPresent() ? f15.get().createDragonFight : false;

                        return DimensionTypeAccessor.invoker$construct(f1, f2, f3, f4, f5, f6, createDragonFight, f7, f8, f9, f10, f11,
                                biomeMagnifier, f12, f13, f14);
                    })
            );

    public static final Codec<Supplier<DimensionType>> CODEC = RegistryKeyCodec.create(Registry.DIMENSION_TYPE_REGISTRY, SpongeWorldTypeTemplate.DIRECT_CODEC);

    protected SpongeWorldTypeTemplate(final BuilderImpl builder) {
        super(builder.key);

        this.effect = builder.effect;
        this.fixedTime = builder.fixedTime;
        this.infiniburn = builder.infiniburn;
        this.ultraWarm = builder.scorching;
        this.natural = builder.natural;
        this.skylight = builder.skylight;
        this.ceiling = builder.ceiling;
        this.piglinSafe = builder.piglinSafe;
        this.bedWorks = builder.bedsUsable;
        this.respawnAnchorWorks = builder.respawnAnchorsUsable;
        this.hasRaids = builder.hasRaids;
        this.ambientLight = builder.ambientLighting;
        this.logicalHeight = builder.logicalHeight;
        this.coordinateScale = builder.coordinateMultiplier;

        // Sponge
        this.biomeFinder = builder.biomeFinder;
        this.createDragonFight = builder.createDragonFight;
    }

    public SpongeWorldTypeTemplate(final ResourceKey key, final DimensionType dimensionType) {
        super(key);

        final OptionalLong fixedTime = ((DimensionTypeAccessor) dimensionType).accessor$fixedTime();
        this.fixedTime = fixedTime.isPresent() ? new SpongeMinecraftDayTime(fixedTime.getAsLong()) : null;
        this.skylight = dimensionType.hasSkyLight();
        this.ceiling = dimensionType.hasCeiling();
        this.ultraWarm = dimensionType.ultraWarm();
        this.natural = dimensionType.natural();
        this.coordinateScale = dimensionType.coordinateScale();
        this.piglinSafe = dimensionType.piglinSafe();
        this.bedWorks = dimensionType.bedWorks();
        this.respawnAnchorWorks = dimensionType.respawnAnchorWorks();
        this.hasRaids = dimensionType.hasRaids();
        this.logicalHeight = dimensionType.logicalHeight();
        this.biomeFinder = (BiomeFinder) dimensionType.getBiomeZoomer();
        this.infiniburn = (ResourceKey) (Object) ((ITag.INamedTag<Block>) dimensionType.infiniburn()).getName();
        this.effect = DimensionEffectProvider.INSTANCE.get((ResourceKey) (Object) ((DimensionTypeAccessor) dimensionType).accessor$effectsLocation());
        this.ambientLight = ((DimensionTypeAccessor) dimensionType).accessor$ambientLight();
        this.createDragonFight = dimensionType.createDragonFight();
    }

    @Override
    public DataPackType type() {
        return DataPackTypes.DIMENSION_TYPE;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        // TODO
        throw new RuntimeException();
    }

    @Override
    public WorldTypeEffect effect() {
        return this.effect;
    }

    @Override
    public BiomeFinder biomeFinder() {
        return this.biomeFinder;
    }

    @Override
    public boolean scorching() {
        return this.ultraWarm;
    }

    @Override
    public boolean natural() {
        return this.natural;
    }

    @Override
    public double coordinateMultiplier() {
        return this.coordinateScale;
    }

    @Override
    public boolean hasSkylight() {
        return this.skylight;
    }

    @Override
    public boolean hasCeiling() {
        return this.ceiling;
    }

    @Override
    public float ambientLighting() {
        return this.ambientLight;
    }

    @Override
    public Optional<MinecraftDayTime> fixedTime() {
        return Optional.ofNullable(this.fixedTime);
    }

    @Override
    public boolean piglinSafe() {
        return this.piglinSafe;
    }

    @Override
    public boolean bedsUsable() {
        return this.bedWorks;
    }

    @Override
    public boolean respawnAnchorsUsable() {
        return this.respawnAnchorWorks;
    }

    @Override
    public boolean hasRaids() {
        return this.hasRaids;
    }

    @Override
    public int logicalHeight() {
        return this.logicalHeight;
    }

    @Override
    public boolean createDragonFight() {
        return this.createDragonFight;
    }

    private static final class SpongeDataSection {
        private final ResourceLocation biomeFinder;
        private final boolean createDragonFight;

        public SpongeDataSection(final ResourceLocation biomeFinder, final boolean createDragonFight) {
            this.biomeFinder = biomeFinder;
            this.createDragonFight = createDragonFight;
        }
    }

    public static final class FactoryImpl implements WorldTypeTemplate.Factory {

        private static final SpongeWorldTypeTemplate
                OVERWORLD = new SpongeWorldTypeTemplate(ResourceKey.minecraft("overworld"), DimensionTypeAccessor.accessor$DEFAULT_OVERWORLD());

        private static final SpongeWorldTypeTemplate
                THE_NETHER = new SpongeWorldTypeTemplate(ResourceKey.minecraft("the_nether"), DimensionTypeAccessor.accessor$DEFAULT_NETHER());

        private static final SpongeWorldTypeTemplate
                THE_END = new SpongeWorldTypeTemplate(ResourceKey.minecraft("the_end"), DimensionTypeAccessor.accessor$DEFAULT_END());

        @Override
        public WorldTypeTemplate overworld() {
            return FactoryImpl.OVERWORLD;
        }

        @Override
        public WorldTypeTemplate theNether() {
            return FactoryImpl.THE_NETHER;
        }

        @Override
        public WorldTypeTemplate theEnd() {
            return FactoryImpl.THE_END;
        }
    }

    public static final class BuilderImpl extends AbstractResourceKeyedBuilder<WorldTypeTemplate, Builder> implements WorldTypeTemplate.Builder {

        protected WorldTypeEffect effect = WorldTypeEffects.OVERWORLD;
        protected MinecraftDayTime fixedTime;
        protected BiomeFinder biomeFinder = BiomeFinders.DEFAULT;
        protected ResourceKey infiniburn = (ResourceKey) (Object) BlockTags.INFINIBURN_OVERWORLD.getName();

        protected boolean scorching, natural, skylight, ceiling, piglinSafe, bedsUsable, respawnAnchorsUsable, hasRaids, createDragonFight;
        protected float ambientLighting;
        protected int logicalHeight;
        protected double coordinateMultiplier;

        @Override
        public WorldTypeTemplate.Builder effect(final WorldTypeEffect effect) {
            Objects.requireNonNull(effect, "effect");
            this.effect = effect;
            return this;
        }

        @Override
        public Builder biomeFinder(final BiomeFinder biomeFinder) {
            this.biomeFinder = Objects.requireNonNull(biomeFinder, "biomeFinder");
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder scorching(final boolean scorching) {
            this.scorching = scorching;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder natural(final boolean natural) {
            this.natural = natural;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder coordinateMultiplier(final double coordinateMultiplier) {
            this.coordinateMultiplier = coordinateMultiplier;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder hasSkylight(final boolean skylight) {
            this.skylight = skylight;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder hasCeiling(final boolean ceiling) {
            this.ceiling = ceiling;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder ambientLighting(final float ambientLighting) {
            this.ambientLighting = ambientLighting;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder fixedTime(@Nullable final MinecraftDayTime fixedTime) {
            this.fixedTime = fixedTime;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder piglinSafe(final boolean piglinSafe) {
            this.piglinSafe = piglinSafe;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder bedsUsable(final boolean bedsUsable) {
            this.bedsUsable = bedsUsable;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder respawnAnchorsUsable(final boolean respawnAnchorsUsable) {
            this.respawnAnchorsUsable = respawnAnchorsUsable;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder hasRaids(final boolean hasRaids) {
            this.hasRaids = hasRaids;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder logicalHeight(final int logicalHeight) {
            this.logicalHeight = logicalHeight;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder createDragonFight(final boolean spawnDragonFight) {
            this.createDragonFight = spawnDragonFight;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder from(final WorldTypeTemplate value) {
            Objects.requireNonNull(value, "value");

            this.effect = value.effect();
            this.biomeFinder = value.biomeFinder();
            this.fixedTime = value.fixedTime().orElse(null);
            this.infiniburn = (ResourceKey) (Object) BlockTags.INFINIBURN_OVERWORLD.getName();
            this.scorching = value.scorching();
            this.natural = value.natural();
            this.skylight = value.hasSkylight();
            this.ceiling = value.hasCeiling();
            this.piglinSafe = value.piglinSafe();
            this.bedsUsable = value.bedsUsable();
            this.respawnAnchorsUsable = value.respawnAnchorsUsable();
            this.hasRaids = value.hasRaids();
            this.ambientLighting = value.ambientLighting();
            this.logicalHeight = value.logicalHeight();
            this.coordinateMultiplier = value.coordinateMultiplier();
            this.createDragonFight = value.createDragonFight();
            return this;
        }

        @Override
        public @NonNull WorldTypeTemplate build0() {
            return new SpongeWorldTypeTemplate(this);
        }
    }
}
