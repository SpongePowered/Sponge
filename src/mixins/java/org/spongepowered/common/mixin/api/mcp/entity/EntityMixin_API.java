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
package org.spongepowered.common.mixin.api.mcp.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(net.minecraft.entity.Entity.class)
@Implements(@Interface(iface = org.spongepowered.api.entity.Entity.class, prefix = "entity$"))
public abstract class EntityMixin_API implements org.spongepowered.api.entity.Entity {

    // @formatter:off

    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public boolean removed;
    @Final @Shadow protected Random rand;
    @Shadow public int ticksExisted;
    @Shadow public DimensionType dimension;
    @Shadow protected UUID entityUniqueID;
    @Shadow @Final private net.minecraft.entity.EntityType<?> type;

    @Shadow public abstract double shadow$getPosX();
    @Shadow public abstract double shadow$getPosY();
    @Shadow public abstract double shadow$getPosZ();
    @Shadow public abstract net.minecraft.world.World shadow$getEntityWorld();
    @Shadow @Nullable public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract void shadow$setPosition(double x, double y, double z);
    @Shadow public abstract void shadow$remove();
    @Shadow public abstract UUID shadow$getUniqueID();
    @Shadow public abstract void shadow$setFire(int seconds);
    @Shadow public abstract boolean shadow$attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract int shadow$getEntityId();
    @Shadow public abstract void shadow$playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow protected abstract void shadow$setRotation(float yaw, float pitch);
    @Shadow public abstract AxisAlignedBB shadow$getBoundingBox();
    @Shadow public abstract boolean shadow$writeUnlessRemoved(CompoundNBT compound);
    @Shadow @Nullable public abstract ITextComponent shadow$getCustomName();
    @Shadow public abstract ITextComponent shadow$getDisplayName();

    // @formatter:on

    @Override
    public Random getRandom() {
        return this.rand;
    }

    @Override
    public Vector3d getPosition() {
        return new Vector3d(this.shadow$getPosX(), this.shadow$getPosY(), this.shadow$getPosZ());
    }

