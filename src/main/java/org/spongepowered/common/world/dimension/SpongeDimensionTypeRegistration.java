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
package org.spongepowered.common.world.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.FuzzedBiomeMagnifier;
import net.minecraft.world.biome.IBiomeMagnifier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.biome.BiomeFinder;
import org.spongepowered.api.world.biome.BiomeFinders;
import org.spongepowered.api.world.dimension.DimensionEffect;
import org.spongepowered.api.world.dimension.DimensionEffects;
import org.spongepowered.api.world.dimension.DimensionTypeRegistration;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.accessor.world.DimensionTypeAccessor;
import org.spongepowered.common.registry.provider.BiomeFinderProvider;
import org.spongepowered.common.registry.provider.DimensionEffectProvider;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.common.util.SpongeMinecraftDayTime;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

public final class SpongeDimensionTypeRegistration extends AbstractResourceKeyed implements DimensionTypeRegistration {

    public final DimensionEffect effect;
    public final BiomeFinder biomeFinder;
    public final MinecraftDayTime fixedTime;
    public final ResourceKey infiniburn;

    public final boolean ultraWarm, natural, skylight, ceiling, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, spawnDragonFight;
    public final float ambientLight;
    public final int logicalHeight;
    public final double coordinateScale;

    private static final Codec<SpongeDataSection> EXTENDED_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            ResourceLocation.CODEC.optionalFieldOf("biome_finder", new ResourceLocation("sponge", "default")).forGetter(v -> v.biomeFinder),
                            Codec.BOOL.optionalFieldOf("spawn_dragon_fight", Boolean.FALSE).forGetter(v -> v.spawnDragonFight)
                    )
                    .apply(r, SpongeDataSection::new)
            );

    public static final Codec<SpongeDimensionTypeRegistration> DIRECT_CODEC = RecordCodecBuilder
            .create(r -> r
                    .group(
                            Codec.LONG.optionalFieldOf("fixed_time")
                                    .xmap(
                                            v -> v.map(OptionalLong::of).orElseGet(OptionalLong::empty),
                                            v -> v.isPresent() ? Optional.of(v.getAsLong()) : Optional.empty()
                                    )
                                    .forGetter(v -> {
                                        if (v.fixedTime == null) {
                                            return OptionalLong.empty();
                                        }

                                        return OptionalLong.of(v.fixedTime.asTicks().getTicks());
                                    })
                            ,
                            Codec.BOOL.fieldOf("has_skylight").forGetter(v -> v.skylight),
                            Codec.BOOL.fieldOf("has_ceiling").forGetter(v -> v.ceiling),
                            Codec.BOOL.fieldOf("ultrawarm").forGetter(v -> v.ultraWarm),
                            Codec.BOOL.fieldOf("natural").forGetter(v -> v.natural),
                            Codec.doubleRange(1.0E-5F, 3.0E7D).fieldOf("coordinate_scale").forGetter(v -> v.coordinateScale),
                            Codec.BOOL.fieldOf("piglin_safe").forGetter(v -> v.piglinSafe),
                            Codec.BOOL.fieldOf("bed_works").forGetter(v -> v.bedWorks),
                            Codec.BOOL.fieldOf("respawn_anchor_works").forGetter(v -> v.respawnAnchorWorks),
                            Codec.BOOL.fieldOf("has_raids").forGetter(v -> v.hasRaids),
                            Codec.intRange(0, 256).fieldOf("logical_height").forGetter(v -> v.logicalHeight),
                            ResourceLocation.CODEC.fieldOf("infiniburn").forGetter(v -> (ResourceLocation) (Object) v.infiniburn),
                            ResourceLocation.CODEC.fieldOf("effects").orElse((ResourceLocation) (Object) DimensionEffects.OVERWORLD.getKey()).forGetter(v -> (ResourceLocation) (Object) v.effect.getKey()),
                            Codec.FLOAT.fieldOf("ambient_light").forGetter(v -> v.ambientLight),
                            Codec.optionalField("_sponge", SpongeDimensionTypeRegistration.EXTENDED_CODEC).forGetter(v -> Optional.of(new SpongeDataSection((ResourceLocation) (Object) BiomeFinderProvider.INSTANCE.get(v.biomeFinder), v.spawnDragonFight)))
                    )
                    .apply(r,  SpongeDimensionTypeRegistration::new)
            );

    protected SpongeDimensionTypeRegistration(final BuilderImpl builder) {
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
        this.spawnDragonFight = builder.spawnDragonFight;
    }

    // Codec constructor
    public SpongeDimensionTypeRegistration(final OptionalLong fixedTime, final boolean hasSkyLight, final boolean hasCeiling,
            final boolean ultraWarm, final boolean natural, final double coordinateScale, final boolean piglinSafe, final boolean bedWorks,
            final boolean respawnAnchorWorks, final boolean hasRaids, final int logicalHeight, final ResourceLocation infiniburn,
            final ResourceLocation effectsLocation, final float ambientLight, final Optional<SpongeDataSection> spongeDataSection) {
        super(null);
        this.fixedTime = fixedTime.isPresent() ? MinecraftDayTime.of(Sponge.getServer(), Ticks.of(fixedTime.getAsLong())) : null;
        this.skylight = hasSkyLight;
        this.ceiling = hasCeiling;
        this.ultraWarm = ultraWarm;
        this.natural = natural;
        this.coordinateScale = coordinateScale;
        this.piglinSafe = piglinSafe;
        this.bedWorks = bedWorks;
        this.respawnAnchorWorks = respawnAnchorWorks;
        this.hasRaids = hasRaids;
        this.logicalHeight = logicalHeight;
        this.infiniburn = (ResourceKey) (Object) infiniburn;
        this.effect = DimensionEffectProvider.INSTANCE.get((ResourceKey) (Object) effectsLocation);
        this.ambientLight = ambientLight;
        final SpongeDataSection sds = spongeDataSection.orElse(null);
        if (sds == null) {
            this.biomeFinder = (BiomeFinder) (Object) FuzzedBiomeMagnifier.INSTANCE;
            this.spawnDragonFight = false;
        } else {
            this.biomeFinder = BiomeFinderProvider.INSTANCE.get((ResourceKey) (Object) sds.biomeFinder);
            this.spawnDragonFight = sds.spawnDragonFight;
        }
    }

    public SpongeDimensionTypeRegistration(final ResourceKey key, final DimensionType dimensionType) {
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
        this.spawnDragonFight = dimensionType.createDragonFight();
    }

    public void setKey(final ResourceKey key) {
        this.key = key;
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
    public DimensionEffect effect() {
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
    public boolean spawnDragonFight() {
        return this.spawnDragonFight;
    }

    public DimensionType asType() {
        final OptionalLong fixedTime = this.fixedTime == null ? OptionalLong.empty() : OptionalLong.of(this.fixedTime.asTicks().getTicks());

        final net.minecraft.world.DimensionType dimensionType = DimensionTypeAccessor.invoker$construct(fixedTime, this.skylight, this.ceiling,
                this.ultraWarm, this.natural, this.coordinateScale, this.spawnDragonFight, this.piglinSafe, this.bedWorks,
                this.respawnAnchorWorks, this.hasRaids, this.logicalHeight, (IBiomeMagnifier) this.biomeFinder,
                (ResourceLocation) (Object) this.infiniburn, (ResourceLocation) (Object) this.effect.getKey(), this.ambientLight
        );

        return dimensionType;
    }

    private static final class SpongeDataSection {
        private final ResourceLocation biomeFinder;
        private final boolean spawnDragonFight;

        public SpongeDataSection(final ResourceLocation biomeFinder, final boolean spawnDragonFight) {
            this.biomeFinder = biomeFinder;
            this.spawnDragonFight = spawnDragonFight;
        }
    }

    public static final class FactoryImpl implements DimensionTypeRegistration.Factory {

        private static final SpongeDimensionTypeRegistration OVERWORLD = new SpongeDimensionTypeRegistration(ResourceKey.minecraft("overworld"), DimensionTypeAccessor.accessor$DEFAULT_OVERWORLD());

        private static final SpongeDimensionTypeRegistration THE_NETHER = new SpongeDimensionTypeRegistration(ResourceKey.minecraft("the_nether"), DimensionTypeAccessor.accessor$DEFAULT_NETHER());

        private static final SpongeDimensionTypeRegistration THE_END = new SpongeDimensionTypeRegistration(ResourceKey.minecraft("the_end"), DimensionTypeAccessor.accessor$DEFAULT_END());

        @Override
        public DimensionTypeRegistration overworld() {
            return FactoryImpl.OVERWORLD;
        }

        @Override
        public DimensionTypeRegistration theNether() {
            return FactoryImpl.THE_NETHER;
        }

        @Override
        public DimensionTypeRegistration theEnd() {
            return FactoryImpl.THE_END;
        }
    }

    public static final class BuilderImpl extends AbstractResourceKeyedBuilder<DimensionTypeRegistration, Builder> implements DimensionTypeRegistration.Builder {

        protected DimensionEffect effect = DimensionEffects.OVERWORLD;
        protected MinecraftDayTime fixedTime;
        protected BiomeFinder biomeFinder = BiomeFinders.DEFAULT;
        protected ResourceKey infiniburn = (ResourceKey) (Object) BlockTags.INFINIBURN_OVERWORLD.getName();

        protected boolean scorching, natural, skylight, ceiling, piglinSafe, bedsUsable, respawnAnchorsUsable, hasRaids, spawnDragonFight;
        protected float ambientLighting;
        protected int logicalHeight;
        protected double coordinateMultiplier;

        @Override
        public DimensionTypeRegistration.Builder effect(final DimensionEffect effect) {
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
        public DimensionTypeRegistration.Builder scorching(final boolean scorching) {
            this.scorching = scorching;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder natural(final boolean natural) {
            this.natural = natural;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder coordinateMultiplier(final double coordinateMultiplier) {
            this.coordinateMultiplier = coordinateMultiplier;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder hasSkylight(final boolean skylight) {
            this.skylight = skylight;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder hasCeiling(final boolean ceiling) {
            this.ceiling = ceiling;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder ambientLighting(final float ambientLighting) {
            this.ambientLighting = ambientLighting;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder fixedTime(@Nullable final MinecraftDayTime fixedTime) {
            this.fixedTime = fixedTime;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder piglinSafe(final boolean piglinSafe) {
            this.piglinSafe = piglinSafe;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder bedsUsable(final boolean bedsUsable) {
            this.bedsUsable = bedsUsable;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder respawnAnchorsUsable(final boolean respawnAnchorsUsable) {
            this.respawnAnchorsUsable = respawnAnchorsUsable;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder hasRaids(final boolean hasRaids) {
            this.hasRaids = hasRaids;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder logicalHeight(final int logicalHeight) {
            this.logicalHeight = logicalHeight;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder spawnDragonFight(final boolean spawnDragonFight) {
            this.spawnDragonFight = spawnDragonFight;
            return this;
        }

        @Override
        public DimensionTypeRegistration.Builder from(final DimensionTypeRegistration value) {
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
            this.spawnDragonFight = value.spawnDragonFight();
            return this;
        }

        @Override
        public @NonNull DimensionTypeRegistration build0() {
            return new SpongeDimensionTypeRegistration(this);
        }
    }
}
