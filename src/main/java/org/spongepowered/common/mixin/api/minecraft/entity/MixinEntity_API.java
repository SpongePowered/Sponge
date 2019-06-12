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
package org.spongepowered.common.mixin.api.minecraft.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGravityData;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;
import org.spongepowered.common.interfaces.world.ServerWorldBridge;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
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
public abstract class MixinEntity_API implements org.spongepowered.api.entity.Entity {

    // @formatter:off
    protected final SpongeEntityType entityType = EntityTypeRegistryModule.getInstance().getForClass(((net.minecraft.entity.Entity) (Object) this).getClass());

    @Shadow @Nullable public net.minecraft.entity.Entity ridingEntity;
    @Shadow @Final private List<net.minecraft.entity.Entity> riddenByEntities;
    @Shadow public net.minecraft.world.World world;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public double motionX;
    @Shadow public double motionY;
    @Shadow public double motionZ;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public boolean velocityChanged;
    @Shadow public boolean onGround;
    @Shadow public boolean isDead;
    @Shadow public float width;
    @Shadow public float height;
    @Shadow public float prevDistanceWalkedModified;
    @Shadow public float distanceWalkedModified;
    @Shadow public float fallDistance;
    @Shadow protected Random rand;
    @Shadow public int ticksExisted;
    @Shadow public int fire;
    @Shadow public int hurtResistantTime;
    @Shadow protected EntityDataManager dataManager;
    @Shadow public int dimension;
    @Shadow private boolean invulnerable;
    @Shadow protected UUID entityUniqueID;

