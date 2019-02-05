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
package org.spongepowered.common.data.processor.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public class SpawnerUtils {

    public static WeightedSerializableObject<EntityArchetype> getNextEntity(MobSpawnerBaseLogic logic) {
        int weight = logic.spawnData.itemWeight;

        EntityType type = EntityUtil.fromNameToType(logic.spawnData.getNbt().getString("id")).orElse(EntityTypes.PIG);

        NBTTagCompound data = logic.spawnData.getNbt();

        EntityArchetype archetype = EntityArchetype.builder()
                .type(type)
                .entityData(NbtTranslator.getInstance().translateFrom(data))
                .build();

        return new WeightedSerializableObject<>(archetype, weight);
    }

    @SuppressWarnings("unchecked")
    public static void setNextEntity(MobSpawnerBaseLogic logic, WeightedSerializableObject<EntityArchetype> value) {
        NBTTagCompound compound = NbtTranslator.getInstance().translateData(value.get().getEntityData());
        if (!compound.hasKey(NbtDataUtil.ENTITY_TYPE_ID)) {
            final ResourceLocation key = EntityList.getKey((Class<? extends Entity>) value.get().getType().getEntityClass());
            compound.setString(NbtDataUtil.ENTITY_TYPE_ID, key != null ? key.toString() : "");
        }

        logic.setNextSpawnData(new WeightedSpawnerEntity((int) value.getWeight(), compound));
    }

    public static WeightedTable<EntityArchetype> getEntities(MobSpawnerBaseLogic logic) {
        WeightedTable<EntityArchetype> possibleEntities = new WeightedTable<>();
        for (WeightedSpawnerEntity weightedEntity : logic.potentialSpawns) {

            NBTTagCompound nbt = weightedEntity.getNbt();

            EntityType type = EntityUtil.fromNameToType(nbt.getString(NbtDataUtil.ENTITY_TYPE_ID)).orElse(EntityTypes.PIG);

            EntityArchetype archetype = EntityArchetype.builder()
                    .type(type)
                    .entityData(NbtTranslator.getInstance().translateFrom(nbt))
                    .build();

            possibleEntities.add(new WeightedSerializableObject<>(archetype, weightedEntity.itemWeight));
        }

        return possibleEntities;
    }

    @SuppressWarnings("unchecked")
    public static void setEntities(MobSpawnerBaseLogic logic, WeightedTable<EntityArchetype> table) {
        logic.potentialSpawns.clear();
        for (TableEntry<EntityArchetype> entry : table) {
            if (!(entry instanceof WeightedObject)) {
                continue;
            }
            WeightedObject<EntityArchetype> object = (WeightedObject<EntityArchetype>) entry;

            NBTTagCompound compound = NbtTranslator.getInstance().translateData(object.get().getEntityData());
            if (!compound.hasKey(NbtDataUtil.ENTITY_TYPE_ID)) {
                final ResourceLocation key = EntityList.getKey((Class<? extends Entity>) object.get().getType().getEntityClass());
                compound.setString(NbtDataUtil.ENTITY_TYPE_ID, key != null ? key.toString() : "");
            }


            logic.potentialSpawns.add(new WeightedSpawnerEntity((int) entry.getWeight(), compound));
        }
    }

    @SuppressWarnings("unchecked")
    public static void applyData(MobSpawnerBaseLogic logic, Map<Key<?>, Object> values) {
        logic.spawnDelay = (short) values.get(Keys.SPAWNER_REMAINING_DELAY);
        logic.minSpawnDelay = (short) values.get(Keys.SPAWNER_MINIMUM_DELAY);
        logic.maxSpawnDelay = (short) values.get(Keys.SPAWNER_MAXIMUM_DELAY);
        logic.spawnCount = (short) values.get(Keys.SPAWNER_SPAWN_COUNT);
        logic.maxNearbyEntities = (short) values.get(Keys.SPAWNER_MAXIMUM_NEARBY_ENTITIES);
        logic.activatingRangeFromPlayer = (short) values.get(Keys.SPAWNER_REQUIRED_PLAYER_RANGE);
        logic.spawnRange = (short) values.get(Keys.SPAWNER_SPAWN_RANGE);
        setNextEntity(logic, (WeightedSerializableObject<EntityArchetype>) values.get(Keys.SPAWNER_NEXT_ENTITY_TO_SPAWN));
        setEntities(logic, (WeightedTable<EntityArchetype>) values.get(Keys.SPAWNER_ENTITIES));
    }

    public static void applyData(MobSpawnerBaseLogic logic, MobSpawnerData data) {
        final Map<Key<?>, Object> map = new IdentityHashMap<>();
        final Set<Value.Immutable<?>> newValues = data.getValues();
        for (Value.Immutable<?> value : newValues) {
            map.put(value.getKey(), value.get());
        }
        applyData(logic, map);
    }

}
