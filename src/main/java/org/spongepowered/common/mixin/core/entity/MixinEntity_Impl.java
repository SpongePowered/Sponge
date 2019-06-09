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

import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.dismount.DismountTypes;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeGravityData;
import org.spongepowered.common.data.nbt.CustomDataNbtUtil;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntitySnapshotBuilder;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.damage.MinecraftBlockDamageSource;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinTrackable;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.interfaces.world.gen.IMixinChunkProviderServer;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.WorldManager;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(Entity.class)
@Implements(@Interface(iface = org.spongepowered.api.entity.Entity.class, prefix = "entityApi$"))
public abstract class MixinEntity_Impl implements IMixinEntity, IMixinTrackable {

    // @formatter:off
    protected final SpongeEntityType entityType = EntityTypeRegistryModule.getInstance().getForClass(((Entity) (Object) this).getClass());
    private boolean teleporting;
    private WeakReference<IMixinChunk> activeChunk = new WeakReference<>(null);
    private float origWidth;
    private float origHeight;
    private boolean isConstructing = true;
    @Nullable private Text displayName;
    @Nullable private BlockState currentCollidingBlock;
    @Nullable private BlockPos lastCollidedBlockPos;
    @Nullable private Entity teleportVehicle;
    // Used by tracker config
    private boolean trackedInWorld = false;

