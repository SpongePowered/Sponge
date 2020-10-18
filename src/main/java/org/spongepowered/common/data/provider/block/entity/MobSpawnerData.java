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
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

public final class MobSpawnerData {

    private MobSpawnerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(MobSpawnerTileEntityAccessor.class)
                    .create(Keys.MAX_NEARBY_ENTITIES)
                        .get(h -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$getMaxNearbyEntities())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$setMaxNearbyEntities(v))
                    .create(Keys.MAX_SPAWN_DELAY)
                        .get(h -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$getMaxSpawnDelay())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$setMaxSpawnDelay(v))
                    .create(Keys.MIN_SPAWN_DELAY)
                        .get(h -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$getMinSpawnDelay())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$setMinSpawnDelay(v))
                    .create(Keys.NEXT_ENTITY_TO_SPAWN)
                        .get(h -> getNextEntity((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()))
                        .set((h, v) -> setNextEntity(h.accessor$getSpawnerLogic(), v))
                    .create(Keys.REMAINING_SPAWN_DELAY)
                        .get(h -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$getSpawnDelay())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$setSpawnDelay(v))
                    .create(Keys.REQUIRED_PLAYER_RANGE)
                        .get(h -> (double) ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$getActivatingRangeFromPlayer())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$setActivatingRangeFromPlayer(v.intValue()))
                    .create(Keys.SPAWN_COUNT)
                        .get(h -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$getSpawnCount())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$setSpawnCount(v))
                    .create(Keys.SPAWN_RANGE)
                        .get(h -> (double) ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$getSpawnRange())
                        .set((h, v) -> ((AbstractSpawnerAccessor) h.accessor$getSpawnerLogic()).accessor$setSpawnRange(v.intValue()))
                    .create(Keys.SPAWNABLE_ENTITIES)
                        .get(h -> getEntities(h.accessor$getSpawnerLogic()))
                        .set((h, v) -> {
                            final AbstractSpawnerAccessor logic = (AbstractSpawnerAccessor) h.accessor$getSpawnerLogic();
                            setEntities(logic, v);
                            setNextEntity((AbstractSpawner) logic, getNextEntity(logic));
                        });
    }
    // @formatter:on

    private static WeightedSerializableObject<EntityArchetype> getNextEntity(final AbstractSpawnerAccessor logic) {
        final int weight = ((WeightedRandom_ItemAccessor) logic.accessor$getSpawnData()).accessor$getItemWeight();

        final String resourceLocation = logic.accessor$getSpawnData().getNbt().getString(Constants.Entity.ENTITY_TYPE_ID);
        final EntityType<?> type =
                Registry.ENTITY_TYPE.getValue(new ResourceLocation(resourceLocation)).map(EntityType.class::cast).orElse(EntityTypes.PIG.get());

        final CompoundNBT data = logic.accessor$getSpawnData().getNbt();

        final EntityArchetype archetype = EntityArchetype.builder()
                .type(type)
                .entityData(NbtTranslator.getInstance().translateFrom(data))
                .build();

        return new WeightedSerializableObject<>(archetype, weight);
    }

    private static void setNextEntity(final AbstractSpawner logic, final WeightedSerializableObject<EntityArchetype> value) {
        final CompoundNBT compound = NbtTranslator.getInstance().translate(value.get().getEntityData());
        if (!compound.contains(Constants.Entity.ENTITY_TYPE_ID)) {
            final ResourceKey key = value.get().getType().getKey();
            compound.putString(Constants.Entity.ENTITY_TYPE_ID, key.toString());
        }

        logic.setNextSpawnData(new WeightedSpawnerEntity((int) value.getWeight(), compound));
    }

    private static WeightedTable<EntityArchetype> getEntities(final AbstractSpawner logic) {
        final WeightedTable<EntityArchetype> possibleEntities = new WeightedTable<>();
        for (final WeightedSpawnerEntity weightedEntity : ((AbstractSpawnerAccessor) logic).accessor$getPotentialSpawns()) {

            final CompoundNBT nbt = weightedEntity.getNbt();

            final String resourceLocation = nbt.getString(Constants.Entity.ENTITY_TYPE_ID);
            final EntityType<?> type =
                    Registry.ENTITY_TYPE.getValue(new ResourceLocation(resourceLocation)).map(EntityType.class::cast).orElse(EntityTypes.PIG.get());

            final EntityArchetype archetype = EntityArchetype.builder()
                    .type(type)
                    .entityData(NbtTranslator.getInstance().translateFrom(nbt))
                    .build();

            possibleEntities
                    .add(new WeightedSerializableObject<>(archetype, ((WeightedRandom_ItemAccessor) weightedEntity).accessor$getItemWeight()));
        }

        return possibleEntities;
    }

    private static void setEntities(final AbstractSpawnerAccessor logic, final WeightedTable<EntityArchetype> table) {
        logic.accessor$getPotentialSpawns().clear();
        for (final TableEntry<EntityArchetype> entry : table) {
            if (!(entry instanceof WeightedObject)) {
                continue;
            }
            final WeightedObject<EntityArchetype> object = (WeightedObject<EntityArchetype>) entry;

            final CompoundNBT compound = NbtTranslator.getInstance().translate(object.get().getEntityData());
            if (!compound.contains(Constants.Entity.ENTITY_TYPE_ID)) {
                final ResourceKey key = object.get().getType().getKey();
                compound.putString(Constants.Entity.ENTITY_TYPE_ID, key.toString());
            }


            logic.accessor$getPotentialSpawns().add(new WeightedSpawnerEntity((int) entry.getWeight(), compound));
        }
    }
}
