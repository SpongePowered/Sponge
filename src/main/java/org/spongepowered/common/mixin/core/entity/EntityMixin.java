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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.cause.entity.dismount.DismountTypes;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.InvulnerableTrackedBridge;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.entity.EntityBridge;
import org.spongepowered.common.bridge.entity.EntityTypeBridge;
import org.spongepowered.common.bridge.entity.GrieferBridge;
import org.spongepowered.common.bridge.network.ServerPlayNetHandlerBridge;
import org.spongepowered.common.bridge.util.DamageSourceBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.chunk.ActiveChunkReferantBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.damage.MinecraftBlockDamageSource;
import org.spongepowered.common.mixin.accessor.world.server.ChunkManagerAccessor;
import org.spongepowered.common.mixin.accessor.world.server.EntityTrackerAccessor;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityBridge, TrackableBridge, VanishableBridge, InvulnerableTrackedBridge, TimingBridge {

    // @formatter:off
    @Shadow @Nullable private Entity ridingEntity;
    @Shadow @Final private List<Entity> passengers;
    @Shadow public net.minecraft.world.World world;
    @Shadow public double posX;
    @Shadow public double posY;
    @Shadow public double posZ;
    @Shadow private Vec3d motion;
    @Shadow public float rotationYaw;
    @Shadow public float rotationPitch;
    @Shadow public boolean velocityChanged;
    @Shadow public boolean onGround;
    @Shadow public boolean removed;
    @Shadow public float prevDistanceWalkedModified;
    @Shadow public float distanceWalkedModified;
    @Shadow public float fallDistance;
    @Shadow @Final protected Random rand;
    @Shadow private int fire;
    @Shadow public int hurtResistantTime;
    @Shadow @Final protected EntityDataManager dataManager;
    @Shadow public DimensionType dimension;
    @Shadow private boolean invulnerable;

    @Shadow public abstract void shadow$remove();
    @Shadow public abstract void shadow$setCustomName(@Nullable ITextComponent name);
    @Shadow public abstract AxisAlignedBB shadow$getBoundingBox();
    @Shadow public abstract boolean attackEntityFrom(DamageSource source, float amount);
    @Shadow public abstract int shadow$getEntityId();
    @Shadow public abstract boolean shadow$isBeingRidden();
    @Shadow public abstract Entity getRidingEntity();
    @Shadow public abstract void shadow$playSound(SoundEvent soundIn, float volume, float pitch);
    @Shadow protected abstract void shadow$removePassenger(Entity passenger);
    @Shadow @Nullable public Entity shadow$changeDimension(final DimensionType dimension) { return null; } // Shadow
    @Shadow public abstract boolean shadow$isInvisible();
    @Shadow public abstract void shadow$setInvisible(boolean invisible);
    @Shadow protected abstract int shadow$getFireImmuneTicks();
    @Shadow public abstract EntityType<?> shadow$getType();
    @Shadow public abstract boolean shadow$isInvulnerableTo(DamageSource source);
    @Shadow public abstract void shadow$setMotion(Vec3d motionIn);
    @Shadow public abstract boolean shadow$isSprinting();
    @Shadow public abstract Vec3d shadow$getMotion();
    @Shadow public abstract boolean shadow$isOnSameTeam(Entity entityIn);
    @Shadow public abstract double shadow$getDistanceSq(Entity entityIn);
    @Shadow public abstract boolean shadow$isInWater();
    @Shadow public abstract boolean shadow$isPassenger();
    @Shadow public abstract void shadow$setPositionAndUpdate(double x, double y, double z);
    @Shadow public abstract int shadow$getMaxAir();

    private boolean impl$isConstructing = true;
    @Nullable private Text impl$displayName;
    @Nullable private BlockPos impl$lastCollidedBlockPos;
    private boolean impl$trackedInWorld = false;
    private boolean vanish$collision = false;
    private boolean vanish$untargetable = false;
    private boolean vanish$isVanished = false;
    private boolean vanish$pendingVisibilityUpdate = false;
    @Nullable private Cause impl$destructCause;
    private int impl$customFireImmuneTicks = this.shadow$getFireImmuneTicks();
    private boolean impl$skipSettingCustomNameTag = false;
    private int vanish$visibilityTicks = 0;

    // @formatter:on

    @Override
    public boolean bridge$isConstructing() {
        return this.impl$isConstructing;
    }

    @Override
    public void bridge$fireConstructors() {
        this.impl$isConstructing = false;
    }

    @Override
    public boolean bridge$isWorldTracked() {
        return this.impl$trackedInWorld;
    }

    @Override
    public void bridge$setWorldTracked(final boolean tracked) {
        this.impl$trackedInWorld = tracked;
        // Since this is called during removeEntity from world, we can
        // post the removal event here, basically.
        if (!tracked && this.impl$destructCause != null) {
            final MessageChannel originalChannel = MessageChannel.toNone();
            SpongeImpl.postEvent(SpongeEventFactory.createDestructEntityEvent(
                this.impl$destructCause, originalChannel, Optional.of(originalChannel),
                (org.spongepowered.api.entity.Entity) this, new MessageEvent.MessageFormatter(), true
            ));

            this.impl$destructCause = null;
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
            Sponge.getCauseStackManager().pushCause(this);
            if (SpongeImpl.postEvent(SpongeEventFactory.createRideEntityEventMount(Sponge.getCauseStackManager().getCurrentCause(), (org.spongepowered.api.entity.Entity) vehicle))) {
                ci.cancel();
            }
            Sponge.getCauseStackManager().popCause();
        }
    }

    /**
     * @author rexbut - December 16th, 2016
     * @reason - adjusted to support {@link DismountTypes}
     */
    @Overwrite
    public void stopRiding() {
        if (this.ridingEntity != null) {
            if (this.getRidingEntity().removed) {
                this.impl$dismountRidingEntity(DismountTypes.DEATH.get());
            } else {
                this.impl$dismountRidingEntity(DismountTypes.PLAYER.get());
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private boolean impl$dismountRidingEntity(final DismountType type) {
        if (!this.world.isRemote && (ShouldFire.RIDE_ENTITY_EVENT_DISMOUNT || ShouldFire.RIDE_ENTITY_EVENT)) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                frame.addContext(EventContextKeys.DISMOUNT_TYPE, type);
                if (SpongeImpl.postEvent(SpongeEventFactory.
                    createRideEntityEventDismount(frame.getCurrentCause(), (org.spongepowered.api.entity.Entity) this.getRidingEntity()))) {
                    return false;
                }
            }
        }

        if (this.ridingEntity != null) {
            final EntityMixin entity = (EntityMixin) (Object) this.ridingEntity;
            this.ridingEntity = null;
            entity.shadow$removePassenger((Entity) (Object) this);
        }
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean bridge$removePassengers(final DismountType type) {
        boolean dismount = false;
        for (int i = this.passengers.size() - 1; i >= 0; --i) {
            dismount = ((EntityMixin) (Object) this.passengers.get(i)).impl$dismountRidingEntity(type) || dismount;
        }
        return dismount;
    }

    @Inject(method = "move",
        at = @At("HEAD"),
        cancellable = true)
    private void impl$onSpongeMoveEntity(final MoverType type, final Vec3d vec3d, final CallbackInfo ci) {
        if (!this.world.isRemote && !SpongeHooks.checkEntitySpeed(((Entity) (Object) this), vec3d.getX(), vec3d.getY(), vec3d.getZ())) {
            ci.cancel();
        }
    }

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
            final Location location = DamageEventHandler.findFirstMatchingBlock((Entity) (Object) this, bb, block ->
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
            final Location location = DamageEventHandler.findFirstMatchingBlock((Entity) (Object) this, bb, block ->
                block.getBlock() == Blocks.FIRE || block.getBlock() == Blocks.LAVA);

            final MinecraftBlockDamageSource fire = new MinecraftBlockDamageSource("inFire", location);
            ((DamageSourceBridge) (Object) fire).bridge$setFireSource();
            return entity.attackEntityFrom(DamageSource.IN_FIRE, damage);
        } finally {
            // Since "source" is already the DamageSource.IN_FIRE object, we can re-use it to re-assign.
            ((DamageSourceBridge) source).bridge$setFireSource();
        }

    }

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
        if (this.vanish$pendingVisibilityUpdate && !this.world.isRemote) {
            final EntityTrackerAccessor trackerAccessor = ((ChunkManagerAccessor) ((ServerWorld) this.world).getChunkProvider().chunkManager).accessor$getEntityTrackers().get(this.shadow$getEntityId());
            if (trackerAccessor != null && this.vanish$visibilityTicks % 4 == 0) {
                if (this.vanish$isVanished) {
                    for (final ServerPlayerEntity entityPlayerMP : trackerAccessor.accessor$getTrackingPlayers()) {
                        entityPlayerMP.connection.sendPacket(new SDestroyEntitiesPacket(this.shadow$getEntityId()));
                        if ((Entity) (Object) this instanceof ServerPlayerEntity) {
                            entityPlayerMP.connection.sendPacket(
                                new SPlayerListItemPacket(SPlayerListItemPacket.Action.REMOVE_PLAYER, (ServerPlayerEntity) (Object) this));
                        }
                    }
                } else {
                    this.vanish$visibilityTicks = 1;
                    this.vanish$pendingVisibilityUpdate = false;
                    for (final ServerPlayerEntity entityPlayerMP : SpongeImpl.getServer().getPlayerList().getPlayers()) {
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
            if (this.vanish$visibilityTicks > 0) {
                this.vanish$visibilityTicks--;
            } else {
                this.vanish$pendingVisibilityUpdate = false;
            }
        }
    }

    @Override
    public boolean bridge$getIsInvulnerable() {
        return this.invulnerable;
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #impl$writeToSpongeCompound}.
     *
     * <p> This makes it easier for other entity mixins to override writeToNBT
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write
     *     to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "writeWithoutTypeId(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;",
        at = @At("HEAD"))
    private void impl$spongeWriteToNBT(final CompoundNBT compound, final CallbackInfoReturnable<CompoundNBT> ci) {
        if (((CustomDataHolderBridge) this).bridge$hasManipulators()) {
            this.impl$writeToSpongeCompound(((DataCompoundHolder) this).data$getSpongeDataCompound());
        }
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #impl$readFromSpongeCompound}.
     *
     * <p> This makes it easier for other entity mixins to override readSpongeNBT
     * without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read
     *     from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "read(Lnet/minecraft/nbt/CompoundNBT;)V",
        at = @At("RETURN"))
    private void impl$spongeReadFromNBT(final CompoundNBT compound, final CallbackInfo ci) {
        if (this.impl$isConstructing) {
            this.bridge$fireConstructors(); // Do this early as possible
        }
        this.impl$readFromSpongeCompound(((DataCompoundHolder) this).data$getSpongeDataCompound());
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
        DataUtil.readCustomData(compound, ((org.spongepowered.api.entity.Entity) this));
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
        DataUtil.writeCustomData(compound, (org.spongepowered.api.entity.Entity) this);
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


    @Override
    public boolean bridge$isInvisible() {
        return this.shadow$isInvisible();
    }

    @Override
    public void bridge$setInvisible(final boolean invisible) {
        this.shadow$setInvisible(invisible);
        if (invisible) {
            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeDataCompound();
            spongeData.putBoolean(Constants.Sponge.Entity.IS_INVISIBLE, true);
        } else {
            if (((DataCompoundHolder) this).data$hasSpongeDataCompound()) {
                ((DataCompoundHolder) this).data$getSpongeDataCompound().remove(Constants.Sponge.Entity.IS_INVISIBLE);
            }
        }
    }

    @Override
    public boolean bridge$isVanished() {
        return this.vanish$isVanished;
    }

    @Override
    public void bridge$setVanished(final boolean vanished) {
        this.vanish$isVanished = vanished;
        this.vanish$pendingVisibilityUpdate = true;
        this.vanish$visibilityTicks = 20;
        if (vanished) {
            final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeDataCompound();
            spongeData.putBoolean(Constants.Sponge.Entity.IS_VANISHED, true);
        } else {
            if (((DataCompoundHolder) this).data$hasSpongeDataCompound()) {
                final CompoundNBT spongeData = ((DataCompoundHolder) this).data$getSpongeDataCompound();
                spongeData.remove(Constants.Sponge.Entity.IS_VANISHED);
                spongeData.remove(Constants.Sponge.Entity.VANISH_UNCOLLIDEABLE);
                spongeData.remove(Constants.Sponge.Entity.VANISH_UNTARGETABLE);
            }
        }
    }

    @Override
    public boolean bridge$isUncollideable() {
        return this.vanish$collision;
    }

    @Override
    public void bridge$setUncollideable(final boolean prevents) {
        this.vanish$collision = prevents;
    }

    @Override
    public boolean bridge$isUntargetable() {
        return this.vanish$untargetable;
    }

    @Override
    public void bridge$setUntargetable(final boolean untargetable) {
        this.vanish$untargetable = untargetable;
    }

    /**
     * @author gabizou - January 4th, 2016
     * @reason gabizou - January 27th, 2016 - Rewrite to a redirect
     *     <p>
     *     This prevents sounds from being sent to the server by entities that are vanished
     */
    @Redirect(method = "playSound",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;isSilent()Z"))
    private boolean impl$checkIsSilentOrInvis(final Entity entity) {
        return entity.isSilent() || this.vanish$isVanished;
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
        if (!this.vanish$isVanished) {
            this.world.addParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset);
        }
    }

    @Redirect(method = "createRunningParticles",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particles/IParticleData;DDDDDD)V"))
    private void impl$runningSpawnParticle(final net.minecraft.world.World world, final IParticleData particleTypes,
        final double xCoord, final double yCoord, final double zCoord,
        final double xOffset, final double yOffset, final double zOffset) {
        if (!this.vanish$isVanished) {
            this.world.addParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset);
        }
    }

    @Nullable
    @Override
    public Text bridge$getDisplayNameText() {
        return this.impl$displayName;
    }

    @Override
    public void bridge$setDisplayName(
        @Nullable
        final Text displayName) {
        this.impl$displayName = displayName;

        this.impl$skipSettingCustomNameTag = true;
        if (this.impl$displayName == null) {
            this.shadow$setCustomName(null);
        } else {
            this.shadow$setCustomName(SpongeTexts.toComponent(this.impl$displayName));
        }

        this.impl$skipSettingCustomNameTag = false;
    }

    @Inject(method = "setCustomName",
        at = @At("RETURN"))
    private void impl$UpdatedisplayNameText(final ITextComponent name, final CallbackInfo ci) {
        if (!this.impl$skipSettingCustomNameTag) {
            this.impl$displayName = SpongeTexts.toText(name);
        }
    }

    /**
     * @param stack
     * @param offsetY
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

    @Override
    public Timing bridge$getTimingsHandler() {
        return ((EntityTypeBridge) this.shadow$getType()).bridge$getTimings();
    }

    @Override
    public boolean bridge$shouldTick() {
        final ChunkBridge chunk = ((ActiveChunkReferantBridge) this).bridge$getActiveChunk();
        // Don't tick if chunk is queued for unload or is in progress of being scheduled for unload
        // See https://github.com/SpongePowered/SpongeVanilla/issues/344
        return chunk == null || chunk.bridge$isActive();
    }

    @Override
    public void bridge$setInvulnerable(final boolean value) {
        this.invulnerable = value;
    }


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
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

                frame.pushCause(((org.spongepowered.api.entity.Entity) this).getLocation().getWorld());
                final IgniteEntityEvent event = SpongeEventFactory.
                    createIgniteEntityEvent(frame.getCurrentCause(), ticks, ticks, (org.spongepowered.api.entity.Entity) this);

                if (SpongeImpl.postEvent(event)) {
                    this.fire = 0;
                    return; // set fire ticks to 0
                }
                this.fire = event.getFireTicks();
            }
        }
    }

    /**
     * Overridden method for Players to determine whether this entity is immune to fire
     * such that {@link IgniteEntityEvent}s are not needed to be thrown as they cannot
     * take fire damage, nor do they light on fire.
     *
     * @return True if this entity is immune to fire.
     */
    protected boolean impl$isImmuneToFireForIgniteEvent() { // Since normal entities don't have the concept of having game modes...
        return false;
    }

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

            this.impl$destructCause = Sponge.getCauseStackManager().getCurrentCause();
        }
    }

    @Inject(method = "getFireImmuneTicks",
        at = @At(value = "RETURN"))
    private void impl$getFireImmuneTicks(final CallbackInfoReturnable<Integer> ci) {
        ci.setReturnValue(this.impl$customFireImmuneTicks);
    }

    @Override
    public void bridge$setFireImmuneTicks(final int ticks) {
        this.impl$customFireImmuneTicks = ticks;
    }
}
