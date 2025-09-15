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

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypeEffect;
import org.spongepowered.common.bridge.tags.TagBridge;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderLookup;

import java.util.OptionalLong;

public final class SpongeWorldTypeBuilder implements WorldType.Builder {

    private static final DataProviderLookup PROVIDER_LOOKUP = SpongeDataManager.getProviderRegistry().getProviderLookup(DimensionType.class);

    private DataManipulator.Mutable data = DataManipulator.mutableOf();

    public SpongeWorldTypeBuilder() {
        this.reset();
    }

    @Override
    public <V> WorldType.Builder add(final Key<? extends Value<V>> key, final V value) {
        if (!PROVIDER_LOOKUP.getProvider(key).isSupported(DimensionType.class)) {
            throw new IllegalArgumentException(key + " is not supported for world types");
        }
        this.data.set(key, value);
        return this;
    }

    @Override
    public WorldType.Builder reset() {
        this.data = DataManipulator.mutableOf();
        return this;
    }

    @Override
    public WorldType.Builder from(final WorldType type) {
        this.data.set(type.getValues());
        return this;
    }

    @Override
    public @NonNull WorldType build() {
        @Nullable final WorldTypeEffect effect = this.data.getOrNull(Keys.WORLD_TYPE_EFFECT);
        final boolean scorching = this.data.require(Keys.SCORCHING);
        final boolean natural = this.data.require(Keys.NATURAL_WORLD_TYPE);
        final double coordinateMultiplier = this.data.require(Keys.COORDINATE_MULTIPLIER);
        final boolean hasSkylight = this.data.require(Keys.HAS_SKYLIGHT);
        final boolean hasCeiling = this.data.require(Keys.HAS_CEILING);
        final float ambientLighting = this.data.require(Keys.AMBIENT_LIGHTING);
        @Nullable final MinecraftDayTime fixedTime = this.data.getOrNull(Keys.FIXED_TIME);
        final boolean bedsUsable = this.data.require(Keys.BEDS_USABLE);
        final boolean respawnAnchorsUsable = this.data.require(Keys.RESPAWN_ANCHOR_USABLE);
        final int floor = this.data.require(Keys.WORLD_FLOOR);
        final int height = this.data.require(Keys.WORLD_HEIGHT);
        final int logicalHeight = this.data.require(Keys.WORLD_LOGICAL_HEIGHT);
        @Nullable final Tag<BlockType> infiniburn = this.data.getOrNull(Keys.INFINIBURN);

        final boolean piglinSafe = this.data.require(Keys.PIGLIN_SAFE);
        final boolean hasRaids = this.data.require(Keys.HAS_RAIDS);
        final int monsterSpawnBlockLightLimit = this.data.getOrElse(Keys.SPAWN_LIGHT_LIMIT, 0);
        final Range<Integer> lightRange = this.data.getOrElse(Keys.SPAWN_LIGHT_RANGE, Range.intRange(0, 7));
        final boolean createDragonFight = this.data.getOrElse(Keys.CREATE_DRAGON_FIGHT, false);
        final UniformInt monsterSpawnLightTest = UniformInt.of(lightRange.min(), lightRange.max());

        final SpongeDimensionTypes.SpongeDataSection spongeData = new SpongeDimensionTypes.SpongeDataSection(createDragonFight);
        try {

            final DimensionType dimensionType =
                new DimensionType(fixedTime == null ? OptionalLong.empty() : OptionalLong.of(fixedTime.asTicks().ticks()),
                    hasSkylight, hasCeiling, scorching, natural, coordinateMultiplier,
                    bedsUsable, respawnAnchorsUsable,
                    floor, height, logicalHeight,
                    ((TagBridge<Block>) infiniburn).bridge$asVanillaTag(),
                    (ResourceLocation) (Object) effect.key(),
                    ambientLighting,
                    new DimensionType.MonsterSettings(piglinSafe, hasRaids, monsterSpawnLightTest, monsterSpawnBlockLightLimit));
            if ((Object) dimensionType instanceof DimensionTypeBridge bridge) {
                bridge.bridge$decorateData(spongeData);
            }
            return (WorldType) (Object) dimensionType;
        } catch (IllegalStateException e) { // catch and rethrow minecraft internal exception
            throw new IllegalStateException("World type was not valid!", e);
        }
    }
}
