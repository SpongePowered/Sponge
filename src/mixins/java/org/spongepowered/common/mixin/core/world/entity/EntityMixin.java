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
package org.spongepowered.common.mixin.core.world.entity;

import co.aikar.timings.Timing;
import net.minecraft.BlockUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.spongepowered.api.data.Keys;
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
import org.spongepowered.api.world.server.ServerLocation;
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
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.commands.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.world.entity.EntityTypeBridge;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.server.level.ServerPlayerBridge;
import org.spongepowered.common.bridge.world.damagesource.DamageSourceBridge;
import org.spongepowered.common.bridge.world.level.PlatformServerLevelBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.cause.entity.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.TeleportContext;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.util.MinecraftBlockDamageSource;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.NetherPortalType;
import org.spongepowered.common.world.portal.PlatformTeleporter;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityBridge, PlatformEntityBridge, VanishableBridge, TimingBridge, CommandSourceProviderBridge, DataCompoundHolder {

    // @formatter:off
    @Shadow public net.minecraft.world.level.Level level;
    @Shadow public float yRot;
    @Shadow public float xRot;
    @Shadow public int invulnerableTime;
    @Shadow public boolean removed;
    @Shadow public float walkDistO;
    @Shadow public float walkDist;
    @Shadow @Final protected Random random;
    @Shadow @Final protected SynchedEntityData entityData;
    @Shadow public float yRotO;
    @Shadow protected int portalTime;
    @Shadow @Nullable private Entity vehicle;
    @Shadow @Final private List<Entity> passengers;
    @Shadow protected boolean onGround;
    @Shadow public float fallDistance;
    @Shadow protected BlockPos portalEntrancePos;
    @Shadow private net.minecraft.world.phys.Vec3 position;
    @Shadow private BlockPos blockPosition;
    @Shadow public double xo;
    @Shadow public double yo;
    @Shadow public double zo;

    @Shadow public abstract void shadow$setPos(double x, double y, double z);
    @Shadow public abstract double shadow$getX();
    @Shadow public abstract double shadow$getY();
    @Shadow public abstract double shadow$getZ();
    @Shadow public abstract void shadow$remove();
    @Shadow public abstract void shadow$setCustomName(@Nullable Component name);
    @Shadow public abstract boolean shadow$hurt(DamageSource source, float amount);
    @Shadow public abstract int shadow$getId();
    @Shadow public abstract boolean shadow$isVehicle();
    @Shadow public abstract void shadow$playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow protected abstract void shadow$removePassenger(Entity passenger);
    @Shadow public abstract boolean shadow$isInvisible();
    @Shadow public abstract void shadow$setInvisible(boolean invisible);
    @Shadow protected abstract int shadow$getFireImmuneTicks();
    @Shadow public abstract EntityType<?> shadow$getType();
    @Shadow public abstract boolean shadow$isInWater();
    @Shadow public abstract boolean shadow$isPassenger();
    @Shadow public abstract void shadow$teleportTo(double x, double y, double z);
    @Shadow public abstract int shadow$getMaxAirSupply();
    @Shadow public abstract void shadow$doEnchantDamageEffects(LivingEntity entityLivingBaseIn, Entity entityIn);
    @Shadow public abstract CommandSourceStack shadow$createCommandSourceStack();
    @Shadow public abstract Level shadow$getCommandSenderWorld();
    @Shadow public abstract net.minecraft.world.phys.Vec3 shadow$position();
    @Shadow public abstract MinecraftServer shadow$getServer();
    @Shadow public abstract void shadow$setLevel(Level worldIn);
    @Shadow @Nullable public abstract ItemEntity shadow$spawnAtLocation(ItemStack stack, float offsetY);
    @Shadow protected abstract void shadow$setRot(float yaw, float pitch);
    @Shadow @Nullable public abstract Entity shadow$getVehicle();
    @Shadow public abstract boolean shadow$isInvulnerableTo(DamageSource source);
    @Shadow public abstract AABB shadow$getBoundingBox();
    @Shadow public abstract boolean shadow$isSprinting();
    @Shadow public abstract boolean shadow$isAlliedTo(Entity entityIn);
    @Shadow public abstract double shadow$distanceToSqr(Entity entityIn);
    @Shadow public abstract SoundSource shadow$getSoundSource();
    @Shadow @Nullable public abstract Team shadow$getTeam();
    @Shadow public abstract void shadow$clearFire();
    @Shadow protected abstract void shadow$setSharedFlag(int flag, boolean set);
    @Shadow public abstract SynchedEntityData shadow$getEntityData();
    @Shadow public abstract void shadow$moveTo(double x, double y, double z);
    @Shadow public abstract void shadow$absMoveTo(double x, double y, double z, float yaw, float pitch);
    @Shadow public abstract net.minecraft.world.phys.Vec3 shadow$getDeltaMovement();
    @Shadow public abstract void shadow$setDeltaMovement(net.minecraft.world.phys.Vec3 motion);
    @Shadow public abstract void shadow$unRide();
    @Shadow protected abstract Optional<BlockUtil.FoundRectangle> shadow$getExitPortal(
            net.minecraft.server.level.ServerLevel targetWorld, BlockPos targetPosition, boolean isNether);
    @Shadow protected abstract net.minecraft.world.phys.Vec3 shadow$getRelativePortalPosition(Direction.Axis direction$axis,
            BlockUtil.FoundRectangle teleportationrepositioner$result);
    @Shadow protected abstract void shadow$removeAfterChangingDimensions();
    @Shadow public abstract void shadow$absMoveTo(double p_242281_1_, double p_242281_3_, double p_242281_5_);
    @Shadow protected abstract int shadow$getPermissionLevel();
    @Shadow protected abstract Vec3 shadow$collide(Vec3 param0);
    // @formatter:on

    private boolean impl$isConstructing = true;
    private boolean impl$vanishPreventsTargeting = false;
    private boolean impl$isVanished = false;
    private boolean impl$pendingVisibilityUpdate = false;
    private int impl$visibilityTicks = 0;
    private boolean impl$vanishIgnoresCollision = true;
    private boolean impl$transient = false;
    private boolean impl$shouldFireRepositionEvent = true;
    private WeakReference<ServerWorld> impl$originalDestinationWorld = null;
    protected boolean impl$hasCustomFireImmuneTicks = false;
    protected boolean impl$dontCreateExitPortal = false;
    protected short impl$fireImmuneTicks = 0;
    private BlockPos impl$lastCollidedBlockPos;

    // When changing custom data it is serialized on to this.
    // On writeInternal the SpongeData tag is added to the new CompoundNBT accordingly
    // In a Forge environment the ForgeData tag is managed by forge
    // Structure: tileNbt - ForgeData - SpongeData - customdata
    private CompoundTag impl$customDataCompound;

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
        if (this.removed || ((WorldBridge) location.world()).bridge$isFake()) {
            return false;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(SpongeCommon.getActivePlugin());
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);

            final net.minecraft.world.phys.Vec3 originalPosition = this.shadow$position();

            net.minecraft.server.level.ServerLevel destinationWorld = (net.minecraft.server.level.ServerLevel) location.world();

            if (this.shadow$getCommandSenderWorld() != destinationWorld) {
                final ChangeEntityWorldEvent.Pre event = SpongeEventFactory.createChangeEntityWorldEventPre(frame.currentCause(),
                        (org.spongepowered.api.entity.Entity) this, (ServerWorld) this.shadow$getCommandSenderWorld(), location.world(),
                        location.world());
                if (SpongeCommon.postEvent(event) && ((WorldBridge) event.destinationWorld()).bridge$isFake()) {
                    return false;
                }

                final ChangeEntityWorldEvent.Reposition repositionEvent =
                        SpongeEventFactory.createChangeEntityWorldEventReposition(frame.currentCause(),
                                (org.spongepowered.api.entity.Entity) this, (ServerWorld) this.shadow$getCommandSenderWorld(),
                                VecHelper.toVector3d(this.shadow$position()), location.position(), event.originalDestinationWorld(),
                                location.position(), event.destinationWorld());

                if (SpongeCommon.postEvent(repositionEvent)) {
                    return false;
                }

                destinationWorld = (net.minecraft.server.level.ServerLevel) event.destinationWorld();

                this.shadow$setPos(repositionEvent.destinationPosition().x(),
                        repositionEvent.destinationPosition().y(), repositionEvent.destinationPosition().z());
            } else {
                final Vector3d destination;
                if (ShouldFire.MOVE_ENTITY_EVENT) {
                    final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.currentCause(),
                            (org.spongepowered.api.entity.Entity) this, VecHelper.toVector3d(this.shadow$position()),
                            location.position(), location.position());
                    if (SpongeCommon.postEvent(event)) {
                        return false;
                    }
                    destination = event.destinationPosition();
                } else {
                    destination = location.position();
                }
                this.shadow$setPos(destination.x(), destination.y(), destination.z());
            }

            if (!destinationWorld.getChunkSource().hasChunk((int) this.shadow$getX() >> 4, (int) this.shadow$getZ() >> 4)) {
                // Roll back the position
                this.shadow$setPos(originalPosition.x, originalPosition.y, originalPosition.z);
                return false;
            }

            ((Entity) (Object) this).unRide();

            final net.minecraft.server.level.ServerLevel originalWorld = (net.minecraft.server.level.ServerLevel) this.shadow$getCommandSenderWorld();
            ((PlatformServerLevelBridge) this.shadow$getCommandSenderWorld()).bridge$removeEntity((Entity) (Object) this, true);
            this.bridge$revive();
            this.shadow$setLevel(destinationWorld);
            destinationWorld.addFromAnotherDimension((Entity) (Object) this);

            originalWorld.resetEmptyTime();
            destinationWorld.resetEmptyTime();

            final ChunkPos chunkPos = new ChunkPos((int) this.shadow$getX() >> 4, (int) this.shadow$getZ() >> 4);
            destinationWorld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkPos, 1, ((Entity) (Object) this).getId());
        }

        return true;
    }

    @Override
    public boolean bridge$dismountRidingEntity(final DismountType type) {
        if (!this.level.isClientSide && ShouldFire.RIDE_ENTITY_EVENT_DISMOUNT) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                frame.addContext(EventContextKeys.DISMOUNT_TYPE, type);
                if (SpongeCommon.postEvent(SpongeEventFactory.
                        createRideEntityEventDismount(frame.currentCause(), (org.spongepowered.api.entity.Entity) this.shadow$getVehicle()))) {
                    return false;
                }
            }
        }

        final Entity tempEntity = this.shadow$getVehicle();
        if (tempEntity != null) {
            this.vehicle = null;
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
    public boolean bridge$isInvisible() {
        return this.shadow$isInvisible();
    }

    @Override
    public void bridge$setInvisible(final boolean invisible) {
        this.shadow$setInvisible(invisible);
        if (invisible) {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.IS_INVISIBLE, true);
        } else {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.IS_INVISIBLE);
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
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.VANISH, true);
        } else {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.VANISH);
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.VANISH_IGNORES_COLLISION);
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.VANISH_PREVENTS_TARGETING);
        }
    }

    @Override
    public boolean bridge$isVanishIgnoresCollision() {
        return this.impl$vanishIgnoresCollision;
    }

    @Override
    public void bridge$setVanishIgnoresCollision(final boolean vanishIgnoresCollision) {
        this.impl$vanishIgnoresCollision = vanishIgnoresCollision;
        if (vanishIgnoresCollision) {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.VANISH_IGNORES_COLLISION);
        } else {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.VANISH_IGNORES_COLLISION, false);
        }
    }

    @Override
    public boolean bridge$isVanishPreventsTargeting() {
        return this.impl$vanishPreventsTargeting;
    }

    @Override
    public void bridge$setVanishPreventsTargeting(final boolean vanishPreventsTargeting) {
        this.impl$vanishPreventsTargeting = vanishPreventsTargeting;
        if (vanishPreventsTargeting) {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.VANISH_PREVENTS_TARGETING, true);
        } else {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.VANISH_PREVENTS_TARGETING);
        }
    }

    @Override
    public Timing bridge$getTimingsHandler() {
        return ((EntityTypeBridge) this.shadow$getType()).bridge$getTimings();
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
    public CommandSourceStack bridge$getCommandSource(final Cause cause) {
        return this.shadow$createCommandSourceStack();
    }

    @Override
    public void bridge$setTransform(final Transform transform) {
        this.shadow$setPos(transform.position().x(), transform.position().y(), transform.position().z());
        this.shadow$setRot((float) transform.yaw(), (float) transform.pitch());
    }

    @Redirect(method = "findDimensionEntryPoint", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getExitPortal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Z)Ljava/util/Optional;"))
    private Optional<BlockUtil.FoundRectangle> impl$redirectGetExitPortal(
            final Entity thisEntity,
            final net.minecraft.server.level.ServerLevel targetWorld,
            final BlockPos targetPosition,
            final boolean targetIsNether) {
        try {
            return this.shadow$getExitPortal(targetWorld, targetPosition, targetIsNether);
        } finally {
            // Reset for the next attempt.
            this.impl$dontCreateExitPortal = false;
        }
    }

    @Override
    public Entity bridge$portalRepositioning(final boolean createEndPlatform,
            final net.minecraft.server.level.ServerLevel serverworld,
            final net.minecraft.server.level.ServerLevel targetWorld,
            final PortalInfo portalinfo) {
        serverworld.getProfiler().popPush("reloading");
        final Entity entity = this.shadow$getType().create(targetWorld);
        if (entity != null) {
            entity.restoreFrom((Entity) (Object) this);
            entity.moveTo(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z, portalinfo.yRot, entity.xRot);
            entity.setDeltaMovement(portalinfo.speed);
            targetWorld.addFromAnotherDimension(entity);
            if (createEndPlatform && targetWorld.dimension() == Level.END) {
                net.minecraft.server.level.ServerLevel.makeObsidianPlatform(targetWorld);
            }
        }
        return entity;
    }

    @Override
    public void bridge$postPortalForceChangeTasks(final Entity entity, final net.minecraft.server.level.ServerLevel targetWorld,
            final boolean isVanilla) {
        this.shadow$removeAfterChangingDimensions();
        this.level.getProfiler().pop();
        ((net.minecraft.server.level.ServerLevel) this.level).resetEmptyTime();
        targetWorld.resetEmptyTime();
        this.level.getProfiler().pop();
    }

    /**
     * This is effectively an overwrite of changeDimension: required due to
     * Forge changing the signature.
     *
     * @author dualspiral - 18th December 2020 - 1.16.4
     *
     * @param originalDestinationWorld The original target world
     * @param platformTeleporter performs additional teleportation logic, as required.
     * @return The {@link Entity} that is either this one, or replaces this one
     */
    @SuppressWarnings("ConstantConditions")
    @org.checkerframework.checker.nullness.qual.Nullable
    public Entity bridge$changeDimension(final net.minecraft.server.level.ServerLevel originalDestinationWorld, final PlatformTeleporter platformTeleporter) {
        // Sponge Start
        if (this.shadow$getCommandSenderWorld().isClientSide || this.removed) {
            return null;
        }

        final boolean isPlayer = ((Object) this) instanceof ServerPlayer;

        final TeleportContext contextToSwitchTo =
                EntityPhase.State.PORTAL_DIMENSION_CHANGE.createPhaseContext(PhaseTracker.getInstance()).worldChange();
        if (isPlayer) {
            contextToSwitchTo.player();
        }
        try (final TeleportContext context = contextToSwitchTo.buildAndSwitch();
                final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.pushCause(platformTeleporter.getPortalType());
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, platformTeleporter.getMovementType());

            this.impl$originalDestinationWorld = new WeakReference<>((ServerWorld) originalDestinationWorld);

            final ChangeEntityWorldEvent.Pre preChangeEvent =
                    PlatformHooks.INSTANCE.getEventHooks().callChangeEntityWorldEventPre((Entity) (Object) this, originalDestinationWorld);
            if (preChangeEvent.isCancelled()) {
                return null;
            }
            final net.minecraft.server.level.ServerLevel targetWorld = (net.minecraft.server.level.ServerLevel) preChangeEvent.destinationWorld();
            final Vector3d currentPosition = VecHelper.toVector3d(this.shadow$position());

            // If a player, set the fact they are changing dimensions
            this.bridge$setPlayerChangingDimensions();

            final net.minecraft.server.level.ServerLevel serverworld = (net.minecraft.server.level.ServerLevel) this.level;
            final ResourceKey<Level> registrykey = serverworld.dimension();
            if (isPlayer && registrykey == Level.END && targetWorld.dimension() == Level.OVERWORLD && platformTeleporter.isVanilla()) { // avoids modded dimensions
                return ((ServerPlayerBridge) this).bridge$performGameWinLogic();
            } else {
                // Sponge Start: Redirect the find portal call to the teleporter.

                // If this is vanilla, this will house our Reposition Event and return an appropriate
                // portal info
                final PortalInfo portalinfo = platformTeleporter.getPortalInfo((Entity) (Object) this, serverworld, targetWorld, currentPosition);
                // Sponge End
                if (portalinfo != null) {
                    // Only start teleporting if we have somewhere to go.
                    this.bridge$playerPrepareForPortalTeleport(serverworld, targetWorld);
                    try {
                        // Sponge Start: wrap the teleportation logic within a function to allow for modification
                        // of the teleporter
                        final Vector3d originalDestination = new Vector3d(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z);
                        final Entity transportedEntity =
                                platformTeleporter.performTeleport((Entity) (Object) this, serverworld, targetWorld, this.xRot,
                                        createEndPlatform -> this
                                                .bridge$portalRepositioning(createEndPlatform, serverworld, targetWorld, portalinfo));
                        // Make sure the right object was returned
                        this.bridge$validateEntityAfterTeleport(transportedEntity, platformTeleporter);

                        // If we need to reposition: well... reposition.
                        // Downside: portals won't come with us, but with how it's implemented in Forge,
                        // not sure how we'd do this.
                        //
                        // If this is vanilla, we've already fired and dealt with the event
                        if (transportedEntity != null && this.impl$shouldFireRepositionEvent) {
                            final Vector3d destination = VecHelper.toVector3d(this.shadow$position());
                            final ChangeEntityWorldEvent.Reposition reposition = SpongeEventFactory.createChangeEntityWorldEventReposition(
                                    PhaseTracker.getCauseStackManager().currentCause(),
                                    (org.spongepowered.api.entity.Entity) transportedEntity,
                                    (org.spongepowered.api.world.server.ServerWorld) serverworld,
                                    currentPosition,
                                    destination,
                                    (org.spongepowered.api.world.server.ServerWorld) originalDestinationWorld,
                                    originalDestination,
                                    (org.spongepowered.api.world.server.ServerWorld) targetWorld
                            );
                            final Vector3d finalPosition;
                            if (reposition.isCancelled()) {
                                // send them back to the original destination
                                finalPosition = originalDestination;
                            } else if (reposition.destinationPosition() != destination) {
                                finalPosition = reposition.destinationPosition();
                            } else {
                                finalPosition = null;
                            }

                            if (finalPosition != null) {
                                // TODO: Rollback captures during phase - anything generated needs to vanish here
                                // Issue chunk ticket of type Portal, even if a portal isn't being created here.
                                final BlockPos ticketPos = VecHelper.toBlockPos(finalPosition);
                                targetWorld.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(ticketPos), 3, ticketPos);

                                this.shadow$absMoveTo(finalPosition.x(), finalPosition.y(), finalPosition.z());
                            }
                        }

                        this.bridge$postPortalForceChangeTasks(transportedEntity, targetWorld, platformTeleporter.getPortalType() instanceof NetherPortalType);
                        // Call post event
                        PlatformHooks.INSTANCE.getEventHooks().callChangeEntityWorldEventPost((Entity) (Object) this, serverworld, targetWorld);
                    } catch (final RuntimeException e) {
                        // nothing throws a checked exception in this block, but we want to catch unchecked stuff and try to recover
                        // just in case a mod does something less than clever.
                        if ((Object) this instanceof ServerPlayer) {
                            this.bridge$postPortalForceChangeTasks((Entity) (Object) this, (net.minecraft.server.level.ServerLevel) this.level, false);
                        }
                        throw e;
                    }
                    // Sponge End
                } else {
                    // Didn't work out.
                    return null;
                }
            }

            return (Entity) (Object) this;
        } finally {
            // Reset for the next attempt.
            this.impl$shouldFireRepositionEvent = true;
            this.impl$originalDestinationWorld = null;
        }
    }

    /**
     * This is from Entity#findDimensionEntryPoint, for determning the destination position before
     * a portal is created (lambda in the return statement after getExitPortal)
     *
     * This is only fired if a portal exists, thus the blockstate checks are okay.
     */
    private Vector3d impl$getEntityPositionInPotentialExitPortal(final BlockUtil.FoundRectangle result) {
        final BlockState blockstate = this.level.getBlockState(this.portalEntrancePos);
        final Direction.Axis direction$axis;
        final net.minecraft.world.phys.Vec3 vector3d;
        if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            direction$axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            final BlockUtil.FoundRectangle teleportationrepositioner$result = BlockUtil.getLargestRectangleAround(this.portalEntrancePos, direction$axis, 21, Direction.Axis.Y, 21, (p_242276_2_) -> {
                return this.level.getBlockState(p_242276_2_) == blockstate;
            });
            vector3d = this.shadow$getRelativePortalPosition(direction$axis, teleportationrepositioner$result);
        } else {
            vector3d = new net.minecraft.world.phys.Vec3(0.5D, 0.0D, 0.0D);
        }
        return VecHelper.toVector3d(vector3d);
    }

    @Inject(method = "getExitPortal", cancellable = true, at = @At("RETURN"))
    private void impl$fireRepositionEventWhenFindingAPortal(final net.minecraft.server.level.ServerLevel targetWorld,
            final BlockPos targetPosition,
            final boolean targetIsNether,
            final CallbackInfoReturnable<Optional<BlockUtil.FoundRectangle>> cir) {
        if (this.impl$shouldFireRepositionEvent) {
            // This exists as we're injecting at return
            final Optional<BlockUtil.FoundRectangle> result = cir.getReturnValue();
            final Vector3d destinationPosition = result.map(this::impl$getEntityPositionInPotentialExitPortal)
                    .orElseGet(() -> VecHelper.toVector3d(targetPosition));
            final ServerWorld originalDestinationWorld;
            if (this.impl$originalDestinationWorld != null && this.impl$originalDestinationWorld.get() != null) {
                originalDestinationWorld = this.impl$originalDestinationWorld.get();
            } else {
                originalDestinationWorld = (ServerWorld) targetWorld;
            }

            final ChangeEntityWorldEvent.Reposition reposition = this.bridge$fireRepositionEvent(
                    originalDestinationWorld,
                    (ServerWorld) targetWorld,
                    destinationPosition
            );
            if (!reposition.isCancelled() && reposition.destinationPosition() != destinationPosition) {
                this.impl$dontCreateExitPortal = true;
                // Something changed so we want to re-rerun this loop.
                // TODO: There is an open question here about whether we want to force the creation of a portal in this
                //  scenario, or whether we're happy if the repositioning will put someone in a nearby portal.
                cir.setReturnValue(this.shadow$getExitPortal(targetWorld, VecHelper.toBlockPos(reposition.destinationPosition()), targetIsNether));
            }
        }
    }

    @Override
    public final ChangeEntityWorldEvent.Reposition bridge$fireRepositionEvent(final ServerWorld originalDestinationWorld,
            final ServerWorld targetWorld,
            final Vector3d destinationPosition) {

        this.impl$shouldFireRepositionEvent = false;
        final ChangeEntityWorldEvent.Reposition reposition = SpongeEventFactory.createChangeEntityWorldEventReposition(
                PhaseTracker.getCauseStackManager().currentCause(),
                (org.spongepowered.api.entity.Entity) this,
                (ServerWorld) this.level,
                VecHelper.toVector3d(this.position),
                destinationPosition,
                originalDestinationWorld,
                destinationPosition,
                targetWorld
        );

        SpongeCommon.postEvent(reposition);
        return reposition;
    }

    @Override
    public CompoundTag data$getCompound() {
        return this.impl$customDataCompound;
    }

    @Override
    public void data$setCompound(final CompoundTag nbt) {
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
    public final void teleportToWithTicket(final double x, final double y, final double z) {
        if (this.level instanceof net.minecraft.server.level.ServerLevel) {
            // Sponge start
            final PhaseTracker server = PhaseTracker.SERVER;
            final Vector3d destinationPosition;
            boolean hasMovementContext = true;
            if (ShouldFire.MOVE_ENTITY_EVENT) {
                if (!server.currentContext().containsKey(EventContextKeys.MOVEMENT_TYPE)) {
                    hasMovementContext = false;
                    server.pushCause(SpongeCommon.getActivePlugin());
                    server.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);
                }

                final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(server.currentCause(),
                        (org.spongepowered.api.entity.Entity) this, VecHelper.toVector3d(this.shadow$position()), new Vector3d(x, y, z),
                        new Vector3d(x, y, z));

                if (!hasMovementContext) {
                    server.popCause();
                    server.removeContext(EventContextKeys.MOVEMENT_TYPE);
                }

                if (SpongeCommon.postEvent(event)) {
                    return;
                }

                destinationPosition = event.destinationPosition();
            } else {
                destinationPosition = new Vector3d(x, y, z);
            }
            // Sponge end
            final ChunkPos chunkpos = new ChunkPos(new BlockPos(destinationPosition.x(), destinationPosition.y(), destinationPosition.z()));
            ((net.minecraft.server.level.ServerLevel)this.level).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 0,
                    this.shadow$getId());
            this.level.getChunk(chunkpos.x, chunkpos.z);
            this.shadow$teleportTo(destinationPosition.x(), destinationPosition.y(), destinationPosition.z());
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;vehicle:Lnet/minecraft/world/entity/Entity;",
            ordinal = 0
        ),
        cancellable = true
    )
    private void impl$onStartRiding(final Entity vehicle, final boolean force,
        final CallbackInfoReturnable<Boolean> ci) {
        if (!this.level.isClientSide && ShouldFire.RIDE_ENTITY_EVENT_MOUNT) {
            PhaseTracker.getCauseStackManager().pushCause(this);
            if (SpongeCommon.postEvent(SpongeEventFactory.createRideEntityEventMount(PhaseTracker.getCauseStackManager().currentCause(), (org.spongepowered.api.entity.Entity) vehicle))) {
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
        if (this.shadow$getVehicle() != null) {
            if (this.shadow$getVehicle().removed) {
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
        if (!this.world.isClientSide && !SpongeHooks.checkEntitySpeed(((Entity) (Object) this), vec3d.x(), vec3d.y(), vec3d.z())) {
            ci.cancel();
        }
    }
*/
    @Redirect(method = "lavaHurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        )
    )
    private boolean impl$createLavaBlockDamageSource(final Entity entity, final DamageSource source, final float damage) {
        if (this.level.isClientSide) { // Short circuit
            return entity.hurt(source, damage);
        }
        try {
            final AABB bb = this.shadow$getBoundingBox().inflate(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D);
            final ServerLocation location = DamageEventHandler.findFirstMatchingBlock((Entity) (Object) this, bb, block ->
                block.getMaterial() == Material.LAVA);
            final MinecraftBlockDamageSource lava = new MinecraftBlockDamageSource("lava", location);
            ((DamageSourceBridge) (Object) lava).bridge$setLava(); // Bridge to bypass issue with using accessor mixins within mixins
            return entity.hurt(DamageSource.LAVA, damage);
        } finally {
            // Since "source" is already the DamageSource.LAVA object, we can simply re-use it here.
            ((DamageSourceBridge) source).bridge$setLava();
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
        if (this.impl$pendingVisibilityUpdate && !this.world.isClientSide) {
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
                    for (final ServerPlayerEntity entityPlayerMP : SpongeCommon.getServer().getPlayerList().players()) {
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

/*
    @Override
    public void bridge$setImplVelocity(final Vector3d velocity) {
        this.motion = VecHelper.toVec3d(velocity);
        this.velocityChanged = true;
    }

    */

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 impl$onMoveCollide(final Entity entity, final Vec3 originalMove) {
        final Vec3 afterCollide = this.shadow$collide(originalMove);
        if (ShouldFire.COLLIDE_BLOCK_EVENT_MOVE && !originalMove.equals(afterCollide)) {
            // We had a collision! Try to find the colliding block
            // TODO this is not 100% accurate as the collision happens with the bb potentially colliding with multiple blocks
            // TODO maybe actually check for blocks in bb?
            BlockPos pos = new BlockPos(this.position.add(originalMove));
            if (this.blockPosition.equals(pos)) {
                // retry with bigger move for entities with big bounding box - e.g. minecart
                pos = new BlockPos(this.position.add(originalMove.normalize()));
            }
            final BlockState state = this.level.getBlockState(pos);
            final org.spongepowered.api.util.Direction dir = org.spongepowered.api.util.Direction.closest(new Vector3d(originalMove.x, originalMove.y, originalMove.z));
            if (SpongeCommonEventFactory.handleCollideBlockEvent(state.getBlock(), this.level, pos, state,
                    (Entity) (Object) this, dir, SpongeCommonEventFactory.CollisionType.MOVE)) {
                return originalMove;
            }
        }
        return afterCollide;
    }

    @Redirect(method = "checkFallDamage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;F)V"))
    private void impl$onFallOnCollide(final Block block, final Level world, final BlockPos pos, final Entity entity, final float fallDistance) {
        if (!ShouldFire.COLLIDE_BLOCK_EVENT_FALL || world.isClientSide) {
            block.fallOn(world, pos, entity, fallDistance);
            return;
        }

        final BlockState state = world.getBlockState(pos);
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, org.spongepowered.api.util.Direction.UP, SpongeCommonEventFactory.CollisionType.FALL)) {
            block.fallOn(world, pos, entity, fallDistance);
            this.impl$lastCollidedBlockPos = pos;
        }
    }

    @Redirect(
            method = "move",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V"
            )
    )
    private void impl$onStepOnCollide(final Block block, final Level world, final BlockPos pos, final Entity entity) {
        if (!ShouldFire.COLLIDE_BLOCK_EVENT_STEP_ON || world.isClientSide) {
            block.stepOn(world, pos, entity);
            return;
        }

        final BlockState state = world.getBlockState(pos);

        final org.spongepowered.api.util.Direction dir = org.spongepowered.api.util.Direction.NONE;
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, dir, SpongeCommonEventFactory.CollisionType.STEP_ON)) {
            block.stepOn(world, pos, entity);
            this.impl$lastCollidedBlockPos = pos;
        }

    }

    @Redirect(method = "checkInsideBlocks",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;entityInside(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V"
            )
    ) // doBlockCollisions
    private void impl$onCheckInsideBlocksCollide(final BlockState blockState, final Level worldIn, final BlockPos pos, final Entity entityIn) {
        if (!ShouldFire.COLLIDE_BLOCK_EVENT_INSIDE || worldIn.isClientSide) {
            blockState.entityInside(worldIn, pos, entityIn);
            return;
        }

        final org.spongepowered.api.util.Direction dir = org.spongepowered.api.util.Direction.NONE;
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(blockState.getBlock(), worldIn, pos, blockState, entityIn, dir, SpongeCommonEventFactory.CollisionType.INSIDE)) {
            blockState.entityInside(worldIn, pos, entityIn);
            this.impl$lastCollidedBlockPos = pos;
        }

    }

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
        if (this.world.isClientSide) {
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
*/
    @Nullable
    @Override
    public BlockPos bridge$getLastCollidedBlockPos() {
        return this.impl$lastCollidedBlockPos;
    }


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

                frame.pushCause(((org.spongepowered.api.entity.Entity) this).location().world());
                final IgniteEntityEvent event = SpongeEventFactory.
                    createIgniteEntityEvent(frame.getCurrentCause(), ticks, ticks, (org.spongepowered.api.entity.Entity) this);

                if (SpongeCommon.postEvent(event)) {
                    this.fire = 0;
                    return; // set fire ticks to 0
                }
                this.fire = event.fireTicks();
            }
        }
    }

    */

    @Redirect(method = "getEncodeId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
    private boolean impl$respectTransientFlag(final EntityType entityType) {
        if (!entityType.canSerialize()) {
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
        if (!this.world.isClientSide) {
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

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void impl$WriteSpongeDataToCompound(final CompoundTag compound, final CallbackInfoReturnable<CompoundTag> ci) {
        if (DataUtil.syncDataToTag(this)) {
            compound.merge(this.data$getCompound());
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void impl$ReadSpongeDataFromCompound(final CompoundTag compound, final CallbackInfo ci) {
        // TODO If we are in Forge data is already present
        this.data$setCompound(compound); // For vanilla we set the incoming nbt
        // Deserialize custom data...
        DataUtil.syncTagToData(this);
        this.data$setCompound(null); // done reading
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
