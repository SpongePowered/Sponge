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
package org.spongepowered.common.data.provider.block.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.level.BaseSpawnerAccessor;
import org.spongepowered.common.accessor.world.level.block.entity.SpawnerBlockEntityAccessor;
import org.spongepowered.common.bridge.world.level.BaseSpawnerBridge;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Optional;

public final class MobSpawnerData {

    private MobSpawnerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(SpawnerBlockEntityAccessor.class)
                    .create(Keys.MAX_NEARBY_ENTITIES)
                        .get(h -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$maxNearbyEntities())
                        .set((h, v) -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$maxNearbyEntities(v))
                    .create(Keys.MAX_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((BaseSpawnerAccessor) h.accessor$spawner()).accessor$maxSpawnDelay()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$maxSpawnDelay(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.MIN_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((BaseSpawnerAccessor) h.accessor$spawner()).accessor$minSpawnDelay()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$minSpawnDelay(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.NEXT_ENTITY_TO_SPAWN)
                        .get(h -> MobSpawnerData.getNextEntity((BaseSpawnerAccessor) h.accessor$spawner()))
                        .set((h, v) -> MobSpawnerData.setNextEntity((SpawnerBlockEntity) h, v))
                    .create(Keys.REMAINING_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnDelay()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnDelay(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.REQUIRED_PLAYER_RANGE)
                        .get(h -> (double) ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$requiredPlayerRange())
                        .set((h, v) -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$requiredPlayerRange(v.intValue()))
                    .create(Keys.SPAWN_COUNT)
                        .get(h -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnCount())
                        .set((h, v) -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnCount(v))
                    .create(Keys.SPAWN_RANGE)
                        .get(h -> (double) ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnRange())
                        .set((h, v) -> ((BaseSpawnerAccessor) h.accessor$spawner()).accessor$spawnRange(v.intValue()))
                    .create(Keys.SPAWNABLE_ENTITIES)
                        .get(h -> MobSpawnerData.getEntities(h.accessor$spawner()))
                        .set((h, v) -> {
                            final BaseSpawnerAccessor logic = (BaseSpawnerAccessor) h.accessor$spawner();
                            MobSpawnerData.setEntities(logic, v);
                            MobSpawnerData.setNextEntity((SpawnerBlockEntity) h, MobSpawnerData.getNextEntity(logic));
                        });
    }
    // @formatter:on

    private static WeightedSerializableObject<EntityArchetype> getNextEntity(final BaseSpawnerAccessor logic) {
        //TODO next entity doesn't have a weight but the api key needs to be changed
        final int weight = 1; //((WeighedRandom_WeighedRandomItemAccessor) logic.accessor$nextSpawnData()).accessor$weight();

        final String resourceLocation = logic.accessor$nextSpawnData().entityToSpawn().getString(Constants.Entity.ENTITY_TYPE_ID);
        final Registry<net.minecraft.world.entity.EntityType<?>> entityTypeRegistry = SpongeCommon.vanillaRegistry(Registries.ENTITY_TYPE);
        final EntityType<?> type = entityTypeRegistry.getOptional(new ResourceLocation(resourceLocation)).map(EntityType.class::cast).orElse(EntityTypes.PIG.get());

        final CompoundTag data = logic.accessor$nextSpawnData().entityToSpawn();

        final EntityArchetype archetype = SpongeEntityArchetypeBuilder.pooled()
                .type(type)
                .entityData(NBTTranslator.INSTANCE.translateFrom(data))
                .build();

        return new WeightedSerializableObject<>(archetype, weight);
    }

    private static void setNextEntity(final SpawnerBlockEntity entity, final WeightedSerializableObject<EntityArchetype> value) {
        final CompoundTag compound = NBTTranslator.INSTANCE.translate(value.get().entityData());
        if (!compound.contains(Constants.Entity.ENTITY_TYPE_ID)) {
            final ResourceKey key = (ResourceKey) (Object) net.minecraft.world.entity.EntityType.getKey((net.minecraft.world.entity.EntityType<?>) value.get()
                    .type());
            compound.putString(Constants.Entity.ENTITY_TYPE_ID, key.toString());
        }

        ((BaseSpawnerBridge) entity.getSpawner()).bridge$setNextSpawnData(entity.getLevel(), entity.getBlockPos(), new SpawnData(compound, Optional.empty(), Optional.empty()));
    }

    private static WeightedTable<EntityArchetype> getEntities(final BaseSpawner logic) {
        final WeightedTable<EntityArchetype> possibleEntities = new WeightedTable<>();
        for (final WeightedEntry.Wrapper<SpawnData> weightedEntity : ((BaseSpawnerAccessor) logic).accessor$spawnPotentials().unwrap()) {

            final CompoundTag nbt = weightedEntity.data().entityToSpawn();

            final String resourceLocation = nbt.getString(Constants.Entity.ENTITY_TYPE_ID);
            final Registry<net.minecraft.world.entity.EntityType<?>> entityTypeRegistry = SpongeCommon.vanillaRegistry(Registries.ENTITY_TYPE);
            final EntityType<?> type = entityTypeRegistry.getOptional(new ResourceLocation(resourceLocation)).map(EntityType.class::cast).orElse(EntityTypes.PIG.get());

            final EntityArchetype archetype = SpongeEntityArchetypeBuilder.pooled()
                    .type(type)
                    .entityData(NBTTranslator.INSTANCE.translateFrom(nbt))
                    .build();

            possibleEntities.add(new WeightedSerializableObject<>(archetype, weightedEntity.getWeight().asInt()));
        }

        return possibleEntities;
    }

    private static void setEntities(final BaseSpawnerAccessor logic, final WeightedTable<EntityArchetype> table) {
        final SimpleWeightedRandomList.Builder<SpawnData> builder = SimpleWeightedRandomList.builder();
        for (final TableEntry<EntityArchetype> entry : table) {
            if (!(entry instanceof WeightedObject)) {
                continue;
            }
            final WeightedObject<EntityArchetype> object = (WeightedObject<EntityArchetype>) entry;

            final CompoundTag compound = NBTTranslator.INSTANCE.translate(object.get().entityData());
            if (!compound.contains(Constants.Entity.ENTITY_TYPE_ID)) {
                final ResourceKey key = (ResourceKey) (Object) net.minecraft.world.entity.EntityType.getKey((net.minecraft.world.entity.EntityType<?>) object
                        .get().type());
                compound.putString(Constants.Entity.ENTITY_TYPE_ID, key.toString());
            }

            builder.add(new SpawnData(compound, Optional.empty(), Optional.empty()), (int) entry.weight());
        }
        logic.accessor$spawnPotentials(builder.build());
    }
}