    @Override
    public ServerLocation getLocation() {
        return ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) this.shadow$getEntityWorld(), this.getPosition());
    }

    @Override
    public boolean setLocation(ServerLocation location) {
        Preconditions.checkNotNull(location, "The location was null!");
        return ((EntityBridge) this).bridge$setLocation(location);
    }

    @Override
    public boolean setLocationAndRotation(ServerLocation location, Vector3d rotation) {
        if (this.setLocation(location)) {
            this.setRotation(rotation);
            return true;
        }
        return false;
    }

    @Override
    public Vector3d getScale() {
        return Vector3d.ONE;
    }

    @Override
    public void setScale(final Vector3d scale) {
        // do nothing, Minecraft doesn't properly support this yet
    }

    @Override
    public Transform getTransform() {
        return Transform.of(this.getPosition(), this.getRotation(), this.getScale());
    }

    @Override
    public boolean setTransform(final Transform transform) {
        if (!PhaseTracker.SERVER.onSidedThread()) {
            return false;
        }
        Preconditions.checkNotNull(transform, "The transform cannot be null!");
        final Vector3d position = transform.getPosition();
        this.shadow$setPosition(position.getX(), position.getY(), position.getZ());
        this.setRotation(transform.getRotation());
        this.setScale(transform.getScale());
        if (!((WorldBridge) this.shadow$getEntityWorld()).bridge$isFake()) {
            ((ServerWorld) this.shadow$getEntityWorld()).chunkCheck((Entity) (Object) this);
        }

        return true;
    }

    @Override
    public boolean transferToWorld(final org.spongepowered.api.world.server.ServerWorld world, final Vector3d position) {
        Preconditions.checkNotNull(world, "World was null!");
        Preconditions.checkNotNull(position, "Position was null!");
        return this.setLocation(ServerLocation.of(world, position));
    }

    @Override
    public Vector3d getRotation() {
        return new Vector3d(this.rotationPitch, this.rotationYaw, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void setRotation(final Vector3d rotation) {
        Preconditions.checkNotNull(rotation, "Rotation was null!");
        if (this.isRemoved()) {
            return;
        }
        if (((Entity) (Object) this) instanceof ServerPlayerEntity && ((ServerPlayerEntity) (Entity) (Object) this).connection != null) {
            // Force an update, this also set the rotation in this entity
            ((ServerPlayerEntity) (Entity) (Object) this).connection.setPlayerLocation(this.getPosition().getX(), this.getPosition().getY(),
                    this.getPosition().getZ(), (float) rotation.getY(), (float) rotation.getX(), EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class));
        } else {
            if (!this.shadow$getEntityWorld().isRemote) { // We can't set the rotation update on client worlds.
                ((ServerWorldBridge) this.getWorld()).bridge$addEntityRotationUpdate((Entity) (Object) this, rotation);
            }

            // Let the entity tracker do its job, this just updates the variables
            this.shadow$setRotation((float) rotation.getY(), (float) rotation.getX());
        }
    }

    @Override
    public Optional<AABB> getBoundingBox() {
        final AxisAlignedBB boundingBox = this.shadow$getBoundingBox();
        if (boundingBox == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(VecHelper.toSpongeAABB(boundingBox));
        } catch (final IllegalArgumentException exception) {
            // Bounding box is degenerate, the entity doesn't actually have one
            return Optional.empty();
        }
    }

    @Override
    public boolean isRemoved() {
        return this.removed;
    }

    @Override
    public boolean isLoaded() {
        // TODO - add flag for entities loaded/unloaded into world
        return !this.isRemoved();
    }

    @Intrinsic
    public void entity$remove() {
        this.shadow$remove();
    }

    @Override
    public boolean damage(final double damage, final org.spongepowered.api.event.cause.entity.damage.source.DamageSource damageSource) {
        if (!(damageSource instanceof DamageSource)) {
            SpongeCommon.getLogger().error("An illegal DamageSource was provided in the cause! The damage source must extend AbstractDamageSource!");
            return false;
        }
        // Causes at this point should already be pushed from plugins before this point with the cause system.
        return this.shadow$attackEntityFrom((DamageSource) damageSource, (float) damage);
    }

    @Override
    public EntityType getType() {
        return (EntityType) this.type;
    }

    @Override
    public UUID getUniqueId() {
        return this.entityUniqueID;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final Transform transform = this.getTransform();
        final CompoundNBT compound = new CompoundNBT();
        this.shadow$writeUnlessRemoved(compound);
        final DataContainer unsafeNbt = NbtTranslator.getInstance().translateFrom(compound);
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.getContentVersion())
                .set(Constants.Entity.CLASS, this.getClass().getName())
                .set(Queries.WORLD_KEY, ((org.spongepowered.api.world.server.ServerWorld) this.getWorld()).getKey().getFormatted())
                .createView(Constants.Sponge.SNAPSHOT_WORLD_POSITION)
                .set(Queries.POSITION_X, transform.getPosition().getX())
                .set(Queries.POSITION_Y, transform.getPosition().getY())
                .set(Queries.POSITION_Z, transform.getPosition().getZ())
                .getContainer()
                .createView(Constants.Entity.ROTATION)
                .set(Queries.POSITION_X, transform.getRotation().getX())
                .set(Queries.POSITION_Y, transform.getRotation().getY())
                .set(Queries.POSITION_Z, transform.getRotation().getZ())
                .getContainer()
                .createView(Constants.Entity.SCALE)
                .set(Queries.POSITION_X, transform.getScale().getX())
                .set(Queries.POSITION_Y, transform.getScale().getY())
                .set(Queries.POSITION_Z, transform.getScale().getZ())
                .getContainer()
                .set(Constants.Entity.TYPE, this.getType().getKey())
                .set(Constants.Sponge.UNSAFE_NBT, unsafeNbt);
        return container;
    }

    @Override
    public org.spongepowered.api.entity.Entity copy() {
        if ((Object) this instanceof Player) {
            throw new IllegalArgumentException("Cannot copy player entities!");
        }
        try {
            final CompoundNBT compound = new CompoundNBT();
            this.shadow$writeUnlessRemoved(compound);
            final Entity entity = net.minecraft.entity.EntityType.loadEntityAndExecute(compound, this.shadow$getEntityWorld(), (createdEntity) -> {
                compound.putUniqueId(Constants.UUID, createdEntity.getUniqueID());
                createdEntity.read(compound);
                return createdEntity;
            });
            return (org.spongepowered.api.entity.Entity) entity;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not copy the entity:", e);
        }
    }

    @Override
    public boolean canSee(final org.spongepowered.api.entity.Entity entity) {
        // note: this implementation will be changing with contextual data
        final Optional<Boolean> optional = entity.get(Keys.VANISH);
        return (!optional.isPresent() || !optional.get()) && !((VanishableBridge) entity).bridge$isVanished();
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        // TODO: Merge custom and Vanilla values and return the merged result.
        return this.api$getVanillaValues();
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return EntitySnapshot.builder().from(this).build();
    }

    @Override
    public EntityArchetype createArchetype() {
        return EntityArchetype.builder().from(this).build();
    }

    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = new HashSet<>();

        values.add(this.displayName().asImmutable());
        values.add(this.fallDistance().asImmutable());
        values.add(this.passengers().asImmutable());
        values.add(this.onGround().asImmutable());
        values.add(this.velocity().asImmutable());
        values.add(this.gravityAffected().asImmutable());
        values.add(this.silent().asImmutable());

        this.baseVehicle().map(Value::asImmutable).ifPresent(values::add);
        this.creator().map(Value::asImmutable).ifPresent(values::add);
        this.notifier().map(Value::asImmutable).ifPresent(values::add);
        this.fireTicks().map(Value::asImmutable).ifPresent(values::add);
        this.fireImmuneTicks().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
