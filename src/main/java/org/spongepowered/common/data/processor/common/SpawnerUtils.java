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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.util.weighted.TableEntry;
import org.spongepowered.api.util.weighted.WeightedObject;
import org.spongepowered.api.util.weighted.WeightedSerializableObject;
import org.spongepowered.api.util.weighted.WeightedTable;
import org.spongepowered.api.world.World;
import org.spongepowered.common.entity.SpongeEntitySnapshot;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpawnerUtils {

    @SuppressWarnings("unchecked")
    public static Optional<WeightedSerializableObject<EntitySnapshot>> getNextSnapshot(MobSpawnerBaseLogic logic) {
        int weight = 1;
        if(logic.getRandomEntity() != null) {
            weight = logic.getRandomEntity().itemWeight;
        }

        EntityType type = getType(logic.getEntityNameToSpawn()).orElse(EntityTypes.PIG);

        NBTTagCompound data = logic.getRandomEntity() == null ? null : logic.getRandomEntity().nbtData;
        Optional<Entity> nextEntity = createEntity((World) logic.getSpawnerWorld(), type, data);

        if(nextEntity.isPresent()) {
            return Optional.of(new WeightedSerializableObject<>(EntitySnapshot.builder().from(nextEntity.get()).build(), weight));
        }

        return Optional.empty();
    }

    public static void setNextSnapshot(MobSpawnerBaseLogic logic, WeightedSerializableObject<EntitySnapshot> value) {
        String name = EntityList.classToStringMapping.get(value.get().getType().getEntityClass());
        logic.mobID = name;
        // We can create based on NBT

        NBTTagCompound compound;
        if(value.get() instanceof SpongeEntitySnapshot) {
            SpongeEntitySnapshot snapshot = (SpongeEntitySnapshot) value.get();
            compound = snapshot.getCompound().orElse(new NBTTagCompound());
        } else {
            // Find a way to serialize snapshots from data not NBT
            compound = new NBTTagCompound();
        }

        logic.setRandomEntity(logic.new WeightedRandomMinecart(compound, name));
    }

    @SuppressWarnings("unchecked")
    public static WeightedTable<EntitySnapshot> getSnapshots(MobSpawnerBaseLogic logic) {
        WeightedTable<EntitySnapshot> possibleEntities = new WeightedTable<>();
        for(MobSpawnerBaseLogic.WeightedRandomMinecart weightedEntity : logic.minecartToSpawn) {
            EntityType weightedType = getType(weightedEntity.entityType).orElse(EntityTypes.PIG);
            Optional<Entity> entity = createEntity((World) logic.getSpawnerWorld(), weightedType, weightedEntity.nbtData);
            possibleEntities.add(new WeightedSerializableObject<>(EntitySnapshot.builder().from(entity.get()).build(), weightedEntity.itemWeight));
        }

        return possibleEntities;
    }

    @SuppressWarnings("unchecked")
    public static void setSnapshots(MobSpawnerBaseLogic logic, WeightedTable<EntitySnapshot> table) {
        logic.minecartToSpawn.clear();
        for (TableEntry<EntitySnapshot> entry : table) {
            if(!(entry instanceof WeightedObject)) continue;
            WeightedObject<EntitySnapshot> object = (WeightedObject<EntitySnapshot>) entry;

            NBTTagCompound compound;
            if(object.get() instanceof SpongeEntitySnapshot) {
                SpongeEntitySnapshot snapshot = (SpongeEntitySnapshot) object.get();
                compound = snapshot.getCompound().orElse(new NBTTagCompound());
            } else {
                compound = new NBTTagCompound();
            }

            String name = EntityList.classToStringMapping.get(object.get().getType().getEntityClass());
            logic.minecartToSpawn.add(logic.new WeightedRandomMinecart(compound, name));
        }
    }

    /**
     * This mimics the spawner logic to create the entity, but does not spawn
     * the created entity into the world. Ridden entities are also not created
     * as these are not needed for a snapshot.
     */
    private static Optional<Entity> createEntity(World world, EntityType type, @Nullable NBTTagCompound data) {
        Optional<Entity> entityOptional = world.createEntity(type, Vector3d.ZERO);

        if(entityOptional.isPresent()) {
            Entity entity = entityOptional.get();

            if(data != null) {
                NBTTagCompound tag = new NBTTagCompound();
                ((net.minecraft.entity.Entity) entity).writeToNBT(tag);

                for(String key : data.getKeySet()) {
                    NBTBase nbt = data.getTag(key);
                    tag.setTag(key, nbt.copy());
                }

                ((net.minecraft.entity.Entity) entity).readFromNBT(tag);
            }

            return Optional.of(entity);
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static Optional<EntityType> getType(String name) {
        // EntityList includes all forge mods with *unedited* entity names
        Class<?> clazz = EntityList.stringToClassMapping.get(name);
        if(clazz == null) return Optional.empty();

        return Optional.of(EntityTypeRegistryModule.getInstance().getForClass((Class<? extends net.minecraft.entity.Entity>) clazz));
    }
}
