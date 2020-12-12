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

import co.aikar.timings.Timing;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.TicketType;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.DismountType;
import org.spongepowered.api.event.cause.entity.DismountTypes;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.Transform;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.command.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.InvulnerableTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.EntityTypeBridge;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.bridge.world.PlatformITeleporterBridge;
import org.spongepowered.common.bridge.world.PlatformServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.cause.entity.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MinecraftBlockDamageSource;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.DimensionChangeResult;
import org.spongepowered.common.world.portal.WrappedITeleporterPortalType;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityBridge, PlatformEntityBridge, VanishableBridge, InvulnerableTrackedBridge,
        TimingBridge, CommandSourceProviderBridge, DataCompoundHolder {

    // @formatter:off
    @Shadow public net.minecraft.world.World world;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public int hurtResistantTime;
    @Shadow public boolean removed;
    @Shadow public float prevDistanceWalkedModified;
    @Shadow public float distanceWalkedModified;
    @Shadow @Final protected Random rand;
    @Shadow @Final protected EntityDataManager dataManager;
    @Shadow public DimensionType dimension;
    @Shadow public float prevRotationYaw;
    @Shadow protected int portalCounter;
    @Shadow public boolean collided;
    @Shadow @Nullable private Entity ridingEntity;
    @Shadow @Final private List<Entity> passengers;
    @Shadow public boolean onGround;
    @Shadow public float fallDistance;

    @Shadow public abstract void shadow$setPosition(double x, double y, double z);
    @Shadow public abstract double shadow$getPosX();
    @Shadow public abstract double shadow$getPosZ();
    @Shadow public abstract double shadow$getPosY();
    @Shadow public abstract void shadow$remove();
    @Shadow public abstract void shadow$setCustomName(@Nullable ITextComponent name);
    @Shadow public abstract boolean shadow$attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract int shadow$getEntityId();
    @Shadow public abstract boolean shadow$isBeingRidden();
    @Shadow public abstract void shadow$playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow protected abstract void shadow$removePassenger(Entity passenger);
    @Shadow public abstract boolean shadow$isInvisible();
    @Shadow public abstract void shadow$setInvisible(boolean invisible);
    @Shadow protected abstract int shadow$getFireImmuneTicks();
    @Shadow public abstract EntityType<?> shadow$getType();
    @Shadow public abstract void shadow$setMotion(Vec3d motionIn);
    @Shadow public abstract Vec3d shadow$getMotion();
    @Shadow public abstract boolean shadow$isInWater();
    @Shadow public abstract boolean shadow$isPassenger();
    @Shadow public abstract void shadow$setPositionAndUpdate(double x, double y, double z);
    @Shadow public abstract int shadow$getMaxAir();
    @Shadow protected abstract void shadow$applyEnchantments(LivingEntity entityLivingBaseIn, Entity entityIn);
    @Shadow public abstract CommandSource shadow$getCommandSource();
    @Shadow public abstract World shadow$getEntityWorld();
    @Shadow public abstract net.minecraft.util.math.vector.Vector3d shadow$position();
    @Shadow public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract void shadow$setWorld(World worldIn);
    @Shadow @Nullable public abstract ItemEntity shadow$entityDropItem(ItemStack stack, float offsetY);
    @Shadow protected abstract void shadow$setRotation(float yaw, float pitch);
    @Shadow @Nullable public abstract Entity shadow$getRidingEntity();
    @Shadow public abstract boolean shadow$isInvulnerableTo(DamageSource source);
    @Shadow public abstract AxisAlignedBB shadow$getBoundingBox();
    @Shadow public abstract boolean shadow$isSprinting();
    @Shadow public abstract boolean shadow$isOnSameTeam(Entity entityIn);
    @Shadow public abstract double shadow$getDistanceSq(Entity entityIn);
    @Shadow public abstract SoundCategory shadow$getSoundCategory();
    @Shadow @Nullable public abstract Team shadow$getTeam();
    @Shadow public abstract void shadow$extinguish();
    @Shadow protected abstract void shadow$setFlag(int flag, boolean set);
    @Shadow public abstract EntityDataManager shadow$getDataManager();
    @Shadow public abstract void shadow$setLocationAndAngles(double x, double y, double z, float yaw, float pitch);
    // @formatter:on

    private boolean impl$isConstructing = true;
    private boolean impl$untargetable = false;
    private boolean impl$isVanished = false;
    private boolean impl$pendingVisibilityUpdate = false;
    private int impl$visibilityTicks = 0;
    private boolean impl$collision = true;
    private boolean impl$invulnerable = false;
    private boolean impl$transient = false;
    protected boolean impl$hasCustomFireImmuneTicks = false;
    protected short impl$fireImmuneTicks = 0;

    // When changing custom data it is serialized on to this.
    // On writeInternal the SpongeData tag is added to the new CompoundNBT accordingly
    // In a Forge environment the ForgeData tag is managed by forge
    // Structure: tileNbt - ForgeData - SpongeData - customdata
    private CompoundNBT impl$customDataCompound;

    @Override
    public boolean bridge$isConstructing() {
        return this.impl$isConstructing;
    }

    @Override
    public void bridge$fireConstructors() {
        this.impl$isConstructing = false;
    }

    @Override
    public boolean bridge$setLocation(final ServerLocation location) {
        if (this.removed || ((WorldBridge) location.getWorld()).bridge$isFake()) {
            return false;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(SpongeCommon.getActivePlugin());
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);

            final Vec3d originalPosition = this.shadow$position();

            net.minecraft.world.server.ServerWorld destinationWorld = (net.minecraft.world.server.ServerWorld) location.getWorld();

            if (this.shadow$getEntityWorld() != destinationWorld) {
                final ChangeEntityWorldEvent.Pre event = SpongeEventFactory.createChangeEntityWorldEventPre(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) this, (ServerWorld) this.shadow$getEntityWorld(), location.getWorld(),
                        location.getWorld());
                if (SpongeCommon.postEvent(event) && ((WorldBridge) event.getDestinationWorld()).bridge$isFake()) {
                    return false;
                }

                final ChangeEntityWorldEvent.Reposition repositionEvent =
                        SpongeEventFactory.createChangeEntityWorldEventReposition(frame.getCurrentCause(),
                                (org.spongepowered.api.entity.Entity) this, (ServerWorld) this.shadow$getEntityWorld(),
                                VecHelper.toVector3d(this.shadow$position()), location.getPosition(), event.getOriginalDestinationWorld(),
                                location.getPosition(), event.getDestinationWorld());

                if (SpongeCommon.postEvent(repositionEvent)) {
                    return false;
                }

                destinationWorld = (net.minecraft.world.server.ServerWorld) event.getDestinationWorld();

                this.shadow$setPosition(repositionEvent.getDestinationPosition().getX(),
                        repositionEvent.getDestinationPosition().getY(), repositionEvent.getDestinationPosition().getZ());
            } else {
                final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.getCurrentCause(),
                        (org.spongepowered.api.entity.Entity) this, VecHelper.toVector3d(this.shadow$position()),
                        location.getPosition(), location.getPosition());
                if (SpongeCommon.postEvent(event)) {
                    return false;
                }

                this.shadow$setPosition(event.getDestinationPosition().getX(), event.getDestinationPosition().getY(),
                        event.getDestinationPosition().getZ());
            }

            if (!destinationWorld.getChunkProvider().chunkExists((int) this.shadow$getPosX() >> 4, (int) this.shadow$getPosZ() >> 4)) {
                // Roll back the position
                this.shadow$setPosition(originalPosition.x, originalPosition.y, originalPosition.z);
                return false;
            }

            ((Entity) (Object) this).detach();

            net.minecraft.world.server.ServerWorld originalWorld = (net.minecraft.world.server.ServerWorld) this.shadow$getEntityWorld();
            ((PlatformServerWorldBridge) this.shadow$getEntityWorld()).bridge$removeEntity((Entity) (Object) this, true);
            this.bridge$revive();
            this.shadow$setWorld(destinationWorld);
            destinationWorld.addFromAnotherDimension((Entity) (Object) this);

            originalWorld.resetUpdateEntityTick();
            destinationWorld.resetUpdateEntityTick();

            final ChunkPos chunkPos = new ChunkPos((int) this.shadow$getPosX() >> 4, (int) this.shadow$getPosZ() >> 4);
            destinationWorld.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkPos, 1, ((Entity) (Object) this).getEntityId());
        }

        return true;
    }

    @Override
    public boolean bridge$dismountRidingEntity(final DismountType type) {
        if (!this.world.isRemote && (ShouldFire.RIDE_ENTITY_EVENT_DISMOUNT || ShouldFire.RIDE_ENTITY_EVENT)) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                frame.addContext(EventContextKeys.DISMOUNT_TYPE, type);
                if (SpongeCommon.postEvent(SpongeEventFactory.
                        createRideEntityEventDismount(frame.getCurrentCause(), (org.spongepowered.api.entity.Entity) this.shadow$getRidingEntity()))) {
                    return false;
                }
            }
        }

        final Entity tempEntity = this.shadow$getRidingEntity();
        if (tempEntity != null) {
            this.ridingEntity = null;
            ((EntityAccessor) tempEntity).invoker$removePassenger((Entity) (Object) this);
        }
        return true;
    }

    @Override
    public boolean bridge$removePassengers(final DismountType type) {
        boolean dismount = false;
        for (int i = this.passengers.size() - 1; i >= 0; --i) {
            dismount = ((EntityBridge) this.passengers.get(i)).bridge$dismountRidingEntity(type) || dismount;
        }
        return dismount;
    }

    @Override
    public boolean bridge$getIsInvulnerable() {
        return this.impl$invulnerable;
    }

    @Override
    public boolean bridge$isInvisible() {
        return this.shadow$isInvisible();
    }

    @Override
    public void bridge$setInvisible(final boolean invisible) {
        this.shadow$setInvisible(invisible);
        if (invisible) {
            final CompoundNBT spongeData = this.data$getSpongeData();
            spongeData.putBoolean(Constants.Sponge.Entity.IS_INVISIBLE, true);
        } else {
            if (this.data$hasSpongeData()) {
                this.data$getSpongeData().remove(Constants.Sponge.Entity.IS_INVISIBLE);
            }
        }
    }

    @Override
    public boolean bridge$isVanished() {
        return this.impl$isVanished;
    }

    @Override
    public void bridge$setVanished(final boolean vanished) {
        this.impl$isVanished = vanished;
        this.impl$pendingVisibilityUpdate = true;
        this.impl$visibilityTicks = 20;
        if (vanished) {
            final CompoundNBT spongeData = this.data$getSpongeData();
            spongeData.putBoolean(Constants.Sponge.Entity.IS_VANISHED, true);
        } else {
            if (this.data$hasSpongeData()) {
                final CompoundNBT spongeData = this.data$getSpongeData();
                spongeData.remove(Constants.Sponge.Entity.IS_VANISHED);
                spongeData.remove(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE);
                spongeData.remove(Constants.Sponge.Entity.VANISH_UNTARGETABLE);
            }
        }
    }

    @Override
    public boolean bridge$isUncollideable() {
        return this.impl$collision;
    }

    @Override
    public void bridge$setUncollideable(final boolean prevents) {
        this.impl$collision = prevents;
    }

    @Override
    public boolean bridge$isUntargetable() {
        return this.impl$untargetable;
    }

    @Override
    public void bridge$setUntargetable(final boolean untargetable) {
        this.impl$untargetable = untargetable;
    }

    @Override
    public Timing bridge$getTimingsHandler() {
        return ((EntityTypeBridge) this.shadow$getType()).bridge$getTimings();
    }

    @Override
    public void bridge$setInvulnerable(final boolean value) {
        this.impl$invulnerable = value;
    }

    @Override
    public void bridge$setTransient(final boolean value) {
        this.impl$transient = value;
    }

    @Override
    public void bridge$setFireImmuneTicks(final int ticks) {
        this.impl$hasCustomFireImmuneTicks = true;
        this.impl$fireImmuneTicks = (short) ticks;
    }

    @Override
    public CommandSource bridge$getCommandSource(final Cause cause) {
        return this.shadow$getCommandSource();
    }

    @Override
    public void bridge$setTransform(final Transform transform) {
        this.shadow$setPosition(transform.getPosition().getX(), transform.getPosition().getY(), transform.getPosition().getZ());
        this.shadow$setRotation((float) transform.getYaw(), (float) transform.getPitch());
    }

    @Override
    public CompoundNBT data$getCompound() {
        return this.impl$customDataCompound;
    }

    @Override
    public void data$setCompound(CompoundNBT nbt) {
        this.impl$customDataCompound = nbt;
    }

    @Override
    public NBTDataType data$getNBTDataType() {
        return NBTDataTypes.ENTITY;
    }

    /**
     * @author Zidane
     * @reason This is a catch-all method to ensure MoveEntityEvent is fired with
     *         useful information
     */
    @Overwrite
    public final void teleportKeepLoaded(double x, double y, double z) {
        if (this.world instanceof net.minecraft.world.server.ServerWorld) {
            // Sponge start
            final PhaseTracker server = PhaseTracker.SERVER;
            boolean hasMovementContext = true;
            if (!server.getCurrentContext().containsKey(EventContextKeys.MOVEMENT_TYPE)) {
                hasMovementContext = false;
                server.pushCause(SpongeCommon.getActivePlugin());
                server.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);
            }

            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(server.getCurrentCause(),
                    (org.spongepowered.api.entity.Entity) this, VecHelper.toVector3d(this.shadow$position()), new Vector3d(x, y, z),
                    new Vector3d(x, y, z));

            if (!hasMovementContext) {
                server.popCause();
                server.removeContext(EventContextKeys.MOVEMENT_TYPE);
            }

            if (SpongeCommon.postEvent(event)) {
                return;
            }

            final Vector3d destinationPosition = event.getDestinationPosition();
            // Sponge end
            ChunkPos chunkpos = new ChunkPos(new BlockPos(destinationPosition.getX(), destinationPosition.getY(), destinationPosition.getZ()));
            ((net.minecraft.world.server.ServerWorld)this.world).getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 0,
                    this.shadow$getEntityId());
            this.world.getChunk(chunkpos.x, chunkpos.z);
            this.shadow$setPositionAndUpdate(destinationPosition.getX(), destinationPosition.getY(), destinationPosition.getZ());
        }
    }

    /**
     * @author Zidane
     * @reason Call to EntityUtil to handle dimension changes
     */
    @Nullable
    @Overwrite
    public Entity changeDimension(net.minecraft.world.server.ServerWorld destination) {
        if (this.shadow$getEntityWorld().isRemote || this.removed) {
            return null;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PORTAL);

            Teleporter p = destination.getPortalForcer()
            final DimensionChangeResult<Entity> result = EntityUtil.invokePortalTo((Entity) (Object) this, new WrappedITeleporterPortalType(
                    (PlatformITeleporterBridge) destination.getPortalForcer(), null), destination);

            if (!result.isSuccess() && result.shouldRemove()) {
                return null;
            }

            return result.getEntity();
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;ridingEntity:Lnet/minecraft/entity/Entity;",
            ordinal = 0
        ),
        cancellable = true
    )
    private void impl$onStartRiding(final Entity vehicle, final boolean force,
        final CallbackInfoReturnable<Boolean> ci) {
        if (!this.world.isRemote && (ShouldFire.RIDE_ENTITY_EVENT_MOUNT || ShouldFire.RIDE_ENTITY_EVENT)) {
            PhaseTracker.getCauseStackManager().pushCause(this);
            if (SpongeCommon.postEvent(SpongeEventFactory.createRideEntityEventMount(PhaseTracker.getCauseStackManager().getCurrentCause(), (org.spongepowered.api.entity.Entity) vehicle))) {
                ci.cancel();
            }
            PhaseTracker.getCauseStackManager().popCause();
        }
    }

    /**
     * @author rexbut - December 16th, 2016
     * @reason - adjusted to support {@link DismountTypes}
     */
    @Overwrite
    public void stopRiding() {
        if (this.shadow$getRidingEntity() != null) {
            if (this.shadow$getRidingEntity().removed) {
                this.bridge$dismountRidingEntity(DismountTypes.DEATH.get());
            } else {
                this.bridge$dismountRidingEntity(DismountTypes.PLAYER.get());
            }
        }
    }

