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
package org.spongepowered.common.mixin.core.entity;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.interfaces.IMixinEntityPlayerMP;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.service.persistence.NbtTranslator;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.DimensionManager;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.entity.Entity.class)
public abstract class MixinEntity implements Entity, IMixinEntity {

    // @formatter:off
    private EntityType entityType = Sponge.getSpongeRegistry().entityClassToTypeMappings.get(this.getClass());
    private boolean teleporting;
    private net.minecraft.entity.Entity teleportVehicle;
    private float origWidth;
    private float origHeight;
    @Nullable private Double modifiedEyeHeight = null;

    @Shadow private UUID entityUniqueID;
    @Shadow public net.minecraft.world.World worldObj;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow public double motionX;
    @Shadow public double motionY;
    @Shadow public double motionZ;
    @Shadow public double prevPosX;
    @Shadow public double prevPosY;
    @Shadow public double prevPosZ;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public float width;
    @Shadow public float height;
    @Shadow public float fallDistance;
    @Shadow public boolean isDead;
    @Shadow public boolean onGround;
    @Shadow public boolean inWater;
    @Shadow public int hurtResistantTime;
    @Shadow public int fireResistance;
    @Shadow public int fire;
    @Shadow public net.minecraft.entity.Entity riddenByEntity;
    @Shadow public net.minecraft.entity.Entity ridingEntity;
    @Shadow protected DataWatcher dataWatcher;
    @Shadow protected Random rand;
    @Shadow public abstract void setPosition(double x, double y, double z);
    @Shadow public abstract void mountEntity(net.minecraft.entity.Entity entityIn);
    @Shadow public abstract void setDead();
    @Shadow public abstract void setFlag(int flag, boolean data);
    @Shadow public abstract boolean getFlag(int flag);
    @Shadow public abstract int getAir();
    @Shadow public abstract void setAir(int air);
    @Shadow public abstract float getEyeHeight();
    @Shadow public abstract String getCustomNameTag();
    @Shadow public abstract void setCustomNameTag(String name);
    @Shadow public abstract UUID getUniqueID();
    @Shadow protected abstract boolean getAlwaysRenderNameTag();
    @Shadow protected abstract void setAlwaysRenderNameTag(boolean visible);
    @Shadow public abstract void writeToNBT(NBTTagCompound compound);
    @Shadow(prefix = "shadow$")
    protected abstract void shadow$setRotation(float yaw, float pitch);


    // @formatter:on

    @Inject(method = "setSize", at = @At("RETURN"))
    public void onSetSize(float width, float height, CallbackInfo ci) {
        if (this.origWidth == 0 || this.origHeight == 0) {
            this.origWidth = this.width;
            this.origHeight = this.height;
        }
    }

    @Inject(method = "moveEntity(DDD)V", at = @At("HEAD"), cancellable = true)
    public void onMoveEntity(double x, double y, double z, CallbackInfo ci) {
        if (!this.worldObj.isRemote && !SpongeHooks.checkEntitySpeed(((net.minecraft.entity.Entity) (Object) this), x, y, z)) {
            ci.cancel();
        }
    }

    @Inject(method = "getEyeHeight()F", at = @At("HEAD"), cancellable = true)
    public void onGetEyeHeight(CallbackInfoReturnable<Float> ci) {
        if (this.modifiedEyeHeight != null) {
            ci.setReturnValue(this.modifiedEyeHeight.floatValue());
        }
    }

