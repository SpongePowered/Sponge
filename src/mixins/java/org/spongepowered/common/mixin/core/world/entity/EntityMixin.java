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

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.PortalProcessor;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LavaCauldronBlock;
import net.minecraft.world.level.block.Portal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.effect.VanishState;
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
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.server.level.ChunkMapAccessor;
import org.spongepowered.common.accessor.server.level.ChunkMap_TrackedEntityAccessor;
import org.spongepowered.common.accessor.world.entity.PortalProcessorAccessor;
import org.spongepowered.common.accessor.world.level.storage.TagValueInputAccessor;
import org.spongepowered.common.bridge.commands.CommandSourceProviderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.data.TransientBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.entity.PortalProcessorBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.data.DataUtil;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.data.value.ImmutableSpongeValue;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.entity.TeleportContext;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DamageEventUtil;
import org.spongepowered.common.util.ReflectionUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.*;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityBridge, PlatformEntityBridge, VanishableBridge, CommandSourceProviderBridge, DataCompoundHolder, TransientBridge {

    // @formatter:off

    @Shadow public abstract Level shadow$level();
    @Shadow public int invulnerableTime;
    @Shadow @Final protected RandomSource random;
    @Shadow @Final protected SynchedEntityData entityData;
    @Shadow public float yRotO;
    @Shadow @Nullable private Entity vehicle;
    @Shadow private ImmutableList<Entity> passengers;
    @Shadow private net.minecraft.world.phys.Vec3 position;
    @Shadow private int remainingFireTicks;
    @Shadow protected UUID uuid;
    @Shadow private EntityDimensions dimensions;

    @Shadow protected abstract void shadow$unsetRemoved();
    @Shadow public abstract void shadow$setRemoved(Entity.RemovalReason reason);
    @Shadow public abstract void shadow$setPos(double x, double y, double z);
    @Shadow public abstract double shadow$getX();
    @Shadow public abstract double shadow$getY();
    @Shadow public abstract double shadow$getZ();
    @Shadow public abstract void shadow$remove(Entity.RemovalReason reason);
    @Shadow public abstract void shadow$discard();
    @Shadow public abstract boolean shadow$isRemoved();
    @Shadow public abstract int shadow$getId();
    @Shadow public abstract void shadow$playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow public abstract boolean shadow$isInvisible();
    @Shadow public abstract void shadow$setInvisible(boolean invisible);
    @Shadow public abstract EntityType<?> shadow$getType();
    @Shadow public abstract void shadow$teleportTo(double x, double y, double z);
    @Shadow public abstract CommandSourceStack shadow$createCommandSourceStackForNameResolution(ServerLevel level);
    @Shadow public abstract net.minecraft.world.phys.Vec3 shadow$position();
    @Shadow @Nullable public abstract ItemEntity shadow$spawnAtLocation(ServerLevel $$0, ItemStack stack, float offsetY);
    @Shadow @Nullable public abstract Entity shadow$getVehicle();
    @Shadow public abstract AABB shadow$getBoundingBox();
    @Shadow @Nullable public abstract PlayerTeam shadow$getTeam();
    @Shadow public abstract SynchedEntityData shadow$getEntityData();
    @Shadow public abstract net.minecraft.world.phys.Vec3 shadow$getDeltaMovement();
    @Shadow public abstract void shadow$setDeltaMovement(net.minecraft.world.phys.Vec3 motion);
    @Shadow public abstract void shadow$unRide();
    @Shadow public abstract boolean shadow$teleportTo(ServerLevel level, double x, double y, double z, Set<Relative> relatives, float xRot, float yRot, boolean setCamera);
    @Shadow public abstract float shadow$getYRot();
    @Shadow public abstract float shadow$getXRot();
    @Shadow public abstract void shadow$setYRot(final float param0);
    @Shadow public abstract boolean shadow$fireImmune();
    @Shadow public abstract boolean shadow$onGround();
    @Shadow @Nullable protected abstract String shadow$getEncodeId();
    @Shadow @javax.annotation.Nullable public PortalProcessor portalProcess;
    @Shadow public abstract void shadow$stopRiding();
    @Shadow public abstract Level level();
    // @formatter:on



    private boolean impl$isConstructing = true;
    private VanishState impl$vanishState = VanishState.unvanished();
    protected boolean impl$transient = false;
    protected boolean impl$skipTeleportCrossDimensionBefore = false;
    protected boolean impl$hasCustomFireImmuneTicks = false;
    protected short impl$fireImmuneTicks = 0;
    private BlockPos impl$lastCollidedBlockPos;
    private Boolean impl$playerTouchDeclared;

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
    public boolean bridge$isPlayerTouchDeclared() {
        if (this.impl$playerTouchDeclared == null) {
            this.impl$playerTouchDeclared = ReflectionUtil.isPlayerTouchDeclared(this.getClass());
        }
        return this.impl$playerTouchDeclared;
    }

    @Override
    public boolean bridge$setPosition(final Vector3d position) {
        return this.bridge$setLocation(ServerLocation.of((ServerWorld) this.shadow$level(), position));
    }

    @Override
    public boolean bridge$setLocation(final ServerLocation location) {
        if (this.shadow$isRemoved() || ((LevelBridge) location.world()).bridge$isFake()) {
            return false;
        }

        final PhaseTracker phaseTracker = PhaseTracker.getWorldInstance((ServerLevel) this.shadow$level());
        try (final CauseStackManager.StackFrame frame = phaseTracker.pushCauseFrame()) {
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.PLUGIN);
            return this.shadow$teleportTo((ServerLevel) location.world(), location.x(), location.y(), location.z(), Relative.ROTATION, 0, 0, true);
        }
    }

    @WrapMethod(method = "teleport")
    private Entity impl$wrapTeleport(final TeleportTransition transition, Operation<Entity> original) {
        final PhaseTracker phaseTracker = PhaseTracker.getWorldInstance(this.shadow$level());
        try (final CauseStackManager.StackFrame frame = phaseTracker.pushCauseFrame()) {
            frame.pushCause(this);
            return original.call(transition);
        }
    }

    @Override
    public boolean bridge$dismountRidingEntity(final DismountType type) {
        final Entity vehicle = this.shadow$getVehicle();

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.addContext(EventContextKeys.DISMOUNT_TYPE, type);

            this.shadow$stopRiding();
        }

        return vehicle != this.shadow$getVehicle();
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
    public VanishState bridge$vanishState() {
        return this.impl$vanishState;
    }

    @Override
    public void bridge$vanishState(VanishState state) {
        final VanishState oldState = this.impl$vanishState;
        this.impl$vanishState = state;

        final ChunkMap_TrackedEntityAccessor trackerAccessor = ((ChunkMapAccessor) ((ServerWorld) this.shadow$level()).chunkManager()).accessor$entityMap().get(this.shadow$getId());
        if (trackerAccessor == null) {
            return;
        }

        if (this.bridge$vanishState().invisible()) {
            for (final ServerPlayerConnection playerConnection : trackerAccessor.accessor$seenBy().toArray(new ServerPlayerConnection[0])) {
                trackerAccessor.accessor$removePlayer(playerConnection.getPlayer());
            }

            if ((Entity) (Object) this instanceof ServerPlayer) {
                for (final ServerPlayer entityPlayerMP : SpongeCommon.server().getPlayerList().getPlayers()) {
                    if ((Object) this == entityPlayerMP) {
                        continue;
                    }
                    if (state.hidesTabListEntry() != oldState.hidesTabListEntry()) {
                        if (state.hidesTabListEntry()) {
                            entityPlayerMP.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(this.uuid)));
                        } else if (oldState.hidesTabListEntry() && (Entity) (Object) this instanceof ServerPlayer player) {
                            entityPlayerMP.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player)));
                        }
                    }
                }
            }
        } else {
            for (final ServerPlayer entityPlayerMP : SpongeCommon.server().getPlayerList().getPlayers()) {
                if ((Object) this == entityPlayerMP) {
                    continue;
                }
                if (oldState.hidesTabListEntry() && (Entity) (Object) this instanceof ServerPlayer player) {
                    entityPlayerMP.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player)));
                }
                if (this.shadow$level() == entityPlayerMP.level()) {
                    trackerAccessor.accessor$updatePlayer(entityPlayerMP);
                }
            }
        }
    }

    @Override
    public boolean bridge$isTransient() {
        return this.shadow$getEncodeId() == null;
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
        return this.shadow$createCommandSourceStackForNameResolution((ServerLevel) this.shadow$level());
    }

    @Inject(method = "setAsInsidePortal", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER,
            target = "Lnet/minecraft/world/entity/Entity;portalProcess:Lnet/minecraft/world/entity/PortalProcessor;"))
    public void impl$onCreatePortalProcessor(final Portal $$0, final BlockPos $$1, final CallbackInfo ci) {
        if (((LevelBridge) this.shadow$level()).bridge$isFake()) {
            return;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()){
            var be = this.shadow$level().getBlockEntity(this.portalProcess.getEntryPosition());
            if (be != null) {
                frame.pushCause(be);
            }
            frame.pushCause(this);
            var portal = ((PortalProcessorAccessor)this.portalProcess).accessor$portal();
            ((PortalProcessorBridge)this.portalProcess).bridge$init(this.shadow$level());
            frame.pushCause(portal);

            final int portalTransitionTime = $$0.getPortalTransitionTime((ServerLevel) this.shadow$level(), (Entity) (Object) this);
            var event = SpongeEventFactory.createInvokePortalEventEnter(frame.currentCause(),
                    (org.spongepowered.api.entity.Entity) this,
                    Optional.empty(),
                    (PortalLogic) $$0,
                    portalTransitionTime);
            if (SpongeCommon.post(event)) {
                this.portalProcess = null;
            } else {
                event.customPortalTransitionTime().ifPresent(customTime -> {
                    ((PortalProcessorBridge)this.portalProcess).bridge$setTransitionTime(customTime);
                });
            }
        }
    }

    /**
     * See {@link PortalProcessorMixin#impl$onGetPortalDestination} for portal events
     */
    @Redirect(method = "handlePortal",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;"))
    public Entity impl$onChangeDimension(final Entity instance, final TeleportTransition transition) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(this);
            var be = this.shadow$level().getBlockEntity(this.portalProcess.getEntryPosition());
            if (be != null) {
                frame.pushCause(be);
            }
            final var portal = ((PortalProcessorAccessor) this.portalProcess).accessor$portal();
            frame.pushCause(portal);
            var movementType = portal == Blocks.END_GATEWAY ? MovementTypes.END_GATEWAY : MovementTypes.PORTAL;
            frame.addContext(EventContextKeys.MOVEMENT_TYPE, movementType);
            frame.addContext(EventContextKeys.PORTAL_LOGIC, (PortalLogic) portal);
            // TODO frame.addContext(EventContextKeys.PORTAL, transition);
            // TODO 2-dim portal?
            this.impl$skipTeleportCrossDimensionBefore = true;
            return instance.teleport(transition);
        } finally {
            this.impl$skipTeleportCrossDimensionBefore = false;
        }
    }

    @ModifyVariable(method = "teleportCrossDimension", at = @At("HEAD"), argsOnly = true)
    private TeleportTransition impl$beforeTeleportCrossDimension(TeleportTransition transition, @Cancellable final CallbackInfoReturnable<Entity> cir) {
        transition = this.impl$fireTeleportCrossDimensionBefore(transition);
        if (transition == null) {
            cir.setReturnValue(null);
        }
        return transition;
    }

    @ModifyVariable(method = "teleportCrossDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPassengers()Ljava/util/List;"), argsOnly = true, ordinal = 1)
    private ServerLevel impl$useChangeWorldEventLevel(final ServerLevel originalNewLevel, @Local(argsOnly = true) final TeleportTransition transition,
                                                      @Share("original-new-level") final LocalRef<ServerLevel> originalNewLevelRef) {
        originalNewLevelRef.set(originalNewLevel);
        return transition.newLevel();
    }

    @Inject(method = "teleportCrossDimension", at = @At("RETURN"))
    private void impl$afterTeleportCrossDimension(final ServerLevel level, final ServerLevel newLevel, final TeleportTransition transition,
                                                  final CallbackInfoReturnable<Entity> cir, @Share("original-new-level") final LocalRef<ServerLevel> originalNewLevelRef) {
        final Entity newEntity = cir.getReturnValue();
        ((EntityMixin) (Object) newEntity).impl$fireTeleportCrossDimensionAfter(level, newLevel, originalNewLevelRef.get());
    }


    @ModifyVariable(method = "teleportSameDimension", at = @At("HEAD"), argsOnly = true)
    private TeleportTransition impl$beforeTeleportSameDimension(TeleportTransition transition, @Cancellable final CallbackInfoReturnable<Entity> cir) {
        transition = this.impl$fireTeleportSameDimension(transition);
        if (transition == null) {
            cir.setReturnValue(null);
        }
        return transition;
    }

    protected final @Nullable TeleportTransition impl$fireTeleportCrossDimensionBefore(TeleportTransition transition) {
        if (this.impl$skipTeleportCrossDimensionBefore) {
            return transition;
        }

        final ServerLevel originalLevel = (ServerLevel) this.shadow$level();
        final ServerLevel originalDestinationLevel = transition.newLevel();
        final PhaseTracker phaseTracker = PhaseTracker.getWorldInstance(originalLevel);
        try (final TeleportContext context = EntityPhase.State.PORTAL_DIMENSION_CHANGE.createPhaseContext(phaseTracker).worldChange()) {
            context.buildAndSwitch();

            final ChangeEntityWorldEvent.Pre preEvent = SpongeEventFactory.createChangeEntityWorldEventPre(
                phaseTracker.currentCause(),
                (org.spongepowered.api.entity.Entity) this,
                (ServerWorld) originalLevel,
                (ServerWorld) originalDestinationLevel,
                (ServerWorld) originalDestinationLevel);
            if (SpongeCommon.post(preEvent)) {
                return null;
            }

            if (preEvent.destinationWorld() != originalDestinationLevel) {
                transition = new TeleportTransition(
                    (ServerLevel) preEvent.destinationWorld(),
                    transition.position(),
                    transition.deltaMovement(),
                    transition.yRot(), transition.xRot(),
                    transition.missingRespawnBlock(),
                    transition.asPassenger(),
                    transition.relatives(),
                    transition.postTeleportTransition()
                );
            }

            final Vector3d originalDestination = this.impl$absoluteDestinationPosition(transition);
            final ChangeEntityWorldEvent.Reposition repositionEvent = SpongeEventFactory.createChangeEntityWorldEventReposition(
                phaseTracker.currentCause(),
                (org.spongepowered.api.entity.Entity) this,
                (ServerWorld) originalLevel,
                VecHelper.toVector3d(this.position),
                originalDestination,
                (ServerWorld) originalDestinationLevel,
                originalDestination,
                (ServerWorld) transition.newLevel()
            );
            transition = this.impl$fireMove(transition, repositionEvent);
            if (transition == null) {
                return null;
            }

            return this.impl$fireRotate(transition);
        }
    }

    protected final void impl$fireTeleportCrossDimensionAfter(final ServerLevel originalLevel, final ServerLevel destinationLevel, final ServerLevel originalDestinationLevel) {
        Sponge.eventManager().post(
            SpongeEventFactory.createChangeEntityWorldEventPost(
                PhaseTracker.getWorldInstance(destinationLevel).currentCause(),
                (org.spongepowered.api.entity.Entity) this,
                (ServerWorld) originalLevel,
                (ServerWorld) destinationLevel,
                (ServerWorld) originalDestinationLevel
            )
        );
    }

    protected final @Nullable TeleportTransition impl$fireTeleportSameDimension(TeleportTransition transition) {
        final PhaseTracker phaseTracker = PhaseTracker.getWorldInstance((ServerLevel) this.shadow$level());
        try (final TeleportContext context = EntityPhase.State.PORTAL_DIMENSION_CHANGE.createPhaseContext(phaseTracker)) {
            context.buildAndSwitch();

            if (ShouldFire.MOVE_ENTITY_EVENT) {
                final Vector3d originalDestination = this.impl$absoluteDestinationPosition(transition);
                final MoveEntityEvent moveEvent = SpongeEventFactory.createMoveEntityEvent(
                    phaseTracker.currentCause(),
                    (org.spongepowered.api.entity.Entity) this,
                    VecHelper.toVector3d(this.position),
                    originalDestination,
                    originalDestination
                );
                transition = this.impl$fireMove(transition, moveEvent);
                if (transition == null) {
                    return null;
                }
            }

            return this.impl$fireRotate(transition);
        }
    }

    private Vector3d impl$absoluteDestinationPosition(final TeleportTransition transition) {
        final Vec3 origin = this.position;
        final Vec3 pos = transition.position();
        final Set<Relative> relatives = transition.relatives();
        return new Vector3d(
            relatives.contains(Relative.X) ? origin.x + pos.x : pos.x,
            relatives.contains(Relative.Y) ? origin.y + pos.y : pos.y,
            relatives.contains(Relative.Z) ? origin.z + pos.z : pos.z
        );
    }

    private @Nullable TeleportTransition impl$fireMove(final TeleportTransition transition, final MoveEntityEvent moveEvent) {
        if (SpongeCommon.post(moveEvent)) {
            return null;
        }

        if (moveEvent.destinationPosition().equals(moveEvent.originalDestinationPosition())) {
            return transition;
        }

        final Set<Relative> newRelatives = EnumSet.noneOf(Relative.class);
        newRelatives.addAll(transition.relatives());
        newRelatives.removeAll(Constants.Entity.RELATIVE_POSITION);

        return new TeleportTransition(
            transition.newLevel(),
            VecHelper.toVanillaVector3d(moveEvent.destinationPosition()),
            transition.deltaMovement(),
            transition.yRot(), transition.xRot(),
            transition.missingRespawnBlock(),
            transition.asPassenger(),
            newRelatives,
            transition.postTeleportTransition()
        );
    }

    private TeleportTransition impl$fireRotate(final TeleportTransition transition) {
        final Vector3d fromRot = new Vector3d(this.shadow$getXRot(), this.shadow$getYRot(), 0);

        final Set<Relative> relatives = transition.relatives();
        final Vector3d toRot = new Vector3d(
            relatives.contains(Relative.X_ROT) ? fromRot.x() + transition.xRot() : transition.xRot(),
            relatives.contains(Relative.Y_ROT) ? fromRot.y() + transition.yRot() : transition.yRot(),
            0
        );

        @Nullable Vector3d newToRot = SpongeCommonEventFactory.callRotateEvent((org.spongepowered.api.entity.Entity) this, fromRot, toRot);
        if (newToRot == null) {
            newToRot = fromRot; // Cancelled, reset to original rotation
        }

        if (newToRot.equals(toRot)) {
            return transition;
        }

        final Set<Relative> newRelatives = EnumSet.noneOf(Relative.class);
        newRelatives.addAll(transition.relatives());
        newRelatives.removeAll(Relative.ROTATION);

        return new TeleportTransition(
            transition.newLevel(),
            transition.position(),
            transition.deltaMovement(),
            (float) newToRot.y(), (float) newToRot.x(),
            transition.missingRespawnBlock(),
            transition.asPassenger(),
            newRelatives,
            transition.postTeleportTransition()
        );
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

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;vehicle:Lnet/minecraft/world/entity/Entity;",
            ordinal = 0
        ),
        cancellable = true
    )
    private void impl$onStartRiding(final Entity vehicle, final boolean force, final boolean createPortal,
        final CallbackInfoReturnable<Boolean> ci) {
        if (!this.shadow$level().isClientSide() && ShouldFire.RIDE_ENTITY_EVENT_MOUNT) {
            PhaseTracker.getInstance().pushCause(this);
            if (SpongeCommon.post(SpongeEventFactory.createRideEntityEventMount(PhaseTracker.getInstance().currentCause(), (org.spongepowered.api.entity.Entity) vehicle))) {
                ci.cancel();
            }
            PhaseTracker.getInstance().popCause();
        }
    }

    @Inject(method = "removeVehicle",
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;vehicle:Lnet/minecraft/world/entity/Entity;", opcode = Opcodes.PUTFIELD), cancellable = true)
    private void impl$onRemoveVehicle(final CallbackInfo ci) {
        final Entity vehicle = this.shadow$getVehicle();
        if (vehicle != null) {
            if (this.shadow$level().isClientSide() || !ShouldFire.RIDE_ENTITY_EVENT_DISMOUNT) {
                return;
            }

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
                frame.pushCause(this);
                if (!frame.currentContext().containsKey(EventContextKeys.DISMOUNT_TYPE)) {
                    frame.addContext(EventContextKeys.DISMOUNT_TYPE, vehicle.isRemoved()
                        ? DismountTypes.DEATH.get()
                        : DismountTypes.PLAYER.get());
                }
                if (SpongeCommon.post(SpongeEventFactory.
                    createRideEntityEventDismount(frame.currentCause(), (org.spongepowered.api.entity.Entity) this.shadow$getVehicle()))) {
                    ci.cancel();
                }
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
            target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"
        )
    )
    private boolean impl$createLavaBlockDamageSource(final Entity instance, ServerLevel serverLevel, DamageSource source, float damage) {
        final AABB bb = this.shadow$getBoundingBox().inflate(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D);
        final ServerLocation location = DamageEventUtil.findFirstMatchingBlock(instance, bb, block ->
            block.is(Blocks.LAVA) || block.getBlock() instanceof LavaCauldronBlock);
        if (location != null) {
            var blockSource = org.spongepowered.api.event.cause.entity.damage.source.DamageSource.builder()
                    .from((org.spongepowered.api.event.cause.entity.damage.source.DamageSource) source).block(location)
                    .block(location.createSnapshot()).build();
            return instance.hurtServer(serverLevel, (DamageSource) blockSource, damage);
        }
        return instance.hurtServer(serverLevel, source, damage);
    }

    @ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 impl$onMoveCollide(final Vec3 afterCollide, @Local(argsOnly = true) final Vec3 originalMove) {
        if (ShouldFire.COLLIDE_BLOCK_EVENT_MOVE && !originalMove.equals(afterCollide)) {
            // We had a collision! Try to find the colliding block
            final Vec3 position = new Vec3(this.shadow$getX() + afterCollide.x, this.shadow$getY() + afterCollide.y, this.shadow$getZ() + afterCollide.z);
            final AABB boundingBox = this.dimensions.makeBoundingBox(position)
                    .expandTowards(originalMove.x - afterCollide.x, originalMove.y - afterCollide.y, originalMove.z - afterCollide.z);

            Optional<Vec3> closestPoint = Optional.empty();
            for (final VoxelShape shape : this.shadow$level().getBlockCollisions((Entity) (Object) this, boundingBox)) {
                final Optional<Vec3> shapeClosestPoint = shape.closestPointTo(position);
                if (shapeClosestPoint.isPresent()) {
                    if (closestPoint.isEmpty()) {
                        closestPoint = shapeClosestPoint;
                    } else if (position.distanceToSqr(closestPoint.get()) > position.distanceToSqr(shapeClosestPoint.get())) {
                        closestPoint = shapeClosestPoint;
                    }
                }
            }

            final BlockPos pos = closestPoint.map(p -> BlockPos.containing(p.x, p.y, p.z)).orElse(BlockPos.containing(position.x, position.y, position.z));
            final BlockState state = this.shadow$level().getBlockState(pos);
            final org.spongepowered.api.util.Direction dir = org.spongepowered.api.util.Direction.closest(new Vector3d(originalMove.x, originalMove.y, originalMove.z));
            if (!state.isAir() && SpongeCommonEventFactory.handleCollideBlockEvent(state.getBlock(), this.shadow$level(), pos, state,
                    (Entity) (Object) this, dir, SpongeCommonEventFactory.CollisionType.MOVE)) {
                return originalMove;
            }
        }
        return afterCollide;
    }

    @Redirect(method = "checkFallDamage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;D)V"))
    private void impl$onFallOnCollide(final Block block, final Level world, final BlockState state, final BlockPos pos, final Entity entity, final double fallDistance) {
        if (!ShouldFire.COLLIDE_BLOCK_EVENT_FALL || world.isClientSide()) {
            block.fallOn(world, state, pos, entity, fallDistance);
            return;
        }

        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, org.spongepowered.api.util.Direction.UP, SpongeCommonEventFactory.CollisionType.FALL)) {
            block.fallOn(world, state, pos, entity, fallDistance);
            this.impl$lastCollidedBlockPos = pos;
        }
    }

    @WrapOperation(
            method = "applyEffectsFromBlocks(Ljava/util/List;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/Block;stepOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/Entity;)V"
            )
    )
    private void impl$onStepOnCollide(final Block block, final Level world, final BlockPos pos, final BlockState state,
                                      final Entity entity, Operation<Void> original) {
        if (!ShouldFire.COLLIDE_BLOCK_EVENT_STEP_ON || world.isClientSide()) {
            original.call(block, world, pos, state, entity);
            return;
        }

        final org.spongepowered.api.util.Direction dir = org.spongepowered.api.util.Direction.NONE;
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(block, world, pos, state, entity, dir, SpongeCommonEventFactory.CollisionType.STEP_ON)) {
            original.call(block, world, pos, state, entity);
            this.impl$lastCollidedBlockPos = pos;
        }
    }

    @WrapOperation(method = "lambda$checkInsideBlocks$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;entityInside(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/InsideBlockEffectApplier;Z)V"
            )
    ) // doBlockCollisions
    private void impl$onCheckInsideBlocksCollide(
        final BlockState blockState, final Level worldIn, final BlockPos pos, final Entity entityIn,
        final InsideBlockEffectApplier insideBlockEffectApplier, final boolean applies, final Operation<Void> original) {
        if (!ShouldFire.COLLIDE_BLOCK_EVENT_INSIDE || worldIn.isClientSide() || blockState.isAir()) {
            original.call(blockState, worldIn, pos, entityIn, insideBlockEffectApplier, applies);
            return;
        }

        final org.spongepowered.api.util.Direction dir = org.spongepowered.api.util.Direction.NONE;
        if (!SpongeCommonEventFactory.handleCollideBlockEvent(blockState.getBlock(), worldIn, pos, blockState, entityIn, dir, SpongeCommonEventFactory.CollisionType.INSIDE)) {
            original.call(blockState, worldIn, pos, entityIn, insideBlockEffectApplier, applies);
            this.impl$lastCollidedBlockPos = pos;
        }

    }

    @Redirect(method = "isSilent", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/network/syncher/SynchedEntityData;get(Lnet/minecraft/network/syncher/EntityDataAccessor;)Ljava/lang/Object;"
    ))
    private Object impl$checkIsSilentOrInvis(final SynchedEntityData data, final EntityDataAccessor<Boolean> key) {
        return data.get(key) || !this.bridge$vanishState().createsSounds();
    }

    @Redirect(method = "push(Lnet/minecraft/world/entity/Entity;)V",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;noPhysics:Z",
            opcode = Opcodes.GETFIELD))
    private boolean impl$applyEntityCollisionCheckVanish(final Entity entity) {
        return entity.noPhysics || ((VanishableBridge) entity).bridge$vanishState().ignoresCollisions();
    }

    @Redirect(method = "doWaterSplashEffect",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void impl$spawnParticle(
        final Level instance, final ParticleOptions particleOptions, final double xCoord, final double yCoord,
        final double zCoord, final double xOffset, final double yOffset, final double zOffset
    ) {
        if (this.bridge$vanishState().createsParticles()) {
            this.shadow$level().addParticle(particleOptions, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset);
        }
    }

    @Redirect(method = "spawnSprintParticle",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private void impl$runningSpawnParticle(
        final Level instance, final ParticleOptions particleOptions, final double xCoord, final double yCoord,
        final double zCoord, final double xOffset, final double yOffset, final double zOffset
    ) {
        if (this.bridge$vanishState().createsParticles()) {
            instance.addParticle(particleOptions, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset);
        }
    }


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

    @Inject(
        method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"),
        cancellable = true
    )
    public void impl$throwDropItemConstructEvent(
        final ServerLevel serverLevel, ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> cir
    ) {
        if (stack.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }
        if (((LevelBridge) serverLevel).bridge$isFake()) {
            return;
        }
        // Now the real fun begins.
        final ItemStack item;
        final double posX = this.shadow$position().x;
        final double posY = this.shadow$position().y + offsetY;
        final double posZ = this.shadow$position().z;

        // FIRST we want to throw the DropItemEvent.PRE
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        // We want to frame ourselves here, because of the two events we have to throw, first for the drop item event, then the constructentityevent.
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            // Perform the event throws first, if they return false, return null
            item = SpongeCommonEventFactory.throwDropItemAndConstructEvent(
                (Entity) (Object) this, posX, posY, posZ, snapshot, original, frame);

            if (item == null || item.isEmpty()) {
                cir.setReturnValue(null);
                return;
            }
            final ItemEntity entityitem = new ItemEntity(this.shadow$level(), posX, posY, posZ, item);
            entityitem.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(entityitem);
            cir.setReturnValue(entityitem);
        }
    }

    @org.checkerframework.checker.nullness.qual.Nullable
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
        if (!((LevelBridge) this.shadow$level()).bridge$isFake() && ShouldFire.IGNITE_ENTITY_EVENT &&
            this.remainingFireTicks < 1 && ticks >= Constants.Entity.MINIMUM_FIRE_TICKS &&
            this.impl$canCallIgniteEntityEvent()) {

            final PhaseTracker phaseTracker = PhaseTracker.getWorldInstance((ServerLevel) entity.level());
            try (final CauseStackManager.StackFrame frame = phaseTracker.pushCauseFrame()) {

                frame.pushCause(((org.spongepowered.api.entity.Entity) this).location().world());
                final IgniteEntityEvent event = SpongeEventFactory.
                    createIgniteEntityEvent(frame.currentCause(), Ticks.of(ticks), Ticks.of(ticks), (org.spongepowered.api.entity.Entity) this);

                if (SpongeCommon.post(event)) {
                    // Don't do anything
                    return;
                }
                final DataTransactionResult transaction = DataTransactionResult.builder()
                    .replace(new ImmutableSpongeValue<>(Keys.FIRE_TICKS, Ticks.of(Math.max(this.remainingFireTicks, 0))))
                    .success(new ImmutableSpongeValue<>(Keys.FIRE_TICKS, event.fireTicks()))
                    .result(DataTransactionResult.Type.SUCCESS)
                    .build();

                final ChangeDataHolderEvent.ValueChange valueChange = SpongeEventFactory.createChangeDataHolderEventValueChange(
                    phaseTracker.currentCause(),
                    transaction,
                    (DataHolder.Mutable) this);

                Sponge.eventManager().post(valueChange);
                if (valueChange.isCancelled()) {
                    //If the event is cancelled, well, don't change the underlying value.
                    return;
                }
                valueChange.endResult().successfulValue(Keys.FIRE_TICKS)
                    .map(Value::get)
                    .map(t -> (int) t.ticks())
                    .ifPresent(t -> this.remainingFireTicks = t);
            }
            return;
        }
        this.remainingFireTicks = ticks; // Vanilla functionality
    }

    @Redirect(
        method = "gameEvent(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void impl$ignoreGameEventIfVanished(final Level instance, final Entity entity, final Holder<GameEvent> gameEvent, final Vec3 vec) {
        if (entity instanceof VanishableBridge && ((VanishableBridge) entity).bridge$vanishState().triggerVibrations()) {
            instance.gameEvent(entity, gameEvent, vec);
        }
    }

    @Redirect(method = "getEncodeId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
    private boolean impl$respectTransientFlag(final EntityType entityType) {
        if (!entityType.canSerialize()) {
            return false;
        }

        return !this.impl$transient;
    }

    @Inject(method = "getFireImmuneTicks", at = @At(value = "HEAD"), cancellable = true)
    private void impl$getFireImmuneTicks(final CallbackInfoReturnable<Integer> ci) {
        if (this.impl$hasCustomFireImmuneTicks) {
            ci.setReturnValue((int) this.impl$fireImmuneTicks);
        }
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void impl$WriteSpongeDataToCompound(final ValueOutput out, final CallbackInfo ci) {
        if (out instanceof TagValueOutput tag) {
            if (DataUtil.syncDataToTag(this)) {
                // TODO - technically this shouldn't be used but we can access the build result
                //   here, ideally we may have to consider migrating the existing data serialization
                //   process to the new "style".
                tag.buildResult().merge(this.data$getCompound());
            }
        }
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void impl$ReadSpongeDataFromCompound(final ValueInput input, final CallbackInfo ci) {
        this.data$setCompound(((TagValueInputAccessor) input).accessor$input()); // For vanilla we set the incoming nbt
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

    protected void impl$callExpireEntityEvent() {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.pushCause(this);
            Sponge.eventManager().post(SpongeEventFactory.createExpireEntityEvent(frame.currentCause(), (org.spongepowered.api.entity.Entity) this));
        }
    }
    @Inject(method = "discard", at = @At("TAIL"))
    private void impl$throwExpireForDiscards(final CallbackInfo ci) {
        SpongeCommon.post(SpongeEventFactory.createExpireEntityEvent(PhaseTracker.getInstance().currentCause(), (org.spongepowered.api.entity.Entity) this));
    }

    /*@Redirect(
        method = "setRemainingFireTicks",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/Entity;remainingFireTicks:I",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void impl$callIgnite(Entity entity, int value) {

    }*/

    @Redirect(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
    private boolean impl$allowRidingAnything(final EntityType<?> instance) {
        //Vanilla has started to prevent riding non-serializable entities.
        //This results in players being unable to ride other players.
        return true;
    }
}