/*
    @Inject(method = "move",
        at = @At("HEAD"),
        cancellable = true)
    private void impl$onSpongeMoveEntity(final MoverType type, final Vec3d vec3d, final CallbackInfo ci) {
        if (!this.world.isRemote && !SpongeHooks.checkEntitySpeed(((Entity) (Object) this), vec3d.getX(), vec3d.getY(), vec3d.getZ())) {
            ci.cancel();
        }
    }
*/
    @Redirect(method = "setOnFireFromLava",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
        )
    )
    private boolean impl$createLavaBlockDamageSource(final Entity entity, final DamageSource source, final float damage) {
        if (this.world.isRemote) { // Short circuit
            return entity.attackEntityFrom(source, damage);
        }
        try {
            final AxisAlignedBB bb = this.shadow$getBoundingBox().grow(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D);
            final ServerLocation location = DamageEventHandler.findFirstMatchingBlock((Entity) (Object) this, bb, block ->
                block.getMaterial() == Material.LAVA);
            final MinecraftBlockDamageSource lava = new MinecraftBlockDamageSource("lava", location);
            ((DamageSourceBridge) (Object) lava).bridge$setLava(); // Bridge to bypass issue with using accessor mixins within mixins
            return entity.attackEntityFrom(DamageSource.LAVA, damage);
        } finally {
            // Since "source" is already the DamageSource.LAVA object, we can simply re-use it here.
            ((DamageSourceBridge) source).bridge$setLava();
        }

    }

    @Redirect(method = "dealFireDamage",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"
        )
    )
    private boolean impl$spongeRedirectForFireDamage(final Entity entity, final DamageSource source, final float damage) {
        if (this.world.isRemote) { // Short Circuit
            return entity.attackEntityFrom(source, damage);
        }
        try {
            final AxisAlignedBB bb = this.shadow$getBoundingBox().shrink(-0.001D);
            final ServerLocation location = DamageEventHandler.findFirstMatchingBlock((Entity) (Object) this, bb, block ->
                block.getBlock() == Blocks.FIRE || block.getBlock() == Blocks.LAVA);

            final MinecraftBlockDamageSource fire = new MinecraftBlockDamageSource("inFire", location);
            ((DamageSourceBridge) (Object) fire).bridge$setFireSource();
            return entity.attackEntityFrom(DamageSource.IN_FIRE, damage);
        } finally {
            // Since "source" is already the DamageSource.IN_FIRE object, we can re-use it to re-assign.
            ((DamageSourceBridge) source).bridge$setFireSource();
        }

    }

    /*
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "setPosition",
        at = @At("HEAD"))
    private void impl$capturePlayerPosition(final double x, final double y, final double z, final CallbackInfo ci) {
        if ((Entity) (Object) this instanceof ServerPlayerEntity) {
            final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.connection != null) {
                ((ServerPlayNetHandlerBridge) player.connection).bridge$captureCurrentPlayerPosition();
            }
        }
    }


    @SuppressWarnings({"ConstantConditions", "RedundantCast"})
    @Inject(method = "tick",
        at = @At("RETURN"))
    private void impl$updateVanishState(final CallbackInfo callbackInfo) {
        if (this.impl$pendingVisibilityUpdate && !this.world.isRemote) {
            final EntityTrackerAccessor trackerAccessor = ((ChunkManagerAccessor) ((ServerWorld) this.world).getChunkProvider().chunkManager).accessor$getEntityTrackers().get(this.shadow$getEntityId());
            if (trackerAccessor != null && this.impl$visibilityTicks % 4 == 0) {
                if (this.impl$isVanished) {
                    for (final ServerPlayerEntity entityPlayerMP : trackerAccessor.accessor$getTrackingPlayers()) {
                        entityPlayerMP.connection.sendPacket(new SDestroyEntitiesPacket(this.shadow$getEntityId()));
                        if ((Entity) (Object) this instanceof ServerPlayerEntity) {
                            entityPlayerMP.connection.sendPacket(
                                new SPlayerListItemPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER, (ServerPlayerEntity) (Object) this));
                        }
                    }
                } else {
                    this.impl$visibilityTicks = 1;
                    this.impl$pendingVisibilityUpdate = false;
                    for (final ServerPlayerEntity entityPlayerMP : SpongeCommon.getServer().getPlayerList().getPlayers()) {
                        if ((Entity) (Object) this == entityPlayerMP) {
                            continue;
                        }
                        if ((Entity) (Object) this instanceof ServerPlayerEntity) {
                            final IPacket<?> packet = new SPlayerListItemPacket(SPlayerListItemPacket.Action.ADD_PLAYER, (ServerPlayerEntity) (Object) this);
                            entityPlayerMP.connection.sendPacket(packet);
                        }
                        trackerAccessor.accessor$getEntry().sendSpawnPackets(entityPlayerMP.connection::sendPacket);
                    }
                }
            }
            if (this.impl$visibilityTicks > 0) {
                this.impl$visibilityTicks--;
            } else {
                this.impl$pendingVisibilityUpdate = false;
            }
        }
    }

    */

    /**
     * Hooks into vanilla's writeToNBT to call {@link #impl$writeToSpongeCompound}.
     *
     * <p> This makes it easier for other entity mixins to override writeToNBT
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write
     *     to SpongeData)
     * @param ci (Unused) callback info
     *//*

    @Inject(method = "writeWithoutTypeId(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;",
        at = @At("HEAD"))
    private void impl$spongeWriteToNBT(final CompoundNBT compound, final CallbackInfoReturnable<CompoundNBT> ci) {
        this.impl$writeToSpongeCompound(((DataCompoundHolder) this).data$getSpongeDataCompound());
    }

    @Override
    public void bridge$setImplVelocity(final Vector3d velocity) {
        this.motion = VecHelper.toVec3d(velocity);
        this.velocityChanged = true;
    }

    @Redirect(
        method = "move",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;onEntityWalk(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"
        )
    )
    private void impl$onEntityCollideWithBlockThrowEventSponge(final Block block, final net.minecraft.world.World world,
        final BlockPos pos, final Entity entity) {
        // if block can't collide, return
        if (!((BlockBridge) block).bridge$hasCollideLogic()) {
            return;
        }

        if (world.isRemote) {
            block.onEntityWalk(world, pos, entity);
            return;
        }

        final BlockState state = world.getBlockState(pos);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, Direction.NONE)) {
            block.onEntityWalk(world, pos, entity);
            this.impl$lastCollidedBlockPos = pos;
        }

    }

    @Redirect(method = "doBlockCollisions",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"
        )
    ) // doBlockCollisions
    private void impl$onEntityCollideWithBlockState(final BlockState blockState,
        final net.minecraft.world.World worldIn, final BlockPos pos, final Entity entityIn) {
        // if block can't collide, return
        if (!((BlockBridge) blockState.getBlock()).bridge$hasCollideWithStateLogic()) {
            return;
        }

        if (world.isRemote) {
            blockState.onEntityCollision(world, pos, entityIn);
            return;
        }

        if (!SpongeCommonEventFactory.handleCollideBlockEvent(blockState.getBlock(), world, pos, blockState, entityIn, Direction.NONE)) {
            blockState.onEntityCollision(world, pos, entityIn);
            this.impl$lastCollidedBlockPos = pos;
        }

    }

    @Redirect(method = "updateFallState",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/Block;onFallenUpon(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;F)V"))
    private void impl$onBlockFallenUpon(final Block block, final net.minecraft.world.World world, final BlockPos pos,
        final Entity entity, final float fallDistance) {
        if (world.isRemote) {
            block.onFallenUpon(world, pos, entity, fallDistance);
            return;
        }

        final BlockState state = world.getBlockState(pos);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, Direction.UP)) {
            block.onFallenUpon(world, pos, entity, fallDistance);
            this.impl$lastCollidedBlockPos = pos;
        }

    }
*/

    /**
         * @author gabizou - January 4th, 2016
         * @reason gabizou - January 27th, 2016 - Rewrite to a redirect
         *     <p>
         *     This prevents sounds from being sent to the server by entities that are vanished
         *//*

    @Redirect(method = "playSound",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;isSilent()Z"))
    private boolean impl$checkIsSilentOrInvis(final Entity entity) {
        return entity.isSilent() || this.impl$isVanished;
    }

    @Redirect(method = "applyEntityCollision",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;noClip:Z",
            opcode = Opcodes.GETFIELD))
    private boolean impl$applyEntityCollisionCheckVanish(final Entity entity) {
        return entity.noClip || ((VanishableBridge) entity).bridge$isVanished();
    }

    @Redirect(method = "doWaterSplashEffect",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    private void impl$spawnParticle(final net.minecraft.world.World world, final IParticleData particleTypes,
        final double xCoord, final double yCoord, final double zCoord,
        final double xOffset, final double yOffset, final double zOffset) {
        if (!this.impl$isVanished) {
            this.world.addParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset);
        }
    }

    @Redirect(method = "createRunningParticles",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    private void impl$runningSpawnParticle(final net.minecraft.world.World world, final IParticleData particleTypes,
        final double xCoord, final double yCoord, final double zCoord,
        final double xOffset, final double yOffset, final double zOffset) {
        if (!this.impl$isVanished) {
            this.world.addParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset);
        }
    }
    */

    /**
     * @return
     * @author gabizou - January 30th, 2016
     * @author blood - May 12th, 2016
     * @author gabizou - June 2nd, 2016
     *     <p>
     *     TODO from i509VCB: gabizou's remider to refactor this code here
     * @reason Rewrites the method entirely for several reasons:
     *     1) If we are in a forge environment, we do NOT want forge to be capturing the item entities, because we handle them ourselves
     *     2) If we are in a client environment, we should not perform any sort of processing whatsoever.
     *     3) This method is entirely managed from the standpoint where our events have final say, as per usual.
     */
