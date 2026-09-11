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

import net.minecraft.core.HolderSet;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.util.Range;
import org.spongepowered.api.world.WorldTypeEffect;
import org.spongepowered.common.accessor.world.level.dimension.DimensionTypeAccessor;
import org.spongepowered.common.bridge.world.level.dimension.DimensionTypeBridge;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.registry.provider.DimensionEffectProvider;

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
                        .get(d -> d.attributes().applyModifier(EnvironmentAttributes.WATER_EVAPORATES, false))
                    .create(Keys.NATURAL_WORLD_TYPE)
                        .get(d -> d.attributes().contains(EnvironmentAttributes.BED_RULE))
                    .create(Keys.COORDINATE_MULTIPLIER)
                        .get(DimensionType::coordinateScale)
                    .create(Keys.HAS_SKYLIGHT)
                        .get(DimensionType::hasSkyLight)
                    .create(Keys.HAS_CEILING)
                        .get(DimensionType::hasCeiling)
                    .create(Keys.PIGLIN_SAFE)
                        .get(d -> d.attributes().applyModifier(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false))
                    .create(Keys.BEDS_USABLE)
                        .get(d -> d.attributes().applyModifier(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK).destroyOnUse())
                    .create(Keys.RESPAWN_ANCHOR_USABLE)
                        .get(d -> d.attributes().applyModifier(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, false))
                    .create(Keys.INFINIBURN)
                        .get(dimensionType -> dimensionType.infiniburn() instanceof HolderSet.Named<Block> named
                            ? (Tag<BlockType>) (Object) named.key()
                            : null)
                    .create(Keys.WORLD_FLOOR)
                        .get(DimensionType::minY)
                    .create(Keys.HAS_RAIDS)
                        .get(d -> d.attributes().applyModifier(EnvironmentAttributes.CAN_START_RAID, true))
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
                .asImmutable(DimensionTypeBridge.class)
                    .create(Keys.CREATE_DRAGON_FIGHT)
                        .get(DimensionTypeBridge::bridge$createDragonFight)
        ;

    }
    // @formatter:on

    private static Range<Integer> lightRange(IntProvider provider) {
        return Range.intRange(provider.minInclusive(), provider.maxInclusive());
    }

    private static WorldTypeEffect worldTypeEffect(final DimensionType type) {
        @Nullable final WorldTypeEffect effect = DimensionEffectProvider.INSTANCE.get(type.skybox());
        if (effect == null) {
            throw new IllegalStateException(String.format("The effect '%s' has not been registered!", type.skybox()));
        }
        return effect;
    }

}
