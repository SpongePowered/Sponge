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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.datapack.DataPackType;
import org.spongepowered.api.datapack.DataPackTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypeEffect;
import org.spongepowered.api.world.WorldTypeTemplate;
import org.spongepowered.common.AbstractResourceKeyed;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.level.dimension.DimensionTypeAccessor;
import org.spongepowered.common.util.AbstractResourceKeyedBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.OptionalLong;

public final class SpongeWorldTypeTemplate extends AbstractResourceKeyed implements WorldTypeTemplate {

    private final DimensionType dimensionType;

    public SpongeWorldTypeTemplate(final ResourceKey key, final DimensionType dimensionType) {
        super(key);
        this.dimensionType = dimensionType;
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
        // TODO content-version?
        // TODO key
        final JsonElement serialized = SpongeDimensionTypes.DIRECT_CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, BootstrapProperties.registries), this.dimensionType).getOrThrow(false, e -> {});
        try {
            return DataFormats.JSON.get().read(serialized.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Could not read deserialized DimensionType:\n" + serialized, e);
        }
    }

    @Override
    public WorldType worldType() {
        return (WorldType) this.dimensionType;
    }

    public static final class BuilderImpl extends AbstractResourceKeyedBuilder<WorldTypeTemplate, Builder> implements WorldTypeTemplate.Builder {

        @Nullable private WorldTypeEffect effect;
        @Nullable private MinecraftDayTime fixedTime;
        @Nullable private Tag<BlockType> infiniburn;

        private boolean scorching, natural, skylight, ceiling, piglinSafe, bedsUsable, respawnAnchorsUsable, hasRaids, createDragonFight;
        private float ambientLighting;
        private int minY, logicalHeight, height;
        private double coordinateMultiplier;

        @Override
        public WorldTypeTemplate.Builder effect(final WorldTypeEffect effect) {
            this.effect = Objects.requireNonNull(effect, "effect");
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
        public Builder minY(final int y) {
            this.minY = y;
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder logicalHeight(final int logicalHeight) {
            this.logicalHeight = logicalHeight;
            return this;
        }

        @Override
        public Builder height(final int maximumHeight) {
            this.height = maximumHeight;
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
            this.from((WorldType) DimensionTypeAccessor.accessor$DEFAULT_OVERWORLD());
            return this;
        }

        @Override
        public Builder from(WorldType type) {
            this.effect = type.effect();
            this.fixedTime = type.fixedTime().orElse(null);
            this.infiniburn = type.infiniburn();
            this.scorching = type.scorching();
            this.natural = type.natural();
            this.skylight = type.hasSkylight();
            this.ceiling = type.hasCeiling();
            this.piglinSafe = type.piglinSafe();
            this.bedsUsable = type.bedsUsable();
            this.respawnAnchorsUsable = type.respawnAnchorsUsable();
            this.hasRaids = type.hasRaids();
            this.ambientLighting = type.ambientLighting();
            this.minY = type.floor();
            this.logicalHeight = type.logicalHeight();
            this.height = type.height();
            this.coordinateMultiplier = type.coordinateMultiplier();
            this.createDragonFight = type.createDragonFight();
            return this;
        }

        @Override
        public Builder fromDataPack(DataView pack) throws IOException {
            // TODO optional key from view?
            // TODO content-version?

            // TODO maybe accept JsonElement instead?
            final JsonElement json = JsonParser.parseString(DataFormats.JSON.get().write(pack));

            // TODO catch & rethrow exceptions in CODEC?
            // TODO probably need to rewrite CODEC to allow reading after registries are frozen
            final DataResult<Holder<DimensionType>> parsed = DimensionType.CODEC.parse(JsonOps.INSTANCE, json);
            final DimensionType dimensionType = parsed.getOrThrow(false, e -> {}).value();

            this.from((WorldType) dimensionType);
            return this;
        }

        @Override
        public WorldTypeTemplate.Builder from(final WorldTypeTemplate value) {
            Objects.requireNonNull(value, "value");
            this.from(value.worldType());
            this.key = value.key();

            return this;
        }

        @Override
        public @NonNull WorldTypeTemplate build0() {
            try {
                final DimensionType dimensionType =
                        DimensionType.create(this.fixedTime == null ? OptionalLong.empty() : OptionalLong.of(this.fixedTime.asTicks().ticks()),
                                this.skylight, this.ceiling, this.scorching, this.natural, this.coordinateMultiplier,
                                this.createDragonFight, this.piglinSafe, this.bedsUsable, this.respawnAnchorsUsable, this.hasRaids,
                                this.minY, this.height, this.logicalHeight,
                                (TagKey<Block>) (Object) this.infiniburn,
                                (ResourceLocation) (Object) this.effect.key(),
                                this.ambientLighting);
                return new SpongeWorldTypeTemplate(this.key, dimensionType);
            } catch (IllegalStateException e) { // catch and rethrow minecraft internal exception
                throw new IllegalStateException(String.format("Template '%s' was not valid!", this.key), e);
            }
        }
    }

    public static final class FactoryImpl implements WorldTypeTemplate.Factory {

        @Override
        public WorldTypeTemplate overworld() {
            var type = SpongeCommon.server().registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(BuiltinDimensionTypes.OVERWORLD);
            return new SpongeWorldTypeTemplate(ResourceKey.minecraft("overworld"), type);
        }

        @Override
        public WorldTypeTemplate overworldCaves() {
            var type = SpongeCommon.server().registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(BuiltinDimensionTypes.OVERWORLD_CAVES);
            return new SpongeWorldTypeTemplate(ResourceKey.minecraft("overworld_caves"), type);
        }

        @Override
        public WorldTypeTemplate theNether() {
            var type = SpongeCommon.server().registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(BuiltinDimensionTypes.NETHER);
            return new SpongeWorldTypeTemplate(ResourceKey.minecraft("the_nether"), type);
        }

        @Override
        public WorldTypeTemplate theEnd() {
            var type = SpongeCommon.server().registryAccess().registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(BuiltinDimensionTypes.END);
            return new SpongeWorldTypeTemplate(ResourceKey.minecraft("the_end"), type);
        }

    }
}
