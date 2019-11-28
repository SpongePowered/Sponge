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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.VehicleData;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkProviderServerBridge;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGravityData;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.bridge.network.NetHandlerPlayServerBridge;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(net.minecraft.entity.Entity.class)
@Implements(@Interface(iface = org.spongepowered.api.entity.Entity.class, prefix = "entity$"))
public abstract class EntityMixin_API implements org.spongepowered.api.entity.Entity {

    // @formatter:off
    protected final SpongeEntityType entityType = EntityTypeRegistryModule.getInstance().getForClass(((net.minecraft.entity.Entity) (Object) this).getClass());

    @Shadow public net.minecraft.world.World world;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public double motionX;
    @Shadow public double motionY;
    @Shadow public double motionZ;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public boolean onGround;
    @Shadow public boolean isDead;
    @Shadow public float width;
    @Shadow public float height;
    @Shadow protected Random rand;
    @Shadow public int ticksExisted;
    @Shadow public int fire;
    @Shadow protected EntityDataManager dataManager;
    @Shadow public int dimension;
    @Shadow protected UUID entityUniqueID;

    @Shadow public abstract void setPosition(double x, double y, double z);
    @Shadow public abstract void setDead();
    @Shadow public abstract int getAir();
    @Shadow public abstract void setAir(int air);
    @Shadow public abstract float getEyeHeight();
    @Shadow public abstract UUID getUniqueID();
    @Shadow public abstract AxisAlignedBB getEntityBoundingBox();
    @Shadow public abstract void setFire(int seconds);
    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    @Shadow public abstract boolean attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract int getEntityId();
    @Shadow public abstract List<net.minecraft.entity.Entity> shadow$getPassengers();
    @Shadow public abstract net.minecraft.entity.Entity getLowestRidingEntity();
    @Shadow public abstract net.minecraft.entity.Entity getRidingEntity();
    @Shadow public abstract void removePassengers();
    @Shadow public abstract void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack);
    @Shadow public abstract void playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow public abstract boolean hasNoGravity();
    @Shadow protected abstract void shadow$setRotation(float yaw, float pitch);

    // @formatter:on

    @Shadow public abstract void dismountRidingEntity();

    @Override
    public EntitySnapshot createSnapshot() {
        return new SpongeEntitySnapshotBuilder().from(this).build();
    }

    @Override
    public Random getRandom() {
        return this.rand;
    }


    public Vector3d getPosition() {
        return new Vector3d(this.posX, this.posY, this.posZ);
    }

    @Override
    public Location<World> getLocation() {
        return new Location<>((World) this.world, getPosition());
    }

    @Override
    public boolean setLocationAndRotation(final Location<World> location, final Vector3d rotation) {
        final boolean result = setLocation(location);
        if (result) {
            setRotation(rotation);
            return true;
        }

        return false;
    }

    @SuppressWarnings({"ConstantConditions", "RedundantCast"})
    @Override
    public boolean setLocation(Location<World> location) {
        checkNotNull(location, "The location was null!");
        if (isRemoved()) {
            return false;
        }

        if (!WorldManager.isKnownWorld((WorldServer) location.getExtent())) {
            return false;
        }

        try (final BasicPluginContext context = PluginPhase.State.TELEPORT.createPhaseContext()) {
            context.buildAndSwitch();

            MoveEntityEvent.Teleport event;

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                if (!frame.getCurrentContext().containsKey(EventContextKeys.TELEPORT_TYPE)) {
                    frame.addContext(EventContextKeys.TELEPORT_TYPE, TeleportTypes.PLUGIN);
                }

                event = EntityUtil.handleDisplaceEntityTeleportEvent((net.minecraft.entity.Entity) (Object) this, location);
                if (event.isCancelled()) {
                    return false;
                }

                location = event.getToTransform().getLocation();
                this.rotationPitch = (float) event.getToTransform().getPitch();
                this.rotationYaw = (float) event.getToTransform().getYaw();
            }

            final ChunkProviderServerBridge chunkProviderServer = (ChunkProviderServerBridge) ((WorldServer) this.world).func_72863_F();
            final boolean previous = chunkProviderServer.bridge$getForceChunkRequests();
            chunkProviderServer.bridge$setForceChunkRequests(true);
            try {
                final List<net.minecraft.entity.Entity> passengers = ((Entity) (Object) this).func_184188_bt();

                boolean isTeleporting = true;
                boolean isChangingDimension = false;
                if (location.getExtent().getUniqueId() != ((World) this.world).getUniqueId()) {
                    if ((net.minecraft.entity.Entity) (Object) this instanceof EntityPlayerMP) {
                        // Close open containers
                        final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) (Object) this;
                        if (entityPlayerMP.field_71070_bA != entityPlayerMP.field_71069_bz) {
                            ((Player) entityPlayerMP).closeInventory(); // Call API method to make sure we capture it
                        }

                        EntityUtil.transferPlayerToWorld(entityPlayerMP, event, (WorldServer) location.getExtent(),
                            (TeleporterBridge) ((WorldServer) location.getExtent()).func_85176_s());
                    } else {
                        EntityUtil.transferEntityToWorld((Entity) (Object) this, event, (WorldServer) location.getExtent(),
                            (TeleporterBridge) ((WorldServer) location.getExtent()).func_85176_s(), false);
                    }

                    isChangingDimension = true;
                }

                double distance = location.getPosition().distance(this.getPosition());

                if (distance <= 4) {
                    isTeleporting = false;
                }

                if ((Entity) (Object) this instanceof EntityPlayerMP && ((EntityPlayerMP) (Entity) (Object) this).field_71135_a != null) {
                    final EntityPlayerMP player = (EntityPlayerMP) (Entity) (Object) this;

                    // No reason to attempt to load chunks unless we're teleporting
                    if (isTeleporting || isChangingDimension) {
                        // Close open containers
                        if (player.field_71070_bA != player.field_71069_bz) {
                            ((Player) player).closeInventory(); // Call API method to make sure we capture it
                        }

                        ((WorldServer) location.getExtent()).func_72863_F()
                            .func_186028_c(location.getChunkPosition().getX(), location.getChunkPosition().getZ());
                    }
                    player.field_71135_a
                        .func_147364_a(location.getX(), location.getY(), location.getZ(), ((Entity) (Object) this).field_70177_z,
                            ((Entity) (Object) this).field_70125_A);
                    ((NetHandlerPlayServerBridge) player.field_71135_a).bridge$setLastMoveLocation(null); // Set last move to teleport target
                } else {
                    setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
                }

                if (isTeleporting || isChangingDimension) {
                    // Re-attach passengers
                    for (net.minecraft.entity.Entity passenger : passengers) {
                        if (((World) passenger.func_130014_f_()).getUniqueId() != ((World) this.world).getUniqueId()) {
                            ((org.spongepowered.api.entity.Entity) passenger).setLocation(location);
                        }
                        passenger.func_184205_a((Entity) (Object) this, true);
                    }
                }
                return true;
            } finally {
                chunkProviderServer.bridge$setForceChunkRequests(previous);
            }

        }
    }

    @SuppressWarnings({"RedundantCast", "ConstantConditions"})
    @Override
    public boolean setLocationAndRotation(final Location<World> location, final Vector3d rotation, final EnumSet<RelativePositions> relativePositions) {
        boolean relocated = true;

        if (relativePositions.isEmpty()) {
            // This is just a normal teleport that happens to set both.
            relocated = setLocation(location);
            setRotation(rotation);
        } else {
            if (((Entity) (Object) this) instanceof EntityPlayerMP && ((EntityPlayerMP) (Entity) (Object) this).field_71135_a != null) {
                // Players use different logic, as they support real relative movement.
                EnumSet<SPacketPlayerPosLook.EnumFlags> relativeFlags = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);

                if (relativePositions.contains(RelativePositions.X)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.X);
                }

                if (relativePositions.contains(RelativePositions.Y)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.Y);
                }

                if (relativePositions.contains(RelativePositions.Z)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.Z);
                }

                if (relativePositions.contains(RelativePositions.PITCH)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
                }

                if (relativePositions.contains(RelativePositions.YAW)) {
                    relativeFlags.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
                }

                ((EntityPlayerMP) ((Entity) (Object) this)).field_71135_a.func_175089_a(location.getPosition().getX(), location.getPosition()
                    .getY(), location.getPosition().getZ(), (float) rotation.getY(), (float) rotation.getX(), relativeFlags);
            } else {
                Location<World> resultantLocation = getLocation();
                Vector3d resultantRotation = getRotation();

                if (relativePositions.contains(RelativePositions.X)) {
                    resultantLocation = resultantLocation.add(location.getPosition().getX(), 0, 0);
                }

                if (relativePositions.contains(RelativePositions.Y)) {
                    resultantLocation = resultantLocation.add(0, location.getPosition().getY(), 0);
                }

                if (relativePositions.contains(RelativePositions.Z)) {
                    resultantLocation = resultantLocation.add(0, 0, location.getPosition().getZ());
                }

                if (relativePositions.contains(RelativePositions.PITCH)) {
                    resultantRotation = resultantRotation.add(rotation.getX(), 0, 0);
                }

                if (relativePositions.contains(RelativePositions.YAW)) {
                    resultantRotation = resultantRotation.add(0, rotation.getY(), 0);
                }

                // From here just a normal teleport is needed.
                relocated = setLocation(resultantLocation);
                setRotation(resultantRotation);
            }
        }
        return relocated;
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
    public Transform<World> getTransform() {
        return new Transform<>(getWorld(), getPosition(), getRotation(), getScale());
    }

    @Override
    public boolean setTransform(final Transform<World> transform) {
        checkNotNull(transform, "The transform cannot be null!");
        final boolean result = setLocation(transform.getLocation());
        if (result) {
            setRotation(transform.getRotation());
            setScale(transform.getScale());
            return true;
        }

        return false;
    }

    @Override
    public boolean transferToWorld(final World world, final Vector3d position) {
        checkNotNull(world, "World was null!");
        checkNotNull(position, "Position was null!");
        return setLocation(new Location<>(world, position));
    }

    @Override
    public Vector3d getRotation() {
        return new Vector3d(this.rotationPitch, this.rotationYaw, 0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void setRotation(final Vector3d rotation) {
        checkNotNull(rotation, "Rotation was null!");
        if (isRemoved()) {
            return;
        }
        if (((Entity) (Object) this) instanceof EntityPlayerMP && ((EntityPlayerMP) (Entity) (Object) this).field_71135_a != null) {
            // Force an update, this also set the rotation in this entity
            ((EntityPlayerMP) (Entity) (Object) this).field_71135_a.func_175089_a(getPosition().getX(), getPosition().getY(),
                getPosition().getZ(), (float) rotation.getY(), (float) rotation.getX(), (Set) EnumSet.noneOf(RelativePositions.class));
        } else {
            if (!this.world.field_72995_K) { // We can't set the rotation update on client worlds.
                ((WorldServerBridge) getWorld()).bridge$addEntityRotationUpdate((Entity) (Object) this, rotation);
            }

            // Let the entity tracker do its job, this just updates the variables
            shadow$setRotation((float) rotation.getY(), (float) rotation.getX());
        }
    }

    @Override
    public Optional<AABB> getBoundingBox() {
        final AxisAlignedBB boundingBox = getEntityBoundingBox();
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
    public boolean isOnGround() {
        return this.onGround;
    }

    @Override
    public boolean isRemoved() {
        return this.isDead;
    }

    @Override
    public boolean isLoaded() {
        // TODO - add flag for entities loaded/unloaded into world
        return !isRemoved();
    }

    @Override
    public void remove() {
        this.isDead = true;
    }

    @Override
    public boolean damage(final double damage, final org.spongepowered.api.event.cause.entity.damage.source.DamageSource damageSource) {
        if (!(damageSource instanceof DamageSource)) {
            SpongeImpl.getLogger().error("An illegal DamageSource was provided in the cause! The damage source must extend AbstractDamageSource!");
            return false;
        }
        // Causes at this point should already be pushed from plugins before this point with the cause system.
        return attackEntityFrom((DamageSource) damageSource, (float) damage);
    }

    @Override
    public EntityType getType() {
        return this.entityType;
    }

    @Override
    public UUID getUniqueId() {
        return this.entityUniqueID;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Intrinsic
    public List<org.spongepowered.api.entity.Entity> entity$getPassengers() {
        return (List) shadow$getPassengers();
    }

    @Override
    public Optional<org.spongepowered.api.entity.Entity> getVehicle() {
        return Optional.ofNullable((org.spongepowered.api.entity.Entity) getRidingEntity());
    }

    @Override
    public org.spongepowered.api.entity.Entity getBaseVehicle() {
        return (org.spongepowered.api.entity.Entity) this.getLowestRidingEntity();
    }

    @Override
    public boolean hasPassenger(final org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        return entity.getPassengers().contains(this);
    }

    @Override
    public boolean addPassenger(final org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        if (entity.getPassengers().contains(this)) {
            throw new IllegalArgumentException(String.format("Cannot add entity %s as a passenger of %s, because the former already has the latter as a passenger!", entity, this));
        }

        return ((net.minecraft.entity.Entity) entity).func_184205_a((net.minecraft.entity.Entity) (Object) this, true);
    }

    @Override
    public void removePassenger(final org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        if (!entity.getPassengers().contains(this)) {
            throw new IllegalArgumentException(String.format("Cannot remove entity %s, because it is not a passenger of %s ", entity, this));
        }

        ((net.minecraft.entity.Entity) entity).func_184210_p();
    }

    @Override
    public void clearPassengers() {
        this.removePassengers();
    }

    @Override
    public boolean setVehicle(@Nullable final org.spongepowered.api.entity.Entity entity) {
        if (getRidingEntity() == null && entity == null) {
            return false;
        }
        if (getRidingEntity() != null) {
            this.dismountRidingEntity();
            return true;
        }
        return entity != null && entity.addPassenger(this);
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
        final Transform<World> transform = getTransform();
        final NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        Constants.NBT.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
        final DataContainer unsafeNbt = NbtTranslator.getInstance().translateFrom(compound);
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(Constants.Entity.CLASS, this.getClass().getName())
            .set(Queries.WORLD_ID, transform.getExtent().getUniqueId().toString())
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
            .set(Constants.Entity.TYPE, this.entityType.getId())
            .set(Constants.Sponge.UNSAFE_NBT, unsafeNbt);
        final Collection<DataManipulator<?, ?>> manipulators = ((CustomDataHolderBridge) this).bridge$getCustomManipulators();
        if (!manipulators.isEmpty()) {
            container.set(Constants.Sponge.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
        }
        return container;
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        final List<DataManipulator<?, ?>> list = Lists.newArrayList();
        this.spongeApi$supplyVanillaManipulators(list);
        if (this instanceof CustomDataHolderBridge && ((CustomDataHolderBridge) this).bridge$hasManipulators()) {
            list.addAll(((CustomDataHolderBridge) this).bridge$getCustomManipulators());
        }
        return list;
    }

    @Override
    public DataHolder copy() {
        if ((Object) this instanceof Player) {
            throw new IllegalArgumentException("Cannot copy player entities!");
        }
        try {
            final NBTTagCompound compound = new NBTTagCompound();
            writeToNBT(compound);
            final Entity entity = EntityList.func_188429_b(new ResourceLocation(this.entityType.getId()), this.world);
            compound.func_186854_a(Constants.UUID, entity.func_110124_au());
            entity.func_70020_e(compound);
            return (org.spongepowered.api.entity.Entity) entity;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not copy the entity:", e);
        }
    }

    @Override
    public Optional<UUID> getCreator() {
        return Optional.empty(); // Mixed in via EntityMixin_TrackerAPI
    }

    @Override
    public Optional<UUID> getNotifier() {
        return Optional.empty(); // Mixed in via EntityMixin_TrackerAPI
    }

    @Override
    public void setCreator(@Nullable final UUID uuid) {  // Mixed in via EntityMixin_TrackerAPI
    }

    @Override
    public void setNotifier(@Nullable final UUID uuid) { // Mixed in via EntityMixin_TrackerAPI
    }

    @Override
    public Vector3d getVelocity() {
        return new Vector3d(this.motionX, this.motionY, this.motionZ);
    }

    @Override
    public Translation getTranslation() {
        return getType().getTranslation();
    }

    @Override
    public boolean canSee(final org.spongepowered.api.entity.Entity entity) {
        // note: this implementation will be changing with contextual data
        final Optional<Boolean> optional = entity.get(Keys.VANISH);
        return (!optional.isPresent() || !optional.get()) && !((VanishableBridge) entity).bridge$isVanished();
    }

    @Override
    public EntityArchetype createArchetype() {
        return new SpongeEntityArchetypeBuilder().from(this).build();
    }

    @Override
    public Value<Boolean> gravity() {
        return this.getValue(Keys.HAS_GRAVITY).get();
    }

    protected void spongeApi$supplyVanillaManipulators(final Collection<? super DataManipulator<?, ?>> manipulators) {
        this.get(VehicleData.class).ifPresent(manipulators::add);
        if (this.fire > 0) {
            manipulators.add(this.get(IgniteableData.class).get());
        }
        manipulators.add(new SpongeGravityData(!this.hasNoGravity()));
    }

}