    @Override
    public void setEyeHeight(Double value) {
        this.modifiedEyeHeight = value;
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {

    }

    @Override
    public World getWorld() {
        return (World) this.worldObj;
    }

    @Override
    public EntitySnapshot createSnapshot() {
        return new SpongeEntitySnapshotBuilder().from(this).build();
    }

    public Vector3d getPosition() {
        return new Vector3d(this.posX, this.posY, this.posZ);
    }

    @Override
    public Location<World> getLocation() {
        return new Location<World>((World) this.worldObj, getPosition());
    }

    @Override
    public void setLocation(Location<World> location) {
        setLocation(location, true);
    }

    @Override
    public boolean setLocationSafely(Location<World> location) {
        return setLocation(location, false);
    }

    @Override
    public void setLocationAndRotation(Location<World> location, Vector3d rotation) {
        setLocation(location);
        setRotation(rotation);
    }

    @Override
    public boolean setLocationAndRotationSafely(Location<World> location, Vector3d rotation) {
        boolean relocated = setLocation(location, false);
        setRotation(rotation);
        return relocated;
    }

    @Override
    public boolean setLocationAndRotationSafely(Location<World> location, Vector3d rotation, EnumSet<RelativePositions> relativePositions) {
        return setLocationAndRotation(location, rotation, relativePositions, true);
    }

    public boolean setLocation(Location<World> location, boolean forced) {
        if (isRemoved()) {
            return false;
        }

        Entity spongeEntity = this;
        net.minecraft.entity.Entity thisEntity = (net.minecraft.entity.Entity) spongeEntity;

        if (!forced) {
            // Validate
            TeleportHelper teleportHelper = Sponge.getGame().getTeleportHelper();
            Optional<Location<World>> safeLocation = teleportHelper.getSafeLocation(location);
            if (!safeLocation.isPresent()) {
                return false;
            } else {
                location = safeLocation.get();
            }
        }
        // detach passengers
        net.minecraft.entity.Entity passenger = thisEntity.riddenByEntity;
        ArrayDeque<net.minecraft.entity.Entity> passengers = new ArrayDeque<net.minecraft.entity.Entity>();
        while (passenger != null) {
            if (passenger instanceof EntityPlayerMP && !this.worldObj.isRemote) {
                ((EntityPlayerMP) passenger).mountEntity(null);
            }
            net.minecraft.entity.Entity nextPassenger = null;
            if (passenger.riddenByEntity != null) {
                nextPassenger = passenger.riddenByEntity;
                this.riddenByEntity.mountEntity(null);
            }
            passengers.add(passenger);
            passenger = nextPassenger;
        }

        net.minecraft.world.World nmsWorld = null;
        if (location.getExtent().getUniqueId() != ((World) this.worldObj).getUniqueId()) {
            nmsWorld = (net.minecraft.world.World) location.getExtent();
            if (thisEntity instanceof EntityPlayerMP) {
                // Close open containers
                if (((EntityPlayerMP) thisEntity).openContainer != ((EntityPlayerMP) thisEntity).inventoryContainer) {
                    ((EntityPlayerMP) thisEntity).closeContainer();
                }
            }
            teleportEntity(thisEntity, location, thisEntity.dimension, nmsWorld.provider.getDimensionId(), forced);
        } else {
            if (thisEntity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) thisEntity).playerNetServerHandler
                    .setPlayerLocation(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ(),
                        thisEntity.rotationYaw, thisEntity.rotationPitch);
            } else {
                setPosition(location.getPosition().getX(), location.getPosition().getY(), location.getPosition().getZ());
            }
        }

        // re-attach passengers
        net.minecraft.entity.Entity lastPassenger = thisEntity;
        while (!passengers.isEmpty()) {
            net.minecraft.entity.Entity passengerEntity = passengers.remove();
            if (nmsWorld != null) {
                teleportEntity(passengerEntity, location, passengerEntity.dimension, nmsWorld.provider.getDimensionId(), true);
            }

            if (passengerEntity instanceof EntityPlayerMP && !this.worldObj.isRemote) {
                // The actual mount is handled in our event as mounting must be set after client fully loads.
                ((IMixinEntity) passengerEntity).setIsTeleporting(true);
                ((IMixinEntity) passengerEntity).setTeleportVehicle(lastPassenger);
            } else {
                passengerEntity.mountEntity(lastPassenger);
            }
            lastPassenger = passengerEntity;
        }

