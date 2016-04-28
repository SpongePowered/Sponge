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
package org.spongepowered.common.entity;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.AbstractArchetype;
import org.spongepowered.common.data.nbt.NbtDataType;
import org.spongepowered.common.data.nbt.NbtDataTypes;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataVersions;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.Arrays;

public class SpongeEntityArchetype extends AbstractArchetype<EntityType, EntitySnapshot> implements EntityArchetype {

    final EntityType entityType;

    SpongeEntityArchetype(SpongeEntityArchetypeBuilder builder) {
        super(builder.entityType, NbtTranslator.getInstance().translateData(builder.entityData));
        this.entityType = builder.entityType;
    }

    @Override
    public EntityType getType() {
        return this.entityType;
    }

    @Override
    public DataContainer getEntityData() {
        return NbtTranslator.getInstance().translateFrom(this.data);
    }

    @Override
    public boolean apply(Location<World> location, Cause cause) {
        final NBTTagCompound data = (NBTTagCompound) this.data.copy();
        final Vector3d position = location.getPosition();
        final double x = position.getX();
        final double y = position.getY();
        final double z = position.getZ();
        final BlockPos blockPos = new BlockPos(x, y, z);
        final World world = location.getExtent();
        final WorldServer worldServer = (WorldServer) world;

        final Entity entity = EntityList.createEntityFromNBT(data, worldServer);

        final org.spongepowered.api.entity.Entity spongeEntity = EntityUtil.fromNative(entity);
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(cause, Arrays.asList(spongeEntity),
                ImmutableList.of(spongeEntity.createSnapshot()), world);
        if (!event.isCancelled()) {
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldServer;
            entity.setPositionAndRotation(x, y, z, entity.rotationYaw, entity.rotationPitch);
            if (entity instanceof EntityLiving) {
                mixinWorldServer.forceSpawnEntity(entity);
                ((EntityLiving) entity).onInitialSpawn(worldServer.getDifficultyForLocation(blockPos), null);
                ((EntityLiving) entity).spawnExplosionParticle();
            } else {
                mixinWorldServer.forceSpawnEntity(entity);
            }
            return true;
        }
        return false;
    }

    @Override
    public EntitySnapshot toSnapshot(Location<World> location) {
        final SpongeEntitySnapshotBuilder builder = new SpongeEntitySnapshotBuilder();
        builder.entityType = this.entityType;
        builder.compound = (NBTTagCompound) this.data.copy();
        builder.worldId = location.getExtent().getUniqueId();
        builder.position = location.getPosition();
        return builder.build();
    }

    @Override
    public int getContentVersion() {
        return DataVersions.EntityArchetype.BASE_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(DataQueries.EntityArchetype.ENTITY_TYPE, this.type)
                .set(DataQueries.EntityArchetype.ENTITY_DATA, this.data)
                ;
    }

    @Override
    protected NbtDataType getDataType() {
        return NbtDataTypes.ENTITY;
    }

    @Override
    protected ValidationType getValidationType() {
        return Validations.ENTITY;
    }

    @Override
    public EntityArchetype copy() {
        final SpongeEntityArchetypeBuilder builder = new SpongeEntityArchetypeBuilder();
        builder.entityType = this.type;
        builder.entityData = NbtTranslator.getInstance().translate(this.data);
        return builder.build();
    }
}
