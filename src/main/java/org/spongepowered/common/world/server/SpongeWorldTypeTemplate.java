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
package org.spongepowered.common.world.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.world.biome.BiomeSampler;
import org.spongepowered.api.world.biome.BiomeSamplers;
import org.spongepowered.api.world.WorldTypeEffect;
import org.spongepowered.api.world.WorldTypeEffects;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.accessor.world.level.dimension.DimensionTypeAccessor;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.data.fixer.SpongeDataCodec;
import org.spongepowered.common.registry.provider.DimensionEffectProvider;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.util.SpongeMinecraftDayTime;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Supplier;

public final class SpongeWorldTypeTemplate extends AbstractResourceKeyed implements WorldTypeTemplate {

    public final WorldTypeEffect effect;
    public final BiomeSampler biomeSampler;
    @Nullable public final MinecraftDayTime fixedTime;
    public final ResourceKey infiniburn;

    public final boolean ultraWarm, natural, skylight, ceiling, piglinSafe, bedWorks, respawnAnchorWorks, hasRaids, createDragonFight;
    public final float ambientLight;
    public final int logicalHeight;
    public final double coordinateScale;

    private static final Codec<SpongeDataSection> SPONGE_CODEC = RecordCodecBuilder
        .create(r -> r
            .group(
                ResourceLocation.CODEC.optionalFieldOf("biome_sampler", new ResourceLocation("sponge", "column_fuzzed")).forGetter(v -> v.biomeSampler),
                Codec.BOOL.optionalFieldOf("create_dragon_fight", Boolean.FALSE).forGetter(v -> v.createDragonFight)
            )
            .apply(r, r.stable(SpongeDataSection::new))
        );

    public static final Codec<DimensionType> DIRECT_CODEC = new MapCodec.MapCodecCodec<>(new SpongeDataCodec<>(DimensionType.DIRECT_CODEC,
        SpongeWorldTypeTemplate.SPONGE_CODEC, (type, data) -> ((DimensionTypeBridge) type).bridge$decorateData(data), type -> ((DimensionTypeBridge)
        type).bridge$createData()));

    public static final Codec<Supplier<DimensionType>> CODEC = RegistryFileCodec.create(Registry.DIMENSION_TYPE_REGISTRY, SpongeWorldTypeTemplate.DIRECT_CODEC);

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
        this.biomeSampler = builder.biomeSampler;
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
        this.biomeSampler = (BiomeSampler) dimensionType.getBiomeZoomer();
        this.infiniburn = (ResourceKey) (Object) ((DimensionTypeAccessor)dimensionType).accessor$infiniburn();
        this.effect = DimensionEffectProvider.INSTANCE.get((ResourceKey) (Object) ((DimensionTypeAccessor) dimensionType).accessor$effectsLocation());
        this.ambientLight = ((DimensionTypeAccessor) dimensionType).accessor$ambientLight();
        this.createDragonFight = dimensionType.createDragonFight();
    }

    @Override
    public DataPackType<WorldTypeTemplate> type() {
        return DataPackTypes.WORLD_TYPE;
    }

    @Override
    public int contentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        throw new MissingImplementationException("SpongeWorldTypeTemplate", "toContainer");
    }

    @Override
    public WorldTypeEffect effect() {
        return this.effect;
    }

    @Override
    public BiomeSampler biomeSampler() {
        return this.biomeSampler;
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

    public static final class SpongeDataSection {
        public final ResourceLocation biomeSampler;
        public final boolean createDragonFight;

        public SpongeDataSection(final ResourceLocation biomeSampler, final boolean createDragonFight) {
            this.biomeSampler = biomeSampler;
            this.createDragonFight = createDragonFight;
        }
    }

    public static final class BuilderImpl extends AbstractResourceKeyedBuilder<WorldTypeTemplate, Builder> implements WorldTypeTemplate.Builder {

        @Nullable protected WorldTypeEffect effect;
        @Nullable protected MinecraftDayTime fixedTime;
        @Nullable protected BiomeSampler biomeSampler;
        @Nullable protected ResourceKey infiniburn;

        protected boolean scorching, natural, skylight, ceiling, piglinSafe, bedsUsable, respawnAnchorsUsable, hasRaids, createDragonFight;
        protected float ambientLighting;
        protected int logicalHeight;
        protected double coordinateMultiplier;

        @Override
        public WorldTypeTemplate.Builder effect(final WorldTypeEffect effect) {
            this.effect = Objects.requireNonNull(effect, "effect");
            return this;
        }

        @Override
        public Builder biomeSampler(final BiomeSampler biomeSampler) {
            this.biomeSampler = Objects.requireNonNull(biomeSampler, "biomeFinder");
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
        public WorldTypeTemplate.Builder fixedTime(final @Nullable MinecraftDayTime fixedTime) {
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
        public Builder reset() {
            super.reset();
            this.effect = WorldTypeEffects.OVERWORLD;
            this.fixedTime = null;
            this.biomeSampler = BiomeSamplers.COLUMN_FUZZED;
            this.infiniburn = (ResourceKey) (Object) BlockTags.INFINIBURN_OVERWORLD.getName();
            this.scorching = false;
            this.natural = true;
            this.skylight = true;
            this.ceiling = false;
            this.piglinSafe = false;
            this.bedsUsable = true;
            this.respawnAnchorsUsable = false;
            this.hasRaids = true;
            this.ambientLighting = 0.5f;
            this.logicalHeight = 256;
            this.coordinateMultiplier = 1;
            this.createDragonFight = false;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder from(final WorldTypeTemplate value) {
            Objects.requireNonNull(value, "value");

            this.effect = value.effect();
            this.biomeSampler = value.biomeSampler();
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

    public static final class FactoryImpl implements WorldTypeTemplate.Factory {

        @Override
        public WorldTypeTemplate overworld() {
            return new SpongeWorldTypeTemplate(ResourceKey.minecraft("overworld"), DimensionTypeAccessor.accessor$DEFAULT_OVERWORLD());
        }

        @Override
        public WorldTypeTemplate overworldCaves() {
            return new SpongeWorldTypeTemplate(ResourceKey.minecraft("overworld_caves"), DimensionTypeAccessor.accessor$DEFAULT_OVERWORLD_CAVES());
        }

        @Override
        public WorldTypeTemplate theNether() {
            return new SpongeWorldTypeTemplate(ResourceKey.minecraft("the_nether"), DimensionTypeAccessor.accessor$DEFAULT_NETHER());
        }

        @Override
        public WorldTypeTemplate theEnd() {
            return new SpongeWorldTypeTemplate(ResourceKey.minecraft("the_end"), DimensionTypeAccessor.accessor$DEFAULT_END());
        }
    }
}