        return true;
    }

    @Override
    public void setLocationAndRotation(Location<World> location, Vector3d rotation, EnumSet<RelativePositions> relativePositions) {
        setLocationAndRotation(location, rotation, relativePositions, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean setLocationAndRotation(Location<World> location, Vector3d rotation, EnumSet<RelativePositions> relativePositions, boolean forced) {
        boolean relocated = true;

        if (relativePositions.isEmpty()) {
            // This is just a normal teleport that happens to set both.
            relocated = setLocation(location, forced);
            setRotation(rotation);
        } else {
            if (((Entity) this) instanceof EntityPlayerMP) {
                // Players use different logic, as they support real relative movement.
                EnumSet relativeFlags = EnumSet.noneOf(S08PacketPlayerPosLook.EnumFlags.class);

                if (relativePositions.contains(RelativePositions.X)) {
                    relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.X);
                }

                if (relativePositions.contains(RelativePositions.Y)) {
                    relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.Y);
                }

                if (relativePositions.contains(RelativePositions.Z)) {
                    relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.Z);
                }

                if (relativePositions.contains(RelativePositions.PITCH)) {
                    relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.X_ROT);
                }

                if (relativePositions.contains(RelativePositions.YAW)) {
                    relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.Y_ROT);
                }

                ((EntityPlayerMP) (Entity) this).playerNetServerHandler.setPlayerLocation(location.getPosition().getX(), location.getPosition()
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
                relocated = setLocation(resultantLocation, forced);
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
        return new Transform<World>(getWorld(), getPosition(), getRotation(), getScale());
    }

    @Override
    public void setTransform(Transform<World> transform) {
        setLocation(transform.getLocation());
        setRotation(transform.getRotation());
        setScale(transform.getScale());
    }

    @Override
    public boolean transferToWorld(String worldName, Vector3d position) {
        Optional<WorldProperties> props = Sponge.getSpongeRegistry().getWorldProperties(worldName);
        if (props.isPresent()) {
            if (props.get().isEnabled()) {
                Optional<World> world = Sponge.getGame().getServer().loadWorld(worldName);
                if (world.isPresent()) {
                    Location<World> location = new Location<World>(world.get(), position);
                    return setLocationSafely(location);
                }
            }
        }
        return false;
    }

    @Override
    public boolean transferToWorld(UUID uuid, Vector3d position) {
        return transferToWorld(Sponge.getSpongeRegistry().getWorldFolder(uuid), position);
    }

    @Override
    public Vector3d getRotation() {
        return new Vector3d(this.rotationPitch, this.rotationYaw, 0);
    }

    @Override
    public void setRotation(Vector3d rotation) {
        if (((Entity) this) instanceof EntityPlayerMP) {
            // Force an update, this also set the rotation in this entity
            ((EntityPlayerMP) (Entity) this).playerNetServerHandler.setPlayerLocation(getPosition().getX(), getPosition().getY(),
                getPosition().getZ(), (float) rotation.getY(), (float) rotation.getX(), EnumSet.noneOf(RelativePositions.class));
        } else {
            // Let the entity tracker do its job, this just updates the variables
            shadow$setRotation((float) rotation.getY(), (float) rotation.getX());
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
    public boolean isTeleporting() {
        return this.teleporting;
    }

    @Override
    public net.minecraft.entity.Entity getTeleportVehicle() {
        return this.teleportVehicle;
    }

    @Override
    public void setIsTeleporting(boolean teleporting) {
        this.teleporting = teleporting;
    }

    @Override
    public void setTeleportVehicle(net.minecraft.entity.Entity vehicle) {
        this.teleportVehicle = vehicle;
    }

    @Override
    public EntityType getType() {
        return this.entityType;
    }

    @Override
    public UUID getUniqueId() {
        return this.entityUniqueID;
    }

    public Vector3d getVelocity() {
        return new Vector3d(this.motionX, this.motionY, this.motionZ);
    }

    public void setVelocity(Vector3d velocity) {
        this.motionX = velocity.getX();
        this.motionY = velocity.getY();
        this.motionZ = velocity.getZ();
    }

    public Entity getBaseVehicle() {
        if (this.ridingEntity == null) {
            return this;
        }

        net.minecraft.entity.Entity baseVehicle = this.ridingEntity;
        while (baseVehicle.ridingEntity != null) {
            baseVehicle = baseVehicle.ridingEntity;
        }
        return (Entity) baseVehicle;
    }

    public boolean setPassenger(@Nullable Entity entity) {
        net.minecraft.entity.Entity passenger = (net.minecraft.entity.Entity) entity;
        if (this.riddenByEntity == null) { // no existing passenger
            if (passenger == null) {
                return true;
            }

            Entity thisEntity = this;
            passenger.mountEntity((net.minecraft.entity.Entity) thisEntity);
        } else { // passenger already exists
            this.riddenByEntity.mountEntity(null); // eject current passenger

            if (passenger != null) {
                Entity thisEntity = this;
                passenger.mountEntity((net.minecraft.entity.Entity) thisEntity);
            }
        }

        return true;
    }

    public boolean setVehicle(@Nullable Entity entity) {
        mountEntity((net.minecraft.entity.Entity) entity);
        return true;
    }

    // for sponge internal use only
    @SuppressWarnings("unchecked")
    public boolean teleportEntity(net.minecraft.entity.Entity entity, Location<World> location, int currentDim, int targetDim, boolean forced) {
        MinecraftServer mcServer = MinecraftServer.getServer();
        final WorldServer fromWorld = mcServer.worldServerForDimension(currentDim);
        final WorldServer toWorld = mcServer.worldServerForDimension(targetDim);
        if (entity instanceof EntityPlayer) {
            fromWorld.getEntityTracker().removePlayerFromTrackers((EntityPlayerMP) entity);
            fromWorld.getPlayerManager().removePlayer((EntityPlayerMP) entity);
            mcServer.getConfigurationManager().playerEntityList.remove(entity);
        } else {
            fromWorld.getEntityTracker().untrackEntity(entity);
        }

        entity.worldObj.removePlayerEntityDangerously(entity);
        entity.dimension = targetDim;
        entity.setPositionAndRotation(location.getX(), location.getY(), location.getZ(), 0, 0);
        if (forced) {
            while (!toWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox()).isEmpty() && entity.posY < 256.0D) {
                entity.setPosition(entity.posX, entity.posY + 1.0D, entity.posZ);
            }
        }

        toWorld.theChunkProviderServer.loadChunk((int) entity.posX >> 4, (int) entity.posZ >> 4);

        if (entity instanceof EntityPlayer) {
            EntityPlayerMP entityplayermp1 = (EntityPlayerMP) entity;

            // Support vanilla clients going into custom dimensions
            int clientDimension = DimensionManager.getClientDimensionToSend(toWorld.provider.getDimensionId(), toWorld, entityplayermp1);
            if (((IMixinEntityPlayerMP) entityplayermp1).usesCustomClient()) {
                DimensionManager.sendDimensionRegistration(toWorld, entityplayermp1, clientDimension);
            } else {
                // Force vanilla client to refresh their chunk cache if same dimension
                if (currentDim != targetDim && (currentDim == clientDimension || targetDim == clientDimension)) {
                    entityplayermp1.playerNetServerHandler.sendPacket(
                        new S07PacketRespawn((byte) (clientDimension >= 0 ? -1 : 0), toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(),
                            entityplayermp1.theItemInWorldManager.getGameType()));
                }
            }

            entityplayermp1.playerNetServerHandler.sendPacket(
                new S07PacketRespawn(clientDimension, toWorld.getDifficulty(), toWorld.getWorldInfo().getTerrainType(),
                    entityplayermp1.theItemInWorldManager.getGameType()));
            entity.setWorld(toWorld);
            entity.isDead = false;
            entityplayermp1.playerNetServerHandler.setPlayerLocation(entityplayermp1.posX, entityplayermp1.posY, entityplayermp1.posZ,
                entityplayermp1.rotationYaw, entityplayermp1.rotationPitch);
            entityplayermp1.setSneaking(false);
            mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(entityplayermp1, toWorld);
            toWorld.getPlayerManager().addPlayer(entityplayermp1);
            toWorld.spawnEntityInWorld(entityplayermp1);
            mcServer.getConfigurationManager().playerEntityList.add(entityplayermp1);
            entityplayermp1.theItemInWorldManager.setWorld(toWorld);
            entityplayermp1.addSelfToInternalCraftingInventory();
            entityplayermp1.setHealth(entityplayermp1.getHealth());
        } else {
            toWorld.spawnEntityInWorld(entity);
        }

        fromWorld.resetUpdateEntityTick();
        toWorld.resetUpdateEntityTick();
        return true;
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #writeToNbt}.
     *
     * <p> This makes it easier for other entity mixins to override writeToNBT
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write
     *        to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/entity/Entity;writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("HEAD"))
    public void onWriteToNBT(NBTTagCompound compound, CallbackInfo ci) {
        this.writeToNbt(this.getSpongeData());
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #readFromNbt}.
     *
     * <p> This makes it easier for other entity mixins to override readFromNbt
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read
     *        from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/entity/Entity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    public void onReadFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        this.readFromNbt(this.getSpongeData());
    }

    @Override
    public boolean validateRawData(DataContainer container) {
        return false;
    }

    @Override
    public void setRawData(DataContainer container) throws InvalidDataException {

    }

    /**
     * Read extra data (SpongeData) from the entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    @Override
    public void readFromNbt(NBTTagCompound compound) {
        if (this instanceof IMixinCustomDataHolder) {
            if (compound.hasKey(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_LIST)) {
                final NBTTagList list = compound.getTagList(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, NbtDataUtil.TAG_COMPOUND);
                final ImmutableList.Builder<DataView> builder = ImmutableList.builder();
                if (list != null && list.tagCount() != 0) {
                    for (int i = 0; i < list.tagCount(); i++) {
                        final NBTTagCompound internal = list.getCompoundTagAt(i);
                        builder.add(NbtTranslator.getInstance().translateFrom(internal));
                    }
                }
                try {
                    final List<DataManipulator<?, ?>> manipulators = DataUtil.deserializeManipulatorList(builder.build());
                    for (DataManipulator<?, ?> manipulator : manipulators) {
                        offer(manipulator);
                    }
                } catch (InvalidDataException e) {
                    Sponge.getLogger().error("Could not deserialize custom plugin data! ", e);
                }
            }
        }
    }

    /**
     * Write extra data (SpongeData) to the entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    @Override
    public void writeToNbt(NBTTagCompound compound) {
        if (this instanceof IMixinCustomDataHolder) {
            final List<DataManipulator<?, ?>> manipulators = ((IMixinCustomDataHolder) this).getCustomManipulators();
            if (!manipulators.isEmpty()) {
                final List<DataView> manipulatorViews = DataUtil.getSerializedManipulatorList(manipulators);
                final NBTTagList manipulatorTagList = new NBTTagList();
                for (DataView dataView : manipulatorViews) {
                    manipulatorTagList.appendTag(NbtTranslator.getInstance().translateData(dataView));
                }
                compound.setTag(NbtDataUtil.CUSTOM_MANIPULATOR_TAG_LIST, manipulatorTagList);
            }
        }
    }

    @Override
    public DataContainer toContainer() {
        final Transform<World> transform = getTransform();
        final NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        NbtDataUtil.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
        final DataContainer unsafeNbt = NbtTranslator.getInstance().translateFrom(compound);
        final DataContainer container = new MemoryDataContainer()
            .set(DataQueries.ENTITY_CLASS, this.getClass().getName())
            .set(Location.WORLD_ID, transform.getExtent().getUniqueId().toString())
            .createView(DataQueries.SNAPSHOT_WORLD_POSITION)
                .set(Location.POSITION_X, transform.getPosition().getX())
                .set(Location.POSITION_Y, transform.getPosition().getY())
                .set(Location.POSITION_Z, transform.getPosition().getZ())
            .getContainer()
            .createView(DataQueries.ENTITY_ROTATION)
                .set(Location.POSITION_X, transform.getRotation().getX())
                .set(Location.POSITION_Y, transform.getRotation().getY())
                .set(Location.POSITION_Z, transform.getRotation().getZ())
            .getContainer()
            .createView(DataQueries.ENTITY_SCALE)
                .set(Location.POSITION_X, transform.getScale().getX())
                .set(Location.POSITION_Y, transform.getScale().getY())
                .set(Location.POSITION_Z, transform.getScale().getZ())
            .getContainer()
            .set(DataQueries.ENTITY_TYPE, this.entityType.getId())
            .set(DataQueries.UNSAFE_NBT, unsafeNbt);
        final Collection<DataManipulator<?, ?>> manipulators = getContainers();
        if (!manipulators.isEmpty()) {
            container.set(DataQueries.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
        }
        return container;
    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        final List<DataManipulator<?, ?>> list = Lists.newArrayList();
        this.supplyVanillaManipulators(list);
        if (this instanceof IMixinCustomDataHolder && ((IMixinCustomDataHolder) this).hasManipulators()) {
            list.addAll(((IMixinCustomDataHolder) this).getCustomManipulators());
        }
        return list;
    }

    @Override
    public Entity copy() {
        if ((Object) this instanceof Player) {
            throw new IllegalArgumentException("Cannot copy player entities!");
        }
        try {
            final NBTTagCompound compound = new NBTTagCompound();
            writeToNBT(compound);
            net.minecraft.entity.Entity entity = EntityList.createEntityByName(this.entityType.getId(), this.worldObj);
            compound.setLong(NbtDataUtil.UUID_MOST, entity.getUniqueID().getMostSignificantBits());
            compound.setLong(NbtDataUtil.UUID_LEAST, entity.getUniqueID().getLeastSignificantBits());
            entity.readFromNBT(compound);
            return (Entity) entity;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not copy the entity:", e);
        }
    }
}
