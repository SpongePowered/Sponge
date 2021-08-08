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

import net.minecraft.BlockUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.DismountType;
import org.spongepowered.api.event.cause.entity.DismountTypes;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalTypes;
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
import org.spongepowered.common.bridge.commands.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.world.damagesource.DamageSourceBridge;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.bridge.world.level.PlatformServerLevelBridge;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.data.value.ImmutableSpongeValue;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.TeleportContext;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DamageEventUtil;
import org.spongepowered.common.util.MinecraftBlockDamageSource;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.NetherPortalType;
import org.spongepowered.common.world.portal.PortalLogic;
import org.spongepowered.common.world.portal.SpongePortalInfo;
import org.spongepowered.common.world.portal.VanillaPortal;
import org.spongepowered.math.vector.Vector3d;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityBridge, PlatformEntityBridge, VanishableBridge, CommandSourceProviderBridge, DataCompoundHolder {

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
    @Shadow private int remainingFireTicks;

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
    @Shadow public abstract void shadow$teleportToWithTicket(double x, double y, double z);
    @Shadow public abstract void shadow$teleportTo(double x, double y, double z);
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
    @Shadow protected abstract boolean shadow$fireImmune();
    @Shadow @Nullable protected abstract PortalInfo shadow$findDimensionEntryPoint(ServerLevel param0);
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
    private boolean impl$customPortal = false;
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
    public boolean bridge$setPosition(final Vector3d position) {
        if (this.removed) {
            return false;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);
            final Vector3d destinationPosition = this.impl$fireMoveEvent(PhaseTracker.SERVER, position);
            if (destinationPosition == null) {
                return false;
            }
            final ServerLevel level = (ServerLevel) this.level;
            return this.impl$setLocation(false, level, level, destinationPosition);
        }
    }

    @Override
    public boolean bridge$setLocation(final ServerLocation location) {
        if (this.removed || ((LevelBridge) location.world()).bridge$isFake()) {
            return false;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(SpongeCommon.activePlugin());
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);

            final net.minecraft.server.level.ServerLevel originalWorld = (ServerLevel) this.shadow$getCommandSenderWorld();
            final net.minecraft.server.level.ServerLevel originalDestinationWorld = (net.minecraft.server.level.ServerLevel) location.world();
            final net.minecraft.server.level.ServerLevel destinationWorld;
            final @org.checkerframework.checker.nullness.qual.Nullable Vector3d destinationPosition;

            final boolean isChangeOfWorld = this.shadow$getCommandSenderWorld() != originalDestinationWorld;
            if (isChangeOfWorld) {
                final ChangeEntityWorldEvent.Pre event = PlatformHooks.INSTANCE.getEventHooks()
                        .callChangeEntityWorldEventPre((Entity) (Object) this, originalDestinationWorld);
                if (event.isCancelled() || ((LevelBridge) event.destinationWorld()).bridge$isFake()) {
                    return false;
                }

                destinationWorld = (ServerLevel) event.destinationWorld();
                final ChangeEntityWorldEvent.Reposition repositionEvent =
                        this.bridge$fireRepositionEvent(event.originalDestinationWorld(), event.destinationWorld(), location.position());
                if (repositionEvent.isCancelled()) {
                    return false;
                }
                destinationPosition = repositionEvent.destinationPosition();
            } else {
                destinationWorld = (ServerLevel) this.level;
                destinationPosition = this.impl$fireMoveEvent(PhaseTracker.SERVER, location.position());
                if (destinationPosition == null) {
                    return false;
                }
            }

            final boolean completed = this.impl$setLocation(isChangeOfWorld, originalDestinationWorld, destinationWorld, destinationPosition);
            if (isChangeOfWorld) {
                Sponge.eventManager().post(SpongeEventFactory.createChangeEntityWorldEventPost(
                        PhaseTracker.getCauseStackManager().currentCause(),
                        (org.spongepowered.api.entity.Entity) this,
                        (ServerWorld) originalWorld,
                        (ServerWorld) originalDestinationWorld,
                        (ServerWorld) destinationWorld
                ));
            }
            return completed;
        }
    }

    protected boolean impl$setLocation(final boolean isChangeOfWorld, final ServerLevel originalDestinationWorld, final ServerLevel destinationWorld,
            final Vector3d destinationPosition) {
        ((Entity) (Object) this).unRide();
        if (isChangeOfWorld) {
            final net.minecraft.server.level.ServerLevel originalWorld = (net.minecraft.server.level.ServerLevel) this.shadow$getCommandSenderWorld();
            ((PlatformServerLevelBridge) this.shadow$getCommandSenderWorld()).bridge$removeEntity((Entity) (Object) this, true);
            this.bridge$revive();
            this.shadow$setLevel(destinationWorld);
            destinationWorld.addFromAnotherDimension((Entity) (Object) this);

            originalWorld.resetEmptyTime();
            destinationWorld.resetEmptyTime();
        }

        return this.impl$teleportToWithTicket(destinationPosition.x(), destinationPosition.y(), destinationPosition.z(), false);
    }

    @Override
    public boolean bridge$dismountRidingEntity(final DismountType type) {
        if (!this.level.isClientSide && ShouldFire.RIDE_ENTITY_EVENT_DISMOUNT) {
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                frame.addContext(EventContextKeys.DISMOUNT_TYPE, type);
                if (SpongeCommon.post(SpongeEventFactory.
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

    @Redirect(method = "findDimensionEntryPoint",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
                    target = "Lnet/minecraft/world/level/Level;END:Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<Level> impl$getNullInsteadOfEndIfCreatingCustomPortal() {
        if (this.impl$customPortal) {
            // This will cause the first two conditions to be false, meaning that the
            // standard portal checks will be disabled an a nether portal can go
            // in any dimension
            return null;
        }
        return Level.END;
    }

    @Redirect(method = "findDimensionEntryPoint",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
                    target = "Lnet/minecraft/world/level/Level;NETHER:Lnet/minecraft/resources/ResourceKey;"))
    private ResourceKey<Level> impl$forceCheckToBeTrueIfCreatingCustomPortal(final ServerLevel targetDimension) {
        if (this.impl$customPortal) {
            // This will cause "var4" to be true in the second if check,
            // meaning that the portal finding logic will always fire
            //
            // This also has the side effect of setting the other Level.NETHER
            // access too, but that's okay as long as var4 is true.
            return targetDimension.dimension();
        }
        return Level.NETHER;
    }

    @Redirect(method = "findDimensionEntryPoint", at = @At(value = "NEW", target = "net/minecraft/world/level/portal/PortalInfo"))
    private PortalInfo impl$addPortalToPortalInfoForEnd(final Vec3 var1, final Vec3 var2, final float var3, final float var4, final ServerLevel serverLevel) {
        final Portal portal = new VanillaPortal(PortalTypes.END.get(), ((ServerWorld) serverLevel).location(VecHelper.toVector3d(var1)), null);
        return new SpongePortalInfo(var1, var2, var3, var4, portal);
    }

    /*
     * Used in bridge$changeDimension
     */
    protected Entity impl$portalRepositioning(final boolean createEndPlatform,
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

    /*
     * Used in bridge$changeDimension
     */
    protected void impl$postPortalForceChangeTasks(final Entity entity, final net.minecraft.server.level.ServerLevel targetWorld,
            final boolean isVanilla) {
        this.shadow$removeAfterChangingDimensions();
        this.level.getProfiler().pop();
        ((net.minecraft.server.level.ServerLevel) this.level).resetEmptyTime();
        targetWorld.resetEmptyTime();
        this.level.getProfiler().pop();
    }

    /*
     * Used in classes that add to the changeDimension behaviour
     */
    protected @org.checkerframework.checker.nullness.qual.Nullable Entity impl$postProcessChangeDimension(final Entity entity) {
        return entity;
    }

    /**
     * This is effectively an overwrite of changeDimension: required due to
     * Forge changing the signature.
     *
     * @author dualspiral - 18th December 2020 - 1.16.4
     * @author dualspiral - 8th August 2021 - 1.16.5 (adjusted for SpongeForge)
     *
     * @param originalDestinationWorld The original target world
     * @param originalPortalLogic performs additional teleportation logic, as required.
     * @return The {@link Entity} that is either this one, or replaces this one
     */
    @SuppressWarnings("ConstantConditions")
    @org.checkerframework.checker.nullness.qual.Nullable
    public Entity bridge$changeDimension(final net.minecraft.server.level.ServerLevel originalDestinationWorld, final PortalLogic originalPortalLogic) {
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
            frame.pushCause(originalPortalLogic.getPortalType());
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, originalPortalLogic.getMovementType());

            this.impl$originalDestinationWorld = new WeakReference<>((ServerWorld) originalDestinationWorld);

            final ChangeEntityWorldEvent.Pre preChangeEvent =
                    PlatformHooks.INSTANCE.getEventHooks().callChangeEntityWorldEventPre((Entity) (Object) this, originalDestinationWorld);
            if (preChangeEvent.isCancelled()) {
                this.impl$onPreWorldChangeCanceled();
                return null;
            }
            this.impl$customPortal = preChangeEvent.originalDestinationWorld() != preChangeEvent.destinationWorld();
            final PortalLogic finalPortalLogic;
            if (this.impl$customPortal && originalPortalLogic == originalDestinationWorld.getPortalForcer()) {
                finalPortalLogic = (PortalLogic) ((ServerLevel) preChangeEvent.destinationWorld()).getPortalForcer();
            } else {
                finalPortalLogic = originalPortalLogic;
            }
            final net.minecraft.server.level.ServerLevel targetWorld = (net.minecraft.server.level.ServerLevel) preChangeEvent.destinationWorld();
            final Vector3d currentPosition = VecHelper.toVector3d(this.shadow$position());

            // If a player, set the fact they are changing dimensions
            this.impl$onChangingDimension(targetWorld);

            final net.minecraft.server.level.ServerLevel serverworld = (net.minecraft.server.level.ServerLevel) this.level;
            final ResourceKey<Level> registrykey = serverworld.dimension();
            if (isPlayer && registrykey == Level.END && targetWorld.dimension() == Level.OVERWORLD && finalPortalLogic.isVanilla()) { // avoids modded dimensions
                return this.impl$postProcessChangeDimension(this.impl$performGameWinLogic());
            } else {
                // Sponge Start: Redirect the find portal call to the teleporter.

                // If this is vanilla, this will house our Reposition Event and return an appropriate
                // portal info
                final PortalInfo portalinfo = originalPortalLogic.getPortalInfo((Entity) (Object) this, targetWorld,
                        x -> this.shadow$findDimensionEntryPoint(x)); // don't make this a method reference, it'll crash vanilla.
                // Sponge End
                if (portalinfo != null) {
                    if (portalinfo instanceof SpongePortalInfo) {
                        frame.addContext(EventContextKeys.PORTAL, ((SpongePortalInfo) portalinfo).portal());
                    }
                    // Only start teleporting if we have somewhere to go.
                    this.impl$prepareForPortalTeleport(serverworld, targetWorld);
                    try {
                        // Sponge Start: wrap the teleportation logic within a function to allow for modification
                        // of the teleporter
                        final Vector3d originalDestination = new Vector3d(portalinfo.pos.x, portalinfo.pos.y, portalinfo.pos.z);

                        // Note that impl$portalRepositioning is the lambda. As this will be different in ServerPlayer,
                        // we transfer it to a method instead so we can override it.
                        final Entity transportedEntity =
                                originalPortalLogic.placeEntity((Entity) (Object) this, serverworld, targetWorld, this.yRot,
                                        createEndPlatform ->
                                                this.impl$portalRepositioning(createEndPlatform, serverworld, targetWorld, portalinfo));
                        // Make sure the right object was returned
                        this.impl$validateEntityAfterTeleport(transportedEntity, originalPortalLogic);

                        // If we need to reposition: well... reposition.
                        // Downside: portals won't come with us, but with how it's implemented in Forge,
                        // not sure how we'd do this.
                        //
                        // If this is vanilla, we've already fired and dealt with the event
                        final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
                        if (transportedEntity != null && this.impl$shouldFireRepositionEvent) {
                            final Vector3d destination = VecHelper.toVector3d(this.shadow$position());
                            final ChangeEntityWorldEvent.Reposition reposition = SpongeEventFactory.createChangeEntityWorldEventReposition(
                                    cause,
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

                        // Used to perform player specific tasks.
                        this.impl$postPortalForceChangeTasks(transportedEntity, targetWorld, originalPortalLogic.getPortalType() instanceof NetherPortalType);
                        // Call post event
                        Sponge.eventManager().post(
                                SpongeEventFactory.createChangeEntityWorldEventPost(
                                        cause,
                                        (org.spongepowered.api.entity.Entity) this,
                                        (ServerWorld) serverworld,
                                        (ServerWorld) originalDestinationWorld,
                                        (ServerWorld) targetWorld
                                )
                        );
                    } catch (final RuntimeException e) {
                        // nothing throws a checked exception in this block, but we want to catch unchecked stuff and try to recover
                        // just in case a mod does something less than clever.
                        if ((Object) this instanceof ServerPlayer) {
                            this.impl$postPortalForceChangeTasks((Entity) (Object) this, (net.minecraft.server.level.ServerLevel) this.level, false);
                        }
                        throw e;
                    }
                    // Sponge End
                } else {
                    // Didn't work out.
                    return null;
                }
            }

            return this.impl$postProcessChangeDimension((Entity) (Object) this);
        } finally {
            // Reset for the next attempt.
            this.impl$shouldFireRepositionEvent = true;
            this.impl$originalDestinationWorld = null;
            this.impl$customPortal = false;
        }
    }

    protected void impl$onPreWorldChangeCanceled() {
        // intentional no-op
    }

    protected void impl$onChangingDimension(final ServerLevel target) {
        // intentional no-op
    }

    protected void impl$prepareForPortalTeleport(final ServerLevel currentWorld, final ServerLevel targetWorld) {
        // intentional no-op
    }

    protected void impl$validateEntityAfterTeleport(final Entity e, final PortalLogic teleporter) {
        // intentional no-op
    }

    protected Entity impl$performGameWinLogic() {
        return (Entity) (Object) this;
    }

    /**
     * This is from Entity#findDimensionEntryPoint, for determining the destination position before
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
                // Something changed so we want to re-rerun this loop.
                // TODO: There is an open question here about whether we want to force the creation of a portal in this
                //  scenario, or whether we're happy if the repositioning will put someone in a nearby portal.
                cir.setReturnValue(this.shadow$getExitPortal(targetWorld, VecHelper.toBlockPos(reposition.destinationPosition()), targetIsNether));
                this.impl$dontCreateExitPortal = true;
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

        SpongeCommon.post(reposition);
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
     *         useful information. We redirect to impl$teleportToWithTicket to
     *         be able to return whether or not a teleport happened.
     */
    @Overwrite
    public final void teleportToWithTicket(final double x, final double y, final double z) {
        this.impl$teleportToWithTicket(x, y, z, true);
    }

    /*
     * (non-Javadoc)
     *
     * This is a modified version of teleportToWithTicket, treat this as an overwrite
     * see above.
     */
    public final boolean impl$teleportToWithTicket(final double x, final double y, final double z, final boolean fireMoveEvent) {
        if (this.level instanceof net.minecraft.server.level.ServerLevel) {
            // Sponge start
            final Vector3d destinationPosition;
            if (ShouldFire.MOVE_ENTITY_EVENT && fireMoveEvent) {
                destinationPosition = this.impl$fireMoveEvent(PhaseTracker.SERVER, new Vector3d(x, y, z));
            } else {
                destinationPosition = new Vector3d(x, y, z);
            }
            // Sponge end
            final ChunkPos chunkpos = new ChunkPos(new BlockPos(destinationPosition.x(), destinationPosition.y(), destinationPosition.z()));
            ((net.minecraft.server.level.ServerLevel)this.level).getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 0,
                    this.shadow$getId());
            this.level.getChunk(chunkpos.x, chunkpos.z);
            this.shadow$teleportTo(destinationPosition.x(), destinationPosition.y(), destinationPosition.z());
            // Sponge: return success
            return true;
        }
        // Sponge: return failure
        return false;
    }

    protected final @org.checkerframework.checker.nullness.qual.Nullable Vector3d impl$fireMoveEvent(
            final PhaseTracker phaseTracker, final Vector3d originalDestinationPosition) {
        final boolean hasMovementContext = phaseTracker.currentContext().containsKey(EventContextKeys.MOVEMENT_TYPE);
        if (!hasMovementContext) {
            phaseTracker.pushCause(SpongeCommon.activePlugin());
            phaseTracker.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);
        }

        final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(phaseTracker.currentCause(),
                (org.spongepowered.api.entity.Entity) this, VecHelper.toVector3d(this.shadow$position()),
                originalDestinationPosition,
                originalDestinationPosition);

        if (!hasMovementContext) {
            phaseTracker.popCause();
            phaseTracker.removeContext(EventContextKeys.MOVEMENT_TYPE);
        }

        if (SpongeCommon.post(event)) {
            return null;
        }

        return event.destinationPosition();
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
            if (SpongeCommon.post(SpongeEventFactory.createRideEntityEventMount(PhaseTracker.getCauseStackManager().currentCause(), (org.spongepowered.api.entity.Entity) vehicle))) {
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
            final ServerLocation location = DamageEventUtil.findFirstMatchingBlock((Entity) (Object) this, bb, block ->
                block.getMaterial() == Material.LAVA);
            final DamageSource lava = MinecraftBlockDamageSource.ofFire("lava", location, false);
            ((DamageSourceBridge) lava).bridge$setLava(); // Bridge to bypass issue with using accessor mixins within mixins
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


    @Redirect(method = "setRemainingFireTicks",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;remainingFireTicks:I",
            opcode = Opcodes.PUTFIELD)
    )
    private void impl$ThrowIgniteEventForFire(final Entity entity, final int ticks) {
        if (!((LevelBridge) this.level).bridge$isFake() && ShouldFire.IGNITE_ENTITY_EVENT &&
            this.remainingFireTicks < 1 && ticks >= Constants.Entity.MINIMUM_FIRE_TICKS &&
            this.impl$canCallIgniteEntityEvent()) {

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {

                frame.pushCause(((org.spongepowered.api.entity.Entity) this).location().world());
                final IgniteEntityEvent event = SpongeEventFactory.
                    createIgniteEntityEvent(frame.currentCause(), ticks, ticks, (org.spongepowered.api.entity.Entity) this);

                if (SpongeCommon.post(event)) {
                    // Don't do anything
                    return;
                }
                final DataTransactionResult transaction = DataTransactionResult.builder()
                    .replace(new ImmutableSpongeValue<>(Keys.FIRE_TICKS, Ticks.of(Math.max(this.remainingFireTicks, 0))))
                    .success(new ImmutableSpongeValue<>(Keys.FIRE_TICKS, Ticks.of(event.fireTicks())))
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();

                final ChangeDataHolderEvent.ValueChange valueChange = SpongeEventFactory.createChangeDataHolderEventValueChange(
                    PhaseTracker.SERVER.currentCause(),
                    transaction,
                    (DataHolder.Mutable) this);

                Sponge.eventManager().post(valueChange);
                if (valueChange.isCancelled()) {
                    //If the event is cancelled, well, don't change the underlying value.
                    return;
                }
                this.remainingFireTicks = valueChange.endResult().successfulValue(Keys.FIRE_TICKS)
                    .map(Value::get)
                    .map(t -> (int) t.ticks())
                    .orElse(0);
            }
            return;
        }
        this.remainingFireTicks = ticks; // Vanilla functionality
    }


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
     * Overridden method for Players to determine whether this entity is not immune to
     * fire such that {@link IgniteEntityEvent}s are not needed to be thrown as they
     * cannot take fire damage, nor do they light on fire.
     *
     * @return True if this entity is not immune to fire.
     */
    protected boolean impl$canCallIgniteEntityEvent() {
        return !this.shadow$fireImmune();
    }

}