    @Shadow public abstract void setPosition(double x, double y, double z);
    @Shadow public abstract void setDead();
    @Shadow public abstract int getAir();
    @Shadow public abstract void setAir(int air);
    @Shadow public abstract float getEyeHeight();
    @Shadow public abstract void setCustomNameTag(String name);
    @Shadow public abstract UUID getUniqueID();
    @Shadow public abstract AxisAlignedBB getEntityBoundingBox();
    @Shadow public abstract void setFire(int seconds);
    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    @Shadow public abstract boolean attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract int getEntityId();
    @Shadow public abstract boolean isBeingRidden();
    @Shadow public abstract SoundCategory getSoundCategory();
    @Shadow public abstract List<net.minecraft.entity.Entity> shadow$getPassengers();
    @Shadow public abstract net.minecraft.entity.Entity getLowestRidingEntity();
    @Shadow public abstract net.minecraft.entity.Entity getRidingEntity();
    @Shadow public abstract void removePassengers();
    @Shadow public abstract void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack);
    @Shadow public abstract void playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow public abstract boolean isEntityInvulnerable(DamageSource source);
    @Shadow public abstract boolean isSprinting();
    @Shadow public abstract boolean isInWater();
    @Shadow public abstract boolean isRiding();
    @Shadow public abstract boolean isOnSameTeam(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract double getDistanceSq(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void setLocationAndAngles(double x, double y, double z, float yaw, float pitch);
    @Shadow public abstract boolean hasNoGravity();
    @Shadow public abstract void setPositionAndUpdate(double x, double y, double z);
    @Shadow protected abstract void removePassenger(net.minecraft.entity.Entity passenger);
    @Shadow protected abstract void shadow$setRotation(float yaw, float pitch);
    @Shadow protected abstract void setSize(float width, float height);
    @Shadow protected abstract void applyEnchantments(EntityLivingBase entityLivingBaseIn, net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void extinguish();
    @Shadow protected abstract void setFlag(int flag, boolean set);

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
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation) {
        boolean result = setLocation(location);
        if (result) {
            setRotation(rotation);
            return true;
        }

        return false;
    }

    @Override
    public boolean setLocation(Location<World> location) {
        checkNotNull(location, "The location was null!");
        if (isRemoved()) {
            return false;
        }

        if (!WorldManager.isKnownWorld((WorldServer) location.getExtent())) {
            return false;
        }

        try (final BasicPluginContext context = PluginPhase.State.TELEPORT.createPhaseContext().buildAndSwitch()) {

            MoveEntityEvent event;

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

            final IMixinChunkProviderServer chunkProviderServer = (IMixinChunkProviderServer) ((WorldServer) this.world).getChunkProvider();
            chunkProviderServer.setForceChunkRequests(true);

            final net.minecraft.entity.Entity thisEntity = (net.minecraft.entity.Entity) (Object) this;
            final List<net.minecraft.entity.Entity> passengers = thisEntity.getPassengers();

            boolean isTeleporting = true;
            boolean isChangingDimension = false;
            if (location.getExtent().getUniqueId() != ((World) this.world).getUniqueId()) {
                if (((Entity) (Object) this) instanceof EntityPlayerMP) {
                    // Close open containers
                    final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) (Object) this;
                    if (entityPlayerMP.openContainer != entityPlayerMP.inventoryContainer) {
                        ((Player) entityPlayerMP).closeInventory(); // Call API method to make sure we capture it
                    }

                    EntityUtil.teleportPlayerToDimension(entityPlayerMP, ((ServerWorldBridge) location.getExtent()).getDimensionId(),
                        (IMixinTeleporter) ((WorldServer) location.getExtent()).getDefaultTeleporter(), event);
                } else {
                    EntityUtil.transferEntityToDimension((Entity) (Object) this, ((ServerWorldBridge) location.getExtent()).getDimensionId(),
                        (IMixinTeleporter) ((WorldServer) location.getExtent()).getDefaultTeleporter(), event);
                }

                isChangingDimension = true;
            }

            double distance = location.getPosition().distance(this.getPosition());

            if (distance <= 4) {
                isTeleporting = false;
            }

            if (thisEntity instanceof EntityPlayerMP && ((EntityPlayerMP) thisEntity).connection != null) {
                final EntityPlayerMP entityPlayerMP = (EntityPlayerMP) thisEntity;

                // No reason to attempt to load chunks unless we're teleporting
                if (isTeleporting || isChangingDimension) {
                    // Close open containers
                    if (entityPlayerMP.openContainer != entityPlayerMP.inventoryContainer) {
                        ((Player) entityPlayerMP).closeInventory(); // Call API method to make sure we capture it
                    }

                    ((WorldServer) location.getExtent()).getChunkProvider()
                        .loadChunk(location.getChunkPosition().getX(), location.getChunkPosition().getZ());
                }
                entityPlayerMP.connection
                    .setPlayerLocation(location.getX(), location.getY(), location.getZ(), thisEntity.rotationYaw, thisEntity.rotationPitch);
                ((IMixinNetHandlerPlayServer) entityPlayerMP.connection).setLastMoveLocation(null); // Set last move to teleport target
            } else {
                setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
            }

            if (isTeleporting || isChangingDimension) {
                // Re-attach passengers
                for (net.minecraft.entity.Entity passenger : passengers) {
                    if (((World) passenger.getEntityWorld()).getUniqueId() != ((World) this.world).getUniqueId()) {
                        ((MixinEntity_API) (Object) passenger).setLocation(location);
                    }
                    passenger.startRiding(thisEntity, true);
                }
            }

            chunkProviderServer.setForceChunkRequests(false);
            return true;
        }
    }

    @Override
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation, EnumSet<RelativePositions> relativePositions) {
        boolean relocated = true;

        if (relativePositions.isEmpty()) {
            // This is just a normal teleport that happens to set both.
            relocated = setLocation(location);
            setRotation(rotation);
        } else {
            if (((Entity) (Object) this) instanceof EntityPlayerMP && ((EntityPlayerMP) (Entity) (Object) this).connection != null) {
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

                ((EntityPlayerMP) ((Entity) (Object) this)).connection.setPlayerLocation(location.getPosition().getX(), location.getPosition()
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
    public void setScale(Vector3d scale) {
        // do nothing, Minecraft doesn't properly support this yet
    }

    @Override
    public Transform<World> getTransform() {
        return new Transform<>(getWorld(), getPosition(), getRotation(), getScale());
    }

    @Override
    public boolean setTransform(Transform<World> transform) {
        checkNotNull(transform, "The transform cannot be null!");
        boolean result = setLocation(transform.getLocation());
        if (result) {
            setRotation(transform.getRotation());
            setScale(transform.getScale());
            return true;
        }

        return false;
    }

    @Override
    public boolean transferToWorld(World world, Vector3d position) {
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
    public void setRotation(Vector3d rotation) {
        checkNotNull(rotation, "Rotation was null!");
        if (isRemoved()) {
            return;
        }
        if (((Entity) (Object) this) instanceof EntityPlayerMP && ((EntityPlayerMP) (Entity) (Object) this).connection != null) {
            // Force an update, this also set the rotation in this entity
            ((EntityPlayerMP) (Entity) (Object) this).connection.setPlayerLocation(getPosition().getX(), getPosition().getY(),
                getPosition().getZ(), (float) rotation.getY(), (float) rotation.getX(), (Set) EnumSet.noneOf(RelativePositions.class));
        } else {
            if (!this.world.isRemote) { // We can't set the rotation update on client worlds.
                ((ServerWorldBridge) getWorld()).addEntityRotationUpdate((Entity) (Object) this, rotation);
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
        } catch (IllegalArgumentException exception) {
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
    public boolean damage(double damage, org.spongepowered.api.event.cause.entity.damage.source.DamageSource damageSource) {
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
    public boolean hasPassenger(org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        return entity.getPassengers().contains(this);
    }

    @Override
    public boolean addPassenger(org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        if (entity.getPassengers().contains(this)) {
            throw new IllegalArgumentException(String.format("Cannot add entity %s as a passenger of %s, because the former already has the latter as a passenger!", entity, this));
        }

        return ((net.minecraft.entity.Entity) entity).startRiding((net.minecraft.entity.Entity) (Object) this, true);
    }

    @Override
    public void removePassenger(org.spongepowered.api.entity.Entity entity) {
        checkNotNull(entity);
        if (!entity.getPassengers().contains(this)) {
            throw new IllegalArgumentException(String.format("Cannot remove entity %s, because it is not a passenger of %s ", entity, this));
        }

        ((net.minecraft.entity.Entity) entity).dismountRidingEntity();
    }

    @Override
    public void clearPassengers() {
        this.removePassengers();
    }

    @Override
    public boolean setVehicle(@Nullable org.spongepowered.api.entity.Entity entity) {
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
    public boolean validateRawData(DataView container) {
        return false;
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {

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
        NbtDataUtil.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
        final DataContainer unsafeNbt = NbtTranslator.getInstance().translateFrom(compound);
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(DataQueries.ENTITY_CLASS, this.getClass().getName())
            .set(Queries.WORLD_ID, transform.getExtent().getUniqueId().toString())
            .createView(DataQueries.SNAPSHOT_WORLD_POSITION)
                .set(Queries.POSITION_X, transform.getPosition().getX())
                .set(Queries.POSITION_Y, transform.getPosition().getY())
                .set(Queries.POSITION_Z, transform.getPosition().getZ())
            .getContainer()
            .createView(DataQueries.ENTITY_ROTATION)
                .set(Queries.POSITION_X, transform.getRotation().getX())
                .set(Queries.POSITION_Y, transform.getRotation().getY())
                .set(Queries.POSITION_Z, transform.getRotation().getZ())
            .getContainer()
            .createView(DataQueries.ENTITY_SCALE)
                .set(Queries.POSITION_X, transform.getScale().getX())
                .set(Queries.POSITION_Y, transform.getScale().getY())
                .set(Queries.POSITION_Z, transform.getScale().getZ())
            .getContainer()
            .set(DataQueries.ENTITY_TYPE, this.entityType.getId())
            .set(DataQueries.UNSAFE_NBT, unsafeNbt);
        final Collection<DataManipulator<?, ?>> manipulators = ((CustomDataHolderBridge) this).getCustomManipulators();
        if (!manipulators.isEmpty()) {
            container.set(DataQueries.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
        }
        return container;
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        final List<DataManipulator<?, ?>> list = Lists.newArrayList();
        this.spongeApi$supplyVanillaManipulators(list);
        if (this instanceof CustomDataHolderBridge && ((CustomDataHolderBridge) this).hasManipulators()) {
            list.addAll(((CustomDataHolderBridge) this).getCustomManipulators());
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
            Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(this.entityType.getId()), this.world);
            compound.setUniqueId(NbtDataUtil.UUID, entity.getUniqueID());
            entity.readFromNBT(compound);
            return (org.spongepowered.api.entity.Entity) entity;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not copy the entity:", e);
        }
    }

    @Override
    public Optional<UUID> getCreator() {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> getNotifier() {
        return Optional.empty();
    }

    @Override
    public void setCreator(@Nullable UUID uuid) {
    }

    @Override
    public void setNotifier(@Nullable UUID uuid) {
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
    public boolean canSee(org.spongepowered.api.entity.Entity entity) {
        // note: this implementation will be changing with contextual data
        Optional<Boolean> optional = entity.get(Keys.VANISH);
        return (!optional.isPresent() || !optional.get()) && !((EntityBridge) entity).isVanished();
    }

    @Override
    public EntityArchetype createArchetype() {
        return new SpongeEntityArchetypeBuilder().from(this).build();
    }

    @Override
    public Value<Boolean> gravity() {
        return this.getValue(Keys.HAS_GRAVITY).get();
    }

    protected void spongeApi$supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        Optional<VehicleData> vehicleData = this.get(VehicleData.class);
        if (vehicleData.isPresent()) {
            manipulators.add(vehicleData.get());
        }
        if (this.fire > 0) {
            manipulators.add(this.get(IgniteableData.class).get());
        }
        manipulators.add(new SpongeGravityData(!this.hasNoGravity()));
    }

}