    @Shadow @Nullable public Entity ridingEntity;
    @Shadow @Final private List<Entity> riddenByEntities;
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
    @Shadow public abstract List<Entity> shadow$getPassengers();
    @Shadow public abstract Entity getLowestRidingEntity();
    @Shadow public abstract Entity getRidingEntity();
    @Shadow public abstract void removePassengers();
    @Shadow public abstract void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack);
    @Shadow public abstract void playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow public abstract boolean isEntityInvulnerable(DamageSource source);
    @Shadow public abstract boolean isSprinting();
    @Shadow public abstract boolean isInWater();
    @Shadow public abstract boolean isRiding();
    @Shadow public abstract boolean isOnSameTeam(Entity entityIn);
    @Shadow public abstract double getDistanceSq(Entity entityIn);
    @Shadow public abstract void setLocationAndAngles(double x, double y, double z, float yaw, float pitch);
    @Shadow public abstract boolean hasNoGravity();
    @Shadow public abstract void setPositionAndUpdate(double x, double y, double z);
    @Shadow protected abstract void removePassenger(Entity passenger);
    @Shadow protected abstract void shadow$setRotation(float yaw, float pitch);
    @Shadow protected abstract void setSize(float width, float height);
    @Shadow protected abstract void applyEnchantments(EntityLivingBase entityLivingBaseIn, Entity entityIn);
    @Shadow public abstract void extinguish();
    @Shadow protected abstract void setFlag(int flag, boolean set);

    // @formatter:on

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;dimension:I", opcode = Opcodes.PUTFIELD))
    private void onSet(Entity self, int dimensionId, net.minecraft.world.World worldIn) {
        if (worldIn instanceof IMixinWorldServer) {
            self.dimension = ((IMixinWorldServer) worldIn).getDimensionId();
        } else {
            self.dimension = dimensionId;
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onSpongeConstruction(net.minecraft.world.World worldIn, CallbackInfo ci) {
        if (this.entityType.isKnown()) {
            this.refreshTrackerStates();
            if (this.entityType.getEnumCreatureType() == null) {
                for (EnumCreatureType type : EnumCreatureType.values()) {
                    if (SpongeImplHooks.isCreatureOfType((Entity) (Object) this, type)) {
                        this.entityType.setEnumCreatureType(type);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean isInConstructPhase() {
        return this.isConstructing;
    }

    @Override
    public void firePostConstructEvents() {
        this.isConstructing = false;
    }

    @Override
    public boolean isTrackedInWorld() {
        return this.trackedInWorld;
    }

    @Override
    public void setTrackedInWorld(boolean tracked) {
        this.trackedInWorld = tracked;
    }

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;ridingEntity:Lnet/minecraft/entity/Entity;", ordinal = 0),
            cancellable = true)
    private void onStartRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> ci) {
        if (!this.world.isRemote && (ShouldFire.RIDE_ENTITY_EVENT_MOUNT || ShouldFire.RIDE_ENTITY_EVENT)) {
            Sponge.getCauseStackManager().pushCause(this);
            if (SpongeImpl.postEvent(SpongeEventFactory.createRideEntityEventMount(Sponge.getCauseStackManager().getCurrentCause(), (org.spongepowered.api.entity.Entity) vehicle))) {
                ci.cancel();
            }
            Sponge.getCauseStackManager().popCause();
        }
    }

    /**
     * @author rexbut - December 16th, 2016
     *
     * @reason - adjusted to support {@link DismountTypes}
     */
    @Overwrite
    public void dismountRidingEntity() {
        if (this.ridingEntity != null) {
            if (this.getRidingEntity().isDead) {
                this.dismountRidingEntity(DismountTypes.DEATH);
            } else {
                this.dismountRidingEntity(DismountTypes.PLAYER);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean dismountRidingEntity(DismountType type) {
        if (!this.world.isRemote && (ShouldFire.RIDE_ENTITY_EVENT_DISMOUNT || ShouldFire.RIDE_ENTITY_EVENT)) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                frame.addContext(EventContextKeys.DISMOUNT_TYPE, type);
                if (SpongeImpl.postEvent(SpongeEventFactory.
                    createRideEntityEventDismount(frame.getCurrentCause(), type, (org.spongepowered.api.entity.Entity) this.getRidingEntity()))) {
                    return false;
                }
            }
        }

        if (this.ridingEntity != null) {
            MixinEntity_Impl entity = (MixinEntity_Impl) (Object) this.ridingEntity;
            this.ridingEntity = null;
            entity.removePassenger((Entity) (Object) this);
        }
        return true;
    }

    @Override
    public boolean removePassengers(DismountType type) {
        boolean dismount = false;
        for (int i = this.riddenByEntities.size() - 1; i >= 0; --i) {
            dismount = ((IMixinEntity) this.riddenByEntities.get(i)).dismountRidingEntity(type) || dismount;
        }
        return dismount;
    }

    @Inject(method = "setSize", at = @At("RETURN"))
    private void spongeImpl$onSpongeSetSize(float width, float height, CallbackInfo ci) {
        if (this.origWidth == 0 || this.origHeight == 0) {
            this.origWidth = this.width;
            this.origHeight = this.height;
        }
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void spongeImpl$onSpongeMoveEntity(MoverType type, double x, double y, double z, CallbackInfo ci) {
        if (!this.world.isRemote && !SpongeHooks.checkEntitySpeed(((Entity) (Object) this), x, y, z)) {
            ci.cancel();
        }
    }

    @Redirect(method = "setOnFireFromLava",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
        )
    )
    private boolean onSpongeRedirectForBlockDamageSource(Entity entity, DamageSource source, float damage) {
        if (this.world.isRemote) { // Short circuit
            return entity.attackEntityFrom(source, damage);
        }
        try {
            AxisAlignedBB bb = this.getEntityBoundingBox().grow(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D);
            Location<World> location = DamageEventHandler.findFirstMatchingBlock((Entity) (Object) this, bb, block ->
                block.getMaterial() == Material.LAVA);
            DamageSource.LAVA = new MinecraftBlockDamageSource("lava", location).setFireDamage();
            return entity.attackEntityFrom(DamageSource.LAVA, damage);
        } finally {
            // Since "source" is already the DamageSource.LAVA object, we can simply re-use it here.
            DamageSource.LAVA = source;
        }

    }

    @Redirect(method = "dealFireDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
        )
    )
    private boolean onSpongeRedirectForFireDamage(Entity entity, DamageSource source, float damage) {
        if (this.world.isRemote) { // Short Circuit
            return entity.attackEntityFrom(source, damage);
        }
        try {
            AxisAlignedBB bb = this.getEntityBoundingBox().grow(-0.001D, -0.001D, -0.001D);
            Location<World> location = DamageEventHandler.findFirstMatchingBlock((Entity) (Object) this, bb, block ->
                block.getBlock() == Blocks.FIRE || block.getBlock() == Blocks.FLOWING_LAVA || block.getBlock() == Blocks.LAVA);
            DamageSource.IN_FIRE = new MinecraftBlockDamageSource("inFire", location).setFireDamage();
            return entity.attackEntityFrom(DamageSource.IN_FIRE, damage);
        } finally {
            // Since "source" is already the DamageSource.IN_FIRE object, we can re-use it to re-assign.
            DamageSource.IN_FIRE = source;
        }

    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        Optional<VehicleData> vehicleData = ((org.spongepowered.api.entity.Entity) this).get(VehicleData.class);
        if (vehicleData.isPresent()) {
            manipulators.add(vehicleData.get());
        }
        if (this.fire > 0) {
            manipulators.add(((org.spongepowered.api.entity.Entity) this).get(IgniteableData.class).get());
        }
        manipulators.add(new SpongeGravityData(!this.hasNoGravity()));
    }

    @Inject(method = "setPosition", at = @At("HEAD"))
    private void onSetPosition(double x, double y, double z, CallbackInfo ci) {
        if (((Entity) (Object) this) instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) (Object) this;
            if (player.connection != null) {
                ((IMixinNetHandlerPlayServer) player.connection).captureCurrentPlayerPosition();
            }
        }
    }

    public Vector3d getPosition() {
        return new Vector3d(this.posX, this.posY, this.posZ);
    }



    // always use these methods internally when setting locations from a transform or location
    // to avoid firing a DisplaceEntityEvent.Teleport
    @Override
    public void setLocationAndAngles(Location<World> location) {
        if (((Entity) (Object) this) instanceof EntityPlayerMP) {
            ((EntityPlayerMP) (Object) this).connection.setPlayerLocation(location.getX(), location.getY(), location.getZ(), this.rotationYaw, this.rotationPitch);
        } else {
            this.setPosition(location.getX(), location.getY(), location.getZ());
        }
        if (this.world != location.getExtent()) {
            this.world = (net.minecraft.world.World) location.getExtent();
        }
    }

    @Override
    public void setLocationAndAngles(Transform<World> transform) {
        Vector3d position = transform.getPosition();
        EntityPlayerMP player = null;
        if (((Entity) (Object) this) instanceof EntityPlayerMP) {
            player = ((EntityPlayerMP) (Object) this);
        }
        if (player != null && player.connection != null) {
            player.connection.setPlayerLocation(position.getX(), position.getY(), position.getZ(), (float) transform.getYaw(), (float) transform.getPitch());
        } else {
            this.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) transform.getYaw(), (float) transform.getPitch());
        }
        if (this.world != transform.getExtent()) {
            this.world = (net.minecraft.world.World) transform.getExtent();
        }
    }


    @Inject(method = "onUpdate", at = @At("RETURN"))
    private void spongeOnUpdate(CallbackInfo callbackInfo) {
        if (this.pendingVisibilityUpdate && !this.world.isRemote) {
            final EntityTracker entityTracker = ((WorldServer) this.world).getEntityTracker();
            final EntityTrackerEntry lookup = entityTracker.trackedEntityHashTable.lookup(this.getEntityId());
            if (this.visibilityTicks % 4 == 0) {
                if (this.isVanished) {
                    for (EntityPlayerMP entityPlayerMP : lookup.trackingPlayers) {
                        entityPlayerMP.connection.sendPacket(new SPacketDestroyEntities(this.getEntityId()));
                        if (((Object) this) instanceof EntityPlayerMP) {
                            entityPlayerMP.connection.sendPacket(
                                    new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, (EntityPlayerMP) (Object) this));
                        }
                    }
                } else {
                    this.visibilityTicks = 1;
                    this.pendingVisibilityUpdate = false;
                    for (EntityPlayerMP entityPlayerMP : SpongeImpl.getServer().getPlayerList().getPlayers()) {
                        if (((Object) this) == entityPlayerMP) {
                            continue;
                        }
                        if (((Object) this) instanceof EntityPlayerMP) {
                            Packet<?> packet = new SPacketPlayerListItem(SPacketPlayerListItem.Action.ADD_PLAYER, (EntityPlayerMP) (Object) this);
                            entityPlayerMP.connection.sendPacket(packet);
                        }
                        Packet<?> newPacket = lookup.createSpawnPacket(); // creates the spawn packet for us
                        entityPlayerMP.connection.sendPacket(newPacket);
                    }
                }
            }
            if (this.visibilityTicks > 0) {
                this.visibilityTicks--;
            } else {
                this.pendingVisibilityUpdate = false;
            }
        }
    }


    @Override
    public boolean isTeleporting() {
        return this.teleporting;
    }

    @Override
    public Entity getTeleportVehicle() {
        return this.teleportVehicle;
    }

    @Override
    public void setIsTeleporting(boolean teleporting) {
        this.teleporting = teleporting;
    }

    @Override
    public void setTeleportVehicle(Entity vehicle) {
        this.teleportVehicle = vehicle;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Intrinsic
    public List<org.spongepowered.api.entity.Entity> entity$getPassengers() {
        return (List) shadow$getPassengers();
    }

    @Override
    public boolean getIsInvulnerable() {
        return this.invulnerable;
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
    @Inject(method = "Lnet/minecraft/entity/Entity;writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("HEAD"))
    private void onSpongeWriteToNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> ci) {
        this.writeToNbt(this.getSpongeData());
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #readFromNbt}.
     *
     * <p> This makes it easier for other entity mixins to override readSpongeNBT
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read
     *        from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/entity/Entity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void onSpongeReadFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (this.isConstructing) {
            firePostConstructEvents(); // Do this early as possible
        }
        this.readFromNbt(this.getSpongeData());
    }

    /**
     * Read extra data (SpongeData) from the entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    @Override
    public void readFromNbt(NBTTagCompound compound) {
        CustomDataNbtUtil.readCustomData(compound, ((org.spongepowered.api.entity.Entity) this));
        if (this instanceof IMixinGriefer && ((IMixinGriefer) this).isGriefer() && compound.hasKey(NbtDataUtil.CAN_GRIEF)) {
            ((IMixinGriefer) this).setCanGrief(compound.getBoolean(NbtDataUtil.CAN_GRIEF));
        }
        if (compound.hasKey(NbtDataUtil.IS_VANISHED, NbtDataUtil.TAG_BYTE)) {
            this.setVanished(compound.getBoolean(NbtDataUtil.IS_VANISHED));
        }
    }

    /**
     * Write extra data (SpongeData) to the entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    @Override
    public void writeToNbt(NBTTagCompound compound) {
        CustomDataNbtUtil.writeCustomData(compound, ((org.spongepowered.api.entity.Entity) this));
        if (this instanceof IMixinGriefer && ((IMixinGriefer) this).isGriefer()) {
            compound.setBoolean(NbtDataUtil.CAN_GRIEF, ((IMixinGriefer) this).canGrief());
        }
        if (this.isVanished) {
            compound.setBoolean(NbtDataUtil.IS_VANISHED, true);
        }
    }

    @Override
    public Optional<User> getTrackedPlayer(String nbtKey) {
        return Optional.empty();
    }

    @Override
    public Optional<User> getCreatorUser() {
        return Optional.empty();
    }

    @Override
    public Optional<User> getNotifierUser() {
        return Optional.empty();
    }

    @Override
    public void trackEntityUniqueId(String nbtKey, @Nullable UUID uuid) {
    }

    @Override
    public void setImplVelocity(Vector3d velocity) {
        this.motionX = checkNotNull(velocity).getX();
        this.motionY = velocity.getY();
        this.motionZ = velocity.getZ();
        this.velocityChanged = true;
    }

    @Redirect(method = "move",at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;"
                                                                        + "onEntityWalk(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
    private void spongeImpl$onEntityCollideWithBlockThrowEventSponge(Block block, net.minecraft.world.World world, BlockPos pos, Entity entity) {
        // if block can't collide, return
        if (!((IMixinBlock) block).hasCollideLogic()) {
            return;
        }

        if (world.isRemote) {
            block.onEntityWalk(world, pos, entity);
            return;
        }

        IBlockState state = world.getBlockState(pos);
        this.setCurrentCollidingBlock((BlockState) state);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, Direction.NONE)) {
            block.onEntityWalk(world, pos, entity);
            this.lastCollidedBlockPos = pos;
        }

        this.setCurrentCollidingBlock(null);
    }

    @Redirect(method = "doBlockCollisions", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V")) // doBlockCollisions
    private void spongeImpl$onEntityCollideWithBlockState(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, Entity entity) {
        // if block can't collide, return
        if (!((IMixinBlock) block).hasCollideWithStateLogic()) {
            return;
        }

        if (world.isRemote) {
            block.onEntityCollision(world, pos, state, entity);
            return;
        }

        this.setCurrentCollidingBlock((BlockState) state);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, Direction.NONE)) {
            block.onEntityCollision(world, pos, state, entity);
            this.lastCollidedBlockPos = pos;
        }

        this.setCurrentCollidingBlock(null);
    }

    @Redirect(method = "updateFallState", at = @At(value = "INVOKE", target="Lnet/minecraft/block/Block;onFallenUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V"))
    private void spongeImpl$onBlockFallenUpon(Block block, net.minecraft.world.World world, BlockPos pos, Entity entity, float fallDistance) {
        if (world.isRemote) {
            block.onFallenUpon(world, pos, entity, fallDistance);
            return;
        }

        IBlockState state = world.getBlockState(pos);
        this.setCurrentCollidingBlock((BlockState) state);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, Direction.UP)) {
            block.onFallenUpon(world, pos, entity, fallDistance);
            this.lastCollidedBlockPos = pos;
        }

        this.setCurrentCollidingBlock(null);
    }


    private boolean collision = false;
    private boolean untargetable = false;
    private boolean isVanished = false;

    private boolean pendingVisibilityUpdate = false;
    private int visibilityTicks = 0;

    @Override
    public boolean isVanished() {
        return this.isVanished;
    }

    @Override
    public void setVanished(boolean vanished) {
        this.isVanished = vanished;
        this.pendingVisibilityUpdate = true;
        this.visibilityTicks = 20;
    }

    @Override
    public boolean ignoresCollision() {
        return this.collision;
    }

    @Override
    public void setIgnoresCollision(boolean prevents) {
        this.collision = prevents;
    }

    @Override
    public boolean isUntargetable() {
        return this.untargetable;
    }

    @Override
    public void setUntargetable(boolean untargetable) {
        this.untargetable = untargetable;
    }

    /**
     * @author gabizou - January 4th, 2016
     * @updated gabizou - January 27th, 2016 - Rewrite to a redirect
     *
     * This prevents sounds from being sent to the server by entities that are vanished
     */
    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSilent()Z"))
    private boolean spongeImpl$checkIsSilentOrInvis(Entity entity) {
        return entity.isSilent() || this.isVanished;
    }

    @Redirect(method = "applyEntityCollision", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;noClip:Z", opcode = Opcodes.GETFIELD))
    private boolean spongeApplyEntityCollisionCheckVanish(Entity entity) {
        return entity.noClip || ((IMixinEntity) entity).isVanished();
    }

    @Redirect(method = "doWaterSplashEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private void spawnParticle(net.minecraft.world.World world, EnumParticleTypes particleTypes, double xCoord, double yCoord, double zCoord,
        double xOffset, double yOffset, double zOffset, int... p_175688_14_) {
        if (!this.isVanished) {
            this.world.spawnParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
        }
    }

    @Redirect(method = "createRunningParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"))
    private void runningSpawnParticle(net.minecraft.world.World world, EnumParticleTypes particleTypes, double xCoord, double yCoord, double zCoord,
        double xOffset, double yOffset, double zOffset, int... p_175688_14_) {
        if (!this.isVanished) {
            this.world.spawnParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
        }
    }

    @Nullable
    @Override
    public Text getDisplayNameText() {
        return this.displayName;
    }

    private boolean skipSettingCustomNameTag = false;

    @Override
    public void setDisplayName(@Nullable Text displayName) {
        this.displayName = displayName;

        this.skipSettingCustomNameTag = true;
        if (this.displayName == null) {
            this.setCustomNameTag("");
        } else {
            this.setCustomNameTag(SpongeTexts.toLegacy(this.displayName));
        }

        this.skipSettingCustomNameTag = false;
    }

    @Inject(method = "setCustomNameTag", at = @At("RETURN"))
    public void onSetCustomNameTag(String name, CallbackInfo ci) {
        if (!this.skipSettingCustomNameTag) {
            this.displayName = SpongeTexts.fromLegacy(name);
        }
    }

    /**
     * @author gabizou - January 30th, 2016
     * @author blood - May 12th, 2016
     * @author gabizou - June 2nd, 2016
     *
     * @reason Rewrites the method entirely for several reasons:
     * 1) If we are in a forge environment, we do NOT want forge to be capturing the item entities, because we handle them ourselves
     * 2) If we are in a client environment, we should not perform any sort of processing whatsoever.
     * 3) This method is entirely managed from the standpoint where our events have final say, as per usual.
     *
     * @param stack
     * @param offsetY
     * @return
     */
    @Overwrite
    @Nullable
    public EntityItem entityDropItem(ItemStack stack, float offsetY) {
        // Sponge Start
        // Gotta stick with the client side handling things
        if (this.world.isRemote) {
            // Sponge End - resume normal client code. Server side we will handle it elsewhere
            if (stack.isEmpty()) {
                return null;
            } else {
                EntityItem entityitem = new EntityItem(this.world, this.posX, this.posY + (double) offsetY, this.posZ, stack);
                entityitem.setDefaultPickupDelay();
                this.world.spawnEntity(entityitem);
                return entityitem;
            }
        }
        // Sponge - Redirect server sided code to handle through the PhaseTracker
        return EntityUtil.entityOnDropItem((Entity) (Object) this, stack, offsetY);
    }

    @Override
    public void setCurrentCollidingBlock(BlockState state) {
        this.currentCollidingBlock = state;
    }

    @Override
    public BlockState getCurrentCollidingBlock() {
        if (this.currentCollidingBlock == null) {
            return (BlockState) Blocks.AIR.getDefaultState();
        }
        return this.currentCollidingBlock;
    }

    @Override
    public BlockPos getLastCollidedBlockPos() {
        return this.lastCollidedBlockPos;
    }

    @Override
    public boolean isVanilla() {
        return this.entityType.isVanilla();
    }

    @Override
    public Timing getTimingsHandler() {
        return this.entityType.getTimingsHandler();
    }

    @Override
    @Nullable
    public IMixinChunk getActiveChunk() {
        return this.activeChunk.get();
    }

    @Override
    public void setActiveChunk(@Nullable IMixinChunk chunk) {
        this.activeChunk = new WeakReference<IMixinChunk>(chunk);
    }

    @Override
    public boolean shouldTick() {
        final IMixinChunk chunk = this.getActiveChunk();
        // Don't tick if chunk is queued for unload or is in progress of being scheduled for unload
        // See https://github.com/SpongePowered/SpongeVanilla/issues/344
        if (chunk != null && !chunk.isActive()) {
            return false;
        }

        return true;
    }

    @Override
    public void setInvulnerable(boolean value) {
        this.invulnerable = value;
    }

    @Override
    public boolean allowsBlockBulkCapture() {
        return this.entityType.allowsBlockBulkCapture;
    }

    @Override
    public boolean allowsEntityBulkCapture() {
        return this.entityType.allowsEntityBulkCapture;
    }

    @Override
    public boolean allowsBlockEventCreation() {
        return this.entityType.allowsBlockEventCreation;
    }

    @Override
    public boolean allowsEntityEventCreation() {
        return this.entityType.allowsEntityEventCreation;
    }


    @Redirect(method = "setFire",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;fire:I", opcode = Opcodes.PUTFIELD)
    )
    private void onFire(Entity entity, int ticks) {
        if (((IMixinWorld) world).isFake() || !ShouldFire.IGNITE_ENTITY_EVENT) {
            this.fire = ticks; // Vanilla functionality
            return;
        }
        if (this.fire < 1 && !this.isImmuneToFireForIgniteEvent()) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

                frame.pushCause(((org.spongepowered.api.entity.Entity) this).getLocation().getExtent());
                IgniteEntityEvent event = SpongeEventFactory.
                        createIgniteEntityEvent(frame.getCurrentCause(), ticks, ticks, ((org.spongepowered.api.entity.Entity) this));

                if (SpongeImpl.postEvent(event)) {
                    this.fire = 0;
                    return; // set fire ticks to 0
                }
                this.fire = event.getFireTicks();
            }
        }
    }

    @Override
    public boolean isImmuneToFireForIgniteEvent() { // Since normal entities don't have the concept of having game modes...
        return false;
    }

    @Redirect(method = "onStruckByLightning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean onSpongeLightningDamage(Entity entity, DamageSource source, float damage, EntityLightningBolt lightningBolt) {
        if (!this.world.isRemote) {
            return entity.attackEntityFrom(source, damage);
        }
        try {
            DamageSource.LIGHTNING_BOLT = new EntityDamageSource("lightningBolt", lightningBolt);
            return entity.attackEntityFrom(DamageSource.LIGHTNING_BOLT, damage);
        } finally {
            DamageSource.LIGHTNING_BOLT = source;
        }
    }

}
