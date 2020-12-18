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

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.spawner.AbstractSpawner;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.accessor.tileentity.MobSpawnerTileEntityAccessor;
import org.spongepowered.common.accessor.util.WeightedRandom_ItemAccessor;
import org.spongepowered.common.accessor.world.spawner.AbstractSpawnerAccessor;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

public final class MobSpawnerData {

    private MobSpawnerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(MobSpawnerTileEntityAccessor.class)
                    .create(Keys.MAX_NEARBY_ENTITIES)
                        .get(h -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$maxNearbyEntities())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$maxNearbyEntities(v))
                    .create(Keys.MAX_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$maxSpawnDelay()))
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$maxSpawnDelay((int) v.getTicks()))
                    .create(Keys.MIN_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$minSpawnDelay()))
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$minSpawnDelay((int) v.getTicks()))
                    .create(Keys.NEXT_ENTITY_TO_SPAWN)
                        .get(h -> MobSpawnerData.getNextEntity((AbstractSpawnerAccessor) h.accessor$spawner()))
                        .set((h, v) -> MobSpawnerData.setNextEntity(h.accessor$spawner(), v))
                    .create(Keys.REMAINING_SPAWN_DELAY)
                        .get(h -> new SpongeTicks(((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$spawnDelay()))
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$spawnDelay((int) v.getTicks()))
                    .create(Keys.REQUIRED_PLAYER_RANGE)
                        .get(h -> (double) ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$requiredPlayerRange())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$requiredPlayerRange(v.intValue()))
                    .create(Keys.SPAWN_COUNT)
                        .get(h -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$spawnCount())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$spawnCount(v))
                    .create(Keys.SPAWN_RANGE)
                        .get(h -> (double) ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$spawnRange())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$spawner()).accessor$spawnRange(v.intValue()))
                    .create(Keys.SPAWNABLE_ENTITIES)
                        .get(h -> MobSpawnerData.getEntities(h.accessor$spawner()))
                        .set((h, v) -> {
                            final AbstractSpawnerAccessor logic = (AbstractSpawnerAccessor) h.accessor$spawner();
                            MobSpawnerData.setEntities(logic, v);
                            MobSpawnerData.setNextEntity((AbstractSpawner) logic, MobSpawnerData.getNextEntity(logic));
                        });
    }
    // @formatter:on

    private static WeightedSerializableObject<EntityArchetype> getNextEntity(final AbstractSpawnerAccessor logic) {
        final int weight = ((WeightedRandom_ItemAccessor) logic.accessor$nextSpawnData()).accessor$weight();

        final String resourceLocation = logic.accessor$nextSpawnData().getTag().getString(Constants.Entity.ENTITY_TYPE_ID);
        final EntityType<?> type =
                Registry.ENTITY_TYPE.getOptional(new ResourceLocation(resourceLocation)).map(EntityType.class::cast).orElse(EntityTypes.PIG.get());

        final CompoundNBT data = logic.accessor$nextSpawnData().getTag();

        final EntityArchetype archetype = EntityArchetype.builder()
                .type(type)
                .entityData(NBTTranslator.INSTANCE.translateFrom(data))
                .build();

        return new WeightedSerializableObject<>(archetype, weight);
    }

    private static void setNextEntity(final AbstractSpawner logic, final WeightedSerializableObject<EntityArchetype> value) {
        final CompoundNBT compound = NBTTranslator.INSTANCE.translate(value.get().getEntityData());
        if (!compound.contains(Constants.Entity.ENTITY_TYPE_ID)) {
            final ResourceKey key = value.get().getType().getKey();
            compound.putString(Constants.Entity.ENTITY_TYPE_ID, key.toString());
        }

        logic.setNextSpawnData(new WeightedSpawnerEntity((int) value.getWeight(), compound));
    }

    private static WeightedTable<EntityArchetype> getEntities(final AbstractSpawner logic) {
        final WeightedTable<EntityArchetype> possibleEntities = new WeightedTable<>();
        for (final WeightedSpawnerEntity weightedEntity : ((AbstractSpawnerAccessor) logic).accessor$spawnPotentials()) {

            final CompoundNBT nbt = weightedEntity.getTag();

            final String resourceLocation = nbt.getString(Constants.Entity.ENTITY_TYPE_ID);
            final EntityType<?> type =
                    Registry.ENTITY_TYPE.getOptional(new ResourceLocation(resourceLocation)).map(EntityType.class::cast).orElse(EntityTypes.PIG.get());

            final EntityArchetype archetype = EntityArchetype.builder()
                    .type(type)
                    .entityData(NBTTranslator.INSTANCE.translateFrom(nbt))
                    .build();

            possibleEntities
                    .add(new WeightedSerializableObject<>(archetype, ((WeightedRandom_ItemAccessor) weightedEntity).accessor$weight()));
        }

        return possibleEntities;
    }

    private static void setEntities(final AbstractSpawnerAccessor logic, final WeightedTable<EntityArchetype> table) {
        logic.accessor$spawnPotentials().clear();
        for (final TableEntry<EntityArchetype> entry : table) {
            if (!(entry instanceof WeightedObject)) {
                continue;
            }
            final WeightedObject<EntityArchetype> object = (WeightedObject<EntityArchetype>) entry;

            final CompoundNBT compound = NBTTranslator.INSTANCE.translate(object.get().getEntityData());
            if (!compound.contains(Constants.Entity.ENTITY_TYPE_ID)) {
                final ResourceKey key = object.get().getType().getKey();
                compound.putString(Constants.Entity.ENTITY_TYPE_ID, key.toString());
            }


            logic.accessor$spawnPotentials().add(new WeightedSpawnerEntity((int) entry.getWeight(), compound));
        }
    }
}
