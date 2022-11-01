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
package org.spongepowered.common.data.provider.world;

import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.util.MinecraftDayTime;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.world.WorldTypeEffect;
import org.spongepowered.common.accessor.world.level.dimension.DimensionTypeAccessor;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.registry.provider.DimensionEffectProvider;
import org.spongepowered.common.util.SpongeMinecraftDayTime;

import java.util.OptionalLong;

public final class WorldTypeData {

    private WorldTypeData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(DimensionType.class)
                    .create(Keys.WORLD_TYPE_EFFECT)
                        .get(WorldTypeData::worldTypeEffect)
                    .create(Keys.SCORCHING)
                        .get(DimensionType::ultraWarm)
                    .create(Keys.NATURAL_WORLD_TYPE)
                        .get(DimensionType::natural)
                    .create(Keys.COORDINATE_MULTIPLIER)
                        .get(DimensionType::coordinateScale)
                    .create(Keys.HAS_SKYLIGHT)
                        .get(DimensionType::hasSkyLight)
                    .create(Keys.HAS_CEILING)
                        .get(DimensionType::hasCeiling)
                    .create(Keys.PIGLIN_SAFE)
                        .get(DimensionType::piglinSafe)
                    .create(Keys.BEDS_USABLE)
                        .get(DimensionType::bedWorks)
                    .create(Keys.RESPAWN_ANCHOR_USABLE)
                        .get(DimensionType::respawnAnchorWorks)
                    .create(Keys.INFINIBURN)
                        .get(dimensionType -> (Tag<BlockType>) (Object) dimensionType.infiniburn())
                    .create(Keys.WORLD_FLOOR)
                        .get(DimensionType::minY)
                    .create(Keys.HAS_RAIDS)
                        .get(DimensionType::hasRaids)
                    .create(Keys.WORLD_HEIGHT)
                        .get(DimensionType::height)
                    .create(Keys.WORLD_LOGICAL_HEIGHT)
                        .get(DimensionType::logicalHeight)
                    .create(Keys.SPAWN_LIGHT_LIMIT)
                        .get(DimensionType::monsterSpawnBlockLightLimit)
                    .create(Keys.SPAWN_LIGHT_RANGE)
                        .get(t -> WorldTypeData.lightRange(t.monsterSettings().monsterSpawnLightTest()))
                .asImmutable(DimensionTypeAccessor.class)
                    .create(Keys.AMBIENT_LIGHTING)
                        .get(DimensionTypeAccessor::accessor$ambientLight)
                    .create(Keys.FIXED_TIME)
                        .get(WorldTypeData::fixedTime)
                .asImmutable(DimensionTypeBridge.class)
                    .create(Keys.CREATE_DRAGON_FIGHT)
                        .get(DimensionTypeBridge::bridge$createDragonFight)
        ;

    }
    // @formatter:on

    private static Range<Integer> lightRange(IntProvider provider) {
        return Range.intRange(provider.getMinValue(), provider.getMaxValue());
    }

    private static WorldTypeEffect worldTypeEffect(final DimensionType type) {
        final var key = (ResourceKey) (Object) type.effectsLocation();
        @Nullable final WorldTypeEffect effect = DimensionEffectProvider.INSTANCE.get(key);
        if (effect == null) {
            throw new IllegalStateException(String.format("The effect '%s' has not been registered!", key));
        }
        return effect;
    }

    @Nullable
    private static MinecraftDayTime fixedTime(final DimensionTypeAccessor accessor) {
        final OptionalLong fixedTime = accessor.accessor$fixedTime();
        if (!fixedTime.isPresent()) {
            return null;
        }
        return new SpongeMinecraftDayTime(fixedTime.getAsLong());
    }
}
