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

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Mixin(net.minecraft.entity.Entity.class)
@Implements(@Interface(iface = org.spongepowered.api.entity.Entity.class, prefix = "entity$"))
public abstract class EntityMixin_API implements org.spongepowered.api.entity.Entity {

    // @formatter:off

    @Shadow public net.minecraft.world.World world;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public Vec3d motion;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public boolean removed;
    @Shadow private EntitySize size;
    @Shadow protected Random rand;
    @Shadow public int ticksExisted;
    @Shadow public int fire;
    @Shadow public DimensionType dimension;
    @Shadow protected UUID entityUniqueID;
    @Shadow @Final private net.minecraft.entity.EntityType<?> type;

    @Shadow public abstract void shadow$setPosition(double x, double y, double z);
    @Shadow public abstract void shadow$remove();
    @Shadow public abstract int shadow$getAir();
    @Shadow public abstract void shadow$setAir(int air);
    @Shadow public abstract UUID shadow$getUniqueID();
    @Shadow public abstract void shadow$setFire(int seconds);
    @Shadow public abstract boolean shadow$attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract int shadow$getEntityId();
    @Shadow public abstract void shadow$playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow protected abstract void shadow$setRotation(float yaw, float pitch);
    @Shadow protected abstract AxisAlignedBB shadow$getBoundingBox();
    @Shadow @Nullable public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract boolean shadow$writeUnlessRemoved(CompoundNBT compound);

    // @formatter:on

    @Override
    public EntitySnapshot createSnapshot() {
        throw new UnsupportedOperationException("implement me");
    }

    @Override
    public Random getRandom() {
        return this.rand;
    }


    public Vector3d getPosition() {
        return new Vector3d(this.posX, this.posY, this.posZ);
    }

    @Override
    public Location getLocation() {
        return Location.of((World) this.world, this.getPosition());
    }

    @Override
    public boolean setLocationAndRotation(final Location location, final Vector3d rotation) {
        final boolean result = this.setLocation(location);
        if (result) {
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
        checkNotNull(transform, "The transform cannot be null!");
        final Vector3d position = transform.getPosition();

        this.shadow$setPosition(position.getX(), position.getY(), position.getZ());
        this.setRotation(transform.getRotation());
        this.setScale(transform.getScale());
        if (!((WorldBridge) this.world).bridge$isFake() && SpongeImplHooks.onServerThread()) {
            ((ServerWorld) this.world).chunkCheck((Entity) (Object) this);
        }

        return false;
    }

    @Override
    public boolean transferToWorld(final World world, final Vector3d position) {
        checkNotNull(world, "World was null!");
        checkNotNull(position, "Position was null!");
        return this.setLocation(Location.of(world, position));
    }

    @Override
    public Vector3d getRotation() {
        return new Vector3d(this.rotationPitch, this.rotationYaw, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void setRotation(final Vector3d rotation) {
        checkNotNull(rotation, "Rotation was null!");
        if (this.isRemoved()) {
            return;
        }
        if (((Entity) (Object) this) instanceof ServerPlayerEntity && ((ServerPlayerEntity) (Entity) (Object) this).connection != null) {
            // Force an update, this also set the rotation in this entity
            ((ServerPlayerEntity) (Entity) (Object) this).connection.setPlayerLocation(this.getPosition().getX(), this.getPosition().getY(),
                    this.getPosition().getZ(), (float) rotation.getY(), (float) rotation.getX(), (Set) EnumSet.noneOf(RelativePositions.class));
        } else {
            if (!this.world.isRemote) { // We can't set the rotation update on client worlds.
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
            SpongeImpl.getLogger().error("An illegal DamageSource was provided in the cause! The damage source must extend AbstractDamageSource!");
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
    public boolean validateRawData(final DataView container) {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public void setRawData(final DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException(); // TODO Data API
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
        Constants.NBT.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
        final DataContainer unsafeNbt = NbtTranslator.getInstance().translateFrom(compound);
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, this.getContentVersion())
                .set(Constants.Entity.CLASS, this.getClass().getName())
                .set(Queries.WORLD_ID, this.getWorld().getProperties().getUniqueId().toString())
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
                .set(Constants.Entity.TYPE, net.minecraft.entity.EntityType.getKey(this.type).toString())
                .set(Constants.Sponge.UNSAFE_NBT, unsafeNbt);
        // TODO - Custom data store
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
            final Entity entity = net.minecraft.entity.EntityType.func_220335_a(compound, this.world, (createdEntity) -> {
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
    public Translation getTranslation() {
        return this.getType().getTranslation();
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

    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = new HashSet<>();

        values.add(this.displayName().asImmutable());
        values.add(this.passengers().asImmutable());
        values.add(this.onGround().asImmutable());
        values.add(this.velocity().asImmutable());
        values.add(this.gravityAffected().asImmutable());
        values.add(this.silent().asImmutable());

        this.baseVehicle().map(Value::asImmutable).ifPresent(values::add);
        this.creator().map(Value::asImmutable).ifPresent(values::add);
        this.notifier().map(Value::asImmutable).ifPresent(values::add);
        this.fireTicks().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
