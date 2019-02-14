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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnType;
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
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SpongeEntityArchetype extends AbstractArchetype<EntityType, EntitySnapshot, org.spongepowered.api.entity.Entity> implements EntityArchetype {

    SpongeEntityArchetype(SpongeEntityArchetypeBuilder builder) {
        super(builder.entityType, NbtTranslator.getInstance().translateData(builder.entityData));
    }

    @Override
    public EntityType getType() {
        return this.type;
    }

    @Override
    public DataContainer getEntityData() {
        return NbtTranslator.getInstance().translateFrom(this.data);
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> apply(Location location) {
        final Vector3d position = location.getPosition();
        final double x = position.getX();
        final double y = position.getY();
        final double z = position.getZ();
        final BlockPos blockPos = new BlockPos(x, y, z);
        final World world = location.getWorld();
        final WorldServer worldServer = (WorldServer) world;

        final Entity entity = ((SpongeEntityType) this.type).type.create(worldServer);
        if (entity == null) {
            return Optional.empty();
        }

        this.data.put("Pos", NbtDataUtil.newDoubleNBTList(x, y, z));
        this.data.putInt("Dimension", ((IMixinWorldInfo) location.getWorld().getProperties()).getDimensionId());
        entity.read(this.data);
        this.data.remove("Pos");
        this.data.remove("Dimension");

        final org.spongepowered.api.entity.Entity spongeEntity = EntityUtil.fromNative(entity);
        final List<org.spongepowered.api.entity.Entity> entities = new ArrayList<>();
        entities.add(spongeEntity);
        // We require spawn types. This is more of a sanity check to throw an IllegalStateException otherwise for the plugin developer to properly associate the type.
        final SpawnType require = Sponge.getCauseStackManager().getCurrentContext().require(EventContextKeys.SPAWN_TYPE);
        final SpawnEntityEvent.Custom event = SpongeEventFactory.createSpawnEntityEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
        if (!event.isCancelled()) {
            final IMixinWorldServer mixinWorldServer = (IMixinWorldServer) worldServer;
            entity.setPositionAndRotation(x, y, z, entity.rotationYaw, entity.rotationPitch);
            if (entity instanceof EntityLiving) {
                // This is ok to force spawn since we aren't considering custom items.
                mixinWorldServer.forceSpawnEntity(EntityUtil.fromNative(entity));
                ((EntityLiving) entity).onInitialSpawn(worldServer.getDifficultyForLocation(blockPos), null, null);
                ((EntityLiving) entity).spawnExplosionParticle();
            } else {
                // This is ok to force spawn since we aren't considering custom items.
                mixinWorldServer.forceSpawnEntity(EntityUtil.fromNative(entity));
            }
            return Optional.of(spongeEntity);
        }
        return Optional.empty();
    }

    @Override
    public EntitySnapshot toSnapshot(Location location) {
        final SpongeEntitySnapshotBuilder builder = new SpongeEntitySnapshotBuilder();
        builder.entityType = this.type;
        NBTTagCompound newCompound = this.data.copy();
        newCompound.put("Pos", NbtDataUtil
                .newDoubleNBTList(new double[] { location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ() }));
        newCompound.putInt("Dimension", ((IMixinWorldInfo) location.getWorld().getProperties()).getDimensionId());
        builder.compound = newCompound;
        builder.worldId = location.getWorld().getUniqueId();
        builder.position = location.getPosition();
        return builder.build();
    }

    @Override
    public int getContentVersion() {
        return DataVersions.EntityArchetype.BASE_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(DataQueries.EntityArchetype.ENTITY_TYPE, this.type)
                .set(DataQueries.EntityArchetype.ENTITY_DATA, getEntityData())
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
