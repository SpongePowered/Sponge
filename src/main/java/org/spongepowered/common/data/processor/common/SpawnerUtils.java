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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.world.spawner.AbstractSpawner;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.mixin.accessor.util.WeightedRandom_ItemAccessor;
import org.spongepowered.common.mixin.accessor.world.spawner.AbstractSpawnerAccessor;
import org.spongepowered.common.util.Constants;

public class SpawnerUtils {


    public static WeightedSerializableObject<EntityArchetype> getNextEntity(final AbstractSpawnerAccessor logic) {
        final int weight = ((WeightedRandom_ItemAccessor) logic.accessor$getSpawnData()).accessor$getItemWeight();

        final EntityType type = EntityUtil.fromNameToType(logic.accessor$getSpawnData().getNbt().getString("id")).orElse(EntityTypes.PIG);

        final CompoundNBT data = logic.accessor$getSpawnData().getNbt();

        final EntityArchetype archetype = EntityArchetype.builder()
                .type(type)
                .entityData(NbtTranslator.getInstance().translateFrom(data))
                .build();

        return new WeightedSerializableObject<>(archetype, weight);
    }

    @SuppressWarnings("unchecked")
    public static void setNextEntity(final AbstractSpawner logic, final WeightedSerializableObject<EntityArchetype> value) {
        final CompoundNBT compound = NbtTranslator.getInstance().translateData(value.get().getEntityData());
        if (!compound.contains(Constants.Entity.ENTITY_TYPE_ID)) {
            final ResourceLocation key = EntityList.getKey((Class<? extends Entity>) value.get().getType().getEntityClass());
            compound.putString(Constants.Entity.ENTITY_TYPE_ID, key != null ? key.toString() : "");
        }

        logic.setNextSpawnData(new WeightedSpawnerEntity((int) value.getWeight(), compound));
    }

    public static WeightedTable<EntityArchetype> getEntities(final AbstractSpawner logic) {
        final WeightedTable<EntityArchetype> possibleEntities = new WeightedTable<>();
        for (final WeightedSpawnerEntity weightedEntity : ((AbstractSpawnerAccessor) logic).accessor$getPotentialSpawns()) {

            final CompoundNBT nbt = weightedEntity.getNbt();

            final EntityType type = EntityUtil.fromNameToType(nbt.getString(Constants.Entity.ENTITY_TYPE_ID)).orElse(EntityTypes.PIG);

            final EntityArchetype archetype = EntityArchetype.builder()
                    .type(type)
                    .entityData(NbtTranslator.getInstance().translateFrom(nbt))
                    .build();

            possibleEntities.add(new WeightedSerializableObject<>(archetype, ((WeightedRandom_ItemAccessor) weightedEntity).accessor$getItemWeight()));
        }

        return possibleEntities;
    }

    @SuppressWarnings("unchecked")
    public static void setEntities(final AbstractSpawnerAccessor logic, final WeightedTable<EntityArchetype> table) {
        logic.accessor$getPotentialSpawns().clear();
        for (final TableEntry<EntityArchetype> entry : table) {
            if (!(entry instanceof WeightedObject)) {
                continue;
            }
            final WeightedObject<EntityArchetype> object = (WeightedObject<EntityArchetype>) entry;

            final CompoundNBT compound = NbtTranslator.getInstance().translateData(object.get().getEntityData());
            if (!compound.contains(Constants.Entity.ENTITY_TYPE_ID)) {
                final ResourceLocation key = EntityList.getKey((Class<? extends Entity>) object.get().getType().getEntityClass());
                compound.putString(Constants.Entity.ENTITY_TYPE_ID, key != null ? key.toString() : "");
            }


            logic.accessor$getPotentialSpawns().add(new WeightedSpawnerEntity((int) entry.getWeight(), compound));
        }
    }
}
