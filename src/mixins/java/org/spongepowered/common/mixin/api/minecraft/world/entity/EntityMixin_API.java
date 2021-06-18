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
package org.spongepowered.common.mixin.api.minecraft.world.entity;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.HoverEvent;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.persistence.NBTTranslator;
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
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

@Mixin(net.minecraft.world.entity.Entity.class)
@Implements(@Interface(iface = org.spongepowered.api.entity.Entity.class, prefix = "entity$"))
public abstract class EntityMixin_API implements org.spongepowered.api.entity.Entity {

    // @formatter:off
    @Shadow public float yRot;
    @Shadow public float xRot;
    @Shadow public boolean removed;
    @Final @Shadow protected Random random;
    @Shadow public int tickCount;
    @Shadow protected UUID uuid;
    @Shadow @Final private net.minecraft.world.entity.EntityType<?> type;
    @Shadow public net.minecraft.world.level.Level level;

    @Shadow public abstract double shadow$getX();
    @Shadow public abstract double shadow$getY();
    @Shadow public abstract double shadow$getZ();
    @Shadow public abstract net.minecraft.world.level.Level shadow$getCommandSenderWorld();
    @Shadow @Nullable public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract void shadow$setPos(double x, double y, double z);
    @Shadow public abstract void shadow$remove();
    @Shadow public abstract UUID shadow$getUUID();
    @Shadow public abstract void shadow$setRemainingFireTicks(int ticks);
    @Shadow public abstract boolean shadow$hurt(DamageSource source, float amount);
    @Shadow public abstract int shadow$getId();
    @Shadow public abstract void shadow$playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow protected abstract void shadow$setRot(float yaw, float pitch);
    @Shadow public abstract net.minecraft.world.phys.AABB shadow$getBoundingBox();
    @Shadow public abstract boolean shadow$saveAsPassenger(CompoundTag compound);
    @Shadow @Nullable public abstract Component shadow$getCustomName();
    @Shadow public abstract Component shadow$getDisplayName();
    // @formatter:on

    @Override
    public Random random() {
        return this.random;
    }

    @Override
    public Vector3d position() {
        return new Vector3d(this.shadow$getX(), this.shadow$getY(), this.shadow$getZ());
    }

    @Override
    public World<?, ?> world() {
        return (World<?, ?>) this.level;
    }

    @Override
    public ServerLocation location() {
        return ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) this.shadow$getCommandSenderWorld(), this.position());
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
    public Vector3d scale() {
        return Vector3d.ONE;
    }

    @Override
    public void setScale(final Vector3d scale) {
        // do nothing, Minecraft doesn't properly support this yet
    }

    @Override
    public Transform transform() {
        return Transform.of(this.position(), this.rotation(), this.scale());
    }

    @Override
    public boolean setTransform(final Transform transform) {
        if (!PhaseTracker.SERVER.onSidedThread()) {
            return false;
        }
        Preconditions.checkNotNull(transform, "The transform cannot be null!");
        final Vector3d position = transform.position();
        this.shadow$setPos(position.x(), position.y(), position.z());
        this.setRotation(transform.rotation());
        this.setScale(transform.scale());
        if (!((WorldBridge) this.shadow$getCommandSenderWorld()).bridge$isFake()) {
            ((ServerLevel) this.shadow$getCommandSenderWorld()).updateChunkPos((Entity) (Object) this);
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
    public Vector3d rotation() {
        return new Vector3d(this.xRot, this.yRot, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void setRotation(final Vector3d rotation) {
        Preconditions.checkNotNull(rotation, "Rotation was null!");
        if (this.isRemoved()) {
            return;
        }
        if (((Entity) (Object) this) instanceof ServerPlayer && ((ServerPlayer) (Entity) (Object) this).connection != null) {
            // Force an update, this also set the rotation in this entity
            ((ServerPlayer) (Entity) (Object) this).connection.teleport(this.position().x(), this.position().y(),
                    this.position().z(), (float) rotation.y(), (float) rotation.x(), EnumSet.noneOf(ClientboundPlayerPositionPacket.RelativeArgument.class));
        } else {
            if (!this.shadow$getCommandSenderWorld().isClientSide) { // We can't set the rotation update on client worlds.
                ((ServerLevelBridge) this.world()).bridge$addEntityRotationUpdate((Entity) (Object) this, rotation);
            }

            // Let the entity tracker do its job, this just updates the variables
            this.shadow$setRot((float) rotation.y(), (float) rotation.x());
        }
    }

    @Override
    public Optional<AABB> boundingBox() {
        final net.minecraft.world.phys.AABB boundingBox = this.shadow$getBoundingBox();
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
        return this.shadow$hurt((DamageSource) damageSource, (float) damage);
    }

    @Override
    public EntityType type() {
        return (EntityType) this.type;
    }

    @Override
    public UUID uniqueId() {
        return this.uuid;
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final Transform transform = this.transform();
        final CompoundTag compound = new CompoundTag();
        this.shadow$saveAsPassenger(compound);
        final DataContainer unsafeNbt = NBTTranslator.INSTANCE.translateFrom(compound);
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.contentVersion())
                .set(Constants.Entity.CLASS, this.getClass().getName())
                .set(Queries.WORLD_KEY, ((org.spongepowered.api.world.server.ServerWorld) this.world()).key().formatted())
                .createView(Constants.Sponge.SNAPSHOT_WORLD_POSITION)
                .set(Queries.POSITION_X, transform.position().x())
                .set(Queries.POSITION_Y, transform.position().y())
                .set(Queries.POSITION_Z, transform.position().z())
                .container()
                .createView(Constants.Entity.ROTATION)
                .set(Queries.POSITION_X, transform.rotation().x())
                .set(Queries.POSITION_Y, transform.rotation().y())
                .set(Queries.POSITION_Z, transform.rotation().z())
                .container()
                .createView(Constants.Entity.SCALE)
                .set(Queries.POSITION_X, transform.scale().x())
                .set(Queries.POSITION_Y, transform.scale().y())
                .set(Queries.POSITION_Z, transform.scale().z())
                .container()
                .set(Constants.Entity.TYPE, Registry.ENTITY_TYPE.getKey((net.minecraft.world.entity.EntityType<?>) this.type()))
                .set(Constants.Sponge.UNSAFE_NBT, unsafeNbt);
        return container;
    }

    @Override
    public org.spongepowered.api.entity.Entity copy() {
        if ((Object) this instanceof Player) {
            throw new IllegalArgumentException("Cannot copy player entities!");
        }
        try {
            final CompoundTag compound = new CompoundTag();
            this.shadow$saveAsPassenger(compound);
            final Entity entity = net.minecraft.world.entity.EntityType.loadEntityRecursive(compound, this.shadow$getCommandSenderWorld(), (createdEntity) -> {
                createdEntity.setUUID(UUID.randomUUID());
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

    @Override
    public HoverEvent<HoverEvent.ShowEntity> asHoverEvent(final UnaryOperator<HoverEvent.ShowEntity> op) {
        final ResourceLocation entityTypeKey = Registry.ENTITY_TYPE.getKey((net.minecraft.world.entity.EntityType<?>) this.type());
        return HoverEvent.showEntity(op.apply(HoverEvent.ShowEntity.of((Key) (Object) entityTypeKey, this.uniqueId(), this.displayName().get())));
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