/*
    @javax.annotation.Nullable
    @Overwrite
    @Nullable
    public ItemEntity entityDropItem(final ItemStack stack, final float offsetY) {
        // Sponge Start
        // Gotta stick with the client side handling things
        if (this.world.isRemote) {
            // Sponge End - resume normal client code. Server side we will handle it elsewhere
            if (stack.isEmpty()) {
                return null;
            } else {
                final ItemEntity entityitem = new ItemEntity(this.world, this.posX, this.posY + (double) offsetY, this.posZ, stack);
                entityitem.setDefaultPickupDelay();
                this.world.addEntity(entityitem);
                return entityitem;
            }
        }
        // Sponge - Redirect server sided code to handle through the PhaseTracker
        return EntityUtil.entityOnDropItem((Entity) (Object) this, stack, offsetY, ((Entity) (Object) this).posX, ((Entity) (Object) this).posZ);
    }

    @Nullable
    @Override
    public BlockPos bridge$getLastCollidedBlockPos() {
        return this.impl$lastCollidedBlockPos;
    }
*/

/*
    @Redirect(method = "setFire",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/entity/Entity;fire:I",
            opcode = Opcodes.PUTFIELD)
    )
    private void impl$ThrowIgniteEventForFire(final Entity entity, final int ticks) {
        if (((WorldBridge) this.world).bridge$isFake() || !ShouldFire.IGNITE_ENTITY_EVENT) {
            this.fire = ticks; // Vanilla functionality
            return;
        }
        if (this.fire < 1 && !this.impl$isImmuneToFireForIgniteEvent()) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {

                frame.pushCause(((org.spongepowered.api.entity.Entity) this).getLocation().getWorld());
                final IgniteEntityEvent event = SpongeEventFactory.
                    createIgniteEntityEvent(frame.getCurrentCause(), ticks, ticks, (org.spongepowered.api.entity.Entity) this);

                if (SpongeCommon.postEvent(event)) {
                    this.fire = 0;
                    return; // set fire ticks to 0
                }
                this.fire = event.getFireTicks();
            }
        }
    }

    */

    @Redirect(method = "getEntityString", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;isSerializable()Z"))
    private boolean impl$respectTransientFlag(final EntityType entityType) {
        if (!entityType.isSerializable()) {
            return false;
        }

        return !this.impl$transient;
    }

    /*
    @Redirect(method = "onStruckByLightning",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"))
    private boolean impl$ThrowDamageEventWithLightingSource(
        final Entity entity, final DamageSource source, final float damage, final LightningBoltEntity lightningBolt) {
        if (!this.world.isRemote) {
            return entity.attackEntityFrom(source, damage);
        }
        try {
            final EntityDamageSource lightning = new EntityDamageSource("lightningBolt", lightningBolt);
            ((DamageSourceBridge) lightning).bridge$setLightningSource();
            return entity.attackEntityFrom(DamageSource.LIGHTNING_BOLT, damage);
        } finally {
            ((DamageSourceBridge) source).bridge$setLightningSource();
        }
    }

    @Inject(method = "remove()V",
        at = @At(value = "RETURN"))
    private void impl$createDestructionEventOnDeath(final CallbackInfo ci) {
        if (ShouldFire.DESTRUCT_ENTITY_EVENT
            && !((WorldBridge) this.world).bridge$isFake()
            && !((Entity) (Object) this instanceof MobEntity)) {

            this.impl$destructCause = PhaseTracker.getCauseStackManager().getCurrentCause();
        }
    }
*/

    @Inject(method = "getFireImmuneTicks", at = @At(value = "HEAD"), cancellable = true)
    private void impl$getFireImmuneTicks(final CallbackInfoReturnable<Integer> ci) {
        if (this.impl$hasCustomFireImmuneTicks) {
            ci.setReturnValue((int) this.impl$fireImmuneTicks);
        }
    }

    // TODO overrides for ForgeData
    // @Shadow private CompoundNBT customEntityData;
    // @Override CompoundNBT data$getForgeData()
    // @Override CompoundNBT data$getForgeData()
    // @Override CompoundNBT data$hasForgeData()
    // @Override CompoundNBT cleanEmptySpongeData()

    @Inject(method = "writeWithoutTypeId", at = @At("RETURN"))
    private void impl$WriteSpongeDataToCompound(final CompoundNBT compound, final CallbackInfoReturnable<CompoundNBT> ci) {
        if (this.data$hasSpongeData()) {
            final CompoundNBT forgeCompound = compound.getCompound(Constants.Forge.FORGE_DATA);
            // If we are in Forge data is already present
            if (forgeCompound != this.data$getForgeData()) {
                if (forgeCompound.isEmpty()) { // In vanilla this should be an new detached empty compound
                    compound.put(Constants.Forge.FORGE_DATA, forgeCompound);
                }
                // Get our nbt data and write it to the compound
                forgeCompound.put(Constants.Sponge.SPONGE_DATA, this.data$getSpongeData());
            }
        }
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void impl$ReadSpongeDataFromCompound(final CompoundNBT compound, final CallbackInfo ci) {
        // If we are in Forge data is already present
        this.data$setCompound(compound); // For vanilla we set the incoming nbt
        if (this.data$hasSpongeData()) {
            // Deserialize our data...
            CustomDataHolderBridge.syncTagToCustom(this);
            this.data$setCompound(null); // For vanilla this will be recreated empty in the next call - for Forge it reuses the existing compound instead
            // ReSync our data (includes failed data)
            CustomDataHolderBridge.syncCustomToTag(this);
        } else {
            this.data$setCompound(null); // No data? No need to keep the nbt
        }
    }

    /**
     * Read extra data (SpongeData) from the entity's NBT tag. This is
     * meant to be overridden for each impl based mixin that has to store
     * custom fields based on it's implementation. Examples can include:
     * vanishing booleans, maximum air, maximum boat speeds and modifiers,
     * etc.
     *
     * @param compound The SpongeData compound to read from
     */

    protected void impl$readFromSpongeCompound(final CompoundNBT compound) {
        CustomDataHolderBridge.syncTagToCustom(this);

        if (this instanceof GrieferBridge && ((GrieferBridge) this).bridge$isGriefer() && compound.contains(Constants.Sponge.Entity.CAN_GRIEF)) {
            ((GrieferBridge) this).bridge$setCanGrief(compound.getBoolean(Constants.Sponge.Entity.CAN_GRIEF));
        }
        if (compound.contains(Constants.Sponge.Entity.IS_VANISHED, Constants.NBT.TAG_BYTE)) {
            this.bridge$setVanished(compound.getBoolean(Constants.Sponge.Entity.IS_VANISHED));
            this.bridge$setUncollideable(compound.getBoolean(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE));
            this.bridge$setUntargetable(compound.getBoolean(Constants.Sponge.Entity.VANISH_UNTARGETABLE));
        }
        if (compound.contains(Constants.Sponge.Entity.IS_INVISIBLE, Constants.NBT.TAG_BYTE)) {
            this.bridge$setInvisible(compound.getBoolean(Constants.Sponge.Entity.IS_INVISIBLE));
        }

        CustomDataHolderBridge.syncCustomToTag(this);
    }

    /**
     * Write extra data (SpongeData) to the entity's NBT tag. This is
     * meant to be overridden for each impl based mixin that has to store
     * custom fields based on it's implementation. Examples can include:
     * vanishing booleans, maximum air, maximum boat speeds and modifiers,
     * etc.
     *
     * @param compound The SpongeData compound to write to
     */
    protected void impl$writeToSpongeCompound(final CompoundNBT compound) {
        CustomDataHolderBridge.syncCustomToTag(this);

        if (this instanceof GrieferBridge && ((GrieferBridge) this).bridge$isGriefer() && ((GrieferBridge) this).bridge$canGrief()) {
            compound.putBoolean(Constants.Sponge.Entity.CAN_GRIEF, true);
        }
        if (this.bridge$isVanished()) {
            compound.putBoolean(Constants.Sponge.Entity.IS_VANISHED, true);
            compound.putBoolean(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE, this.bridge$isUncollideable());
            compound.putBoolean(Constants.Sponge.Entity.VANISH_UNTARGETABLE, this.bridge$isUntargetable());
        }
        if (this.shadow$isInvisible()) {
            compound.putBoolean(Constants.Sponge.Entity.IS_INVISIBLE, true);
        }
    }

    /**
     * Overridden method for Players to determine whether this entity is immune to fire
     * such that {@link IgniteEntityEvent}s are not needed to be thrown as they cannot
     * take fire damage, nor do they light on fire.
     *
     * @return True if this entity is immune to fire.
     */
    protected boolean impl$canCallIgniteEntityEvent() {
        return false;
    }
}
