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
package org.spongepowered.common.mixin.tracker.world.entity;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.entity.EntityTrackedBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin_Tracker implements TrackableBridge, EntityTrackedBridge {

    // @formatter:off
    @Shadow @Final private EntityType<?> type;
    @Shadow public Level level;
    @Shadow public boolean removed;
    @Shadow private Vec3 position;
    @Shadow public float yRot;
    @Shadow public float xRot;
    @Shadow @Final protected Random random;

    @Shadow public abstract void shadow$clearFire();
    @Shadow protected abstract void shadow$setSharedFlag(int flag, boolean set);
    @Shadow @Nullable public abstract Team getTeam();
    @Shadow public abstract void shadow$setPos(double x, double y, double z);
    @Shadow public abstract void shadow$setDeltaMovement(Vec3 motionIn);
    @Shadow public abstract void shadow$setDeltaMovement(double x, double y, double z);
    @Shadow public abstract float getEyeHeight();
    @Shadow public abstract UUID shadow$getUUID();
    @Shadow public abstract void shadow$setPose(Pose pose);
    @Shadow protected abstract void shadow$reapplyPosition();
    @Shadow public abstract double shadow$getEyeY();
    @Shadow public abstract double shadow$getX();
    @Shadow public abstract double shadow$getZ();
    private boolean tracker$trackedInWorld = false;
    @Nullable private Cause tracker$destructCause;
    //@formatter:on


//    @Inject(method = "<init>", at = @At("RETURN"))
//    private void tracker$refreshTrackerStates(final EntityType<?> entityType, final net.minecraft.world.World world, final CallbackInfo ci) {
//        this.bridge$refreshTrackerStates();
//
//        final EntityTypeBridge entityTypeBridge = (EntityTypeBridge) entityType;
//        if (!entityTypeBridge.bridge$checkedDamageEntity()) {
//            try {
//                final String mapping = Launcher.getInstance().isDeveloperEnvironment() ? Constants.Entity.ATTACK_ENTITY_FROM_MAPPING : Constants.Entity.ATTACK_ENTITY_FROM_OBFUSCATED;
//                final Class<?>[] argTypes = {DamageSource.class, float.class};
//                final Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
//                if (!(clazz.equals(LivingEntity.class) || clazz.equals(PlayerEntity.class) || clazz.equals(ServerPlayerEntity.class))) {
//                    entityTypeBridge.bridge$setOverridesDamageEntity(true);
//                }
//            } catch (final Throwable ex) {
//                // In some rare cases, we just want to ignore class errors or
//                // reflection errors and can "Safely" ignore our tracking because the alternative
//                // is to silently ignore the mod's custom handling if it's there.
//                entityTypeBridge.bridge$setOverridesDamageEntity(true);
//            } finally {
//                entityTypeBridge.bridge$setCheckedDamageEntity(true);
//            }
//        }
//    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(
        method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD")
    )
    private void tracker$logEntityDropTransactionIfNecessary(final ItemStack stack, final float offsetY,
        final CallbackInfoReturnable<ItemEntity> cir) {
        final PhaseTracker instance = PhaseTracker.SERVER;
        if (!instance.onSidedThread()) {
            return;
        }
        if (((WorldBridge) this.level).bridge$isFake()) {
            return;
        }
        final PhaseContext<@NonNull ?> context = instance.getPhaseContext();
        if (!context.doesBlockEventTracking()) {
            return;
        }
        if (this.tracker$dropsTransactor == null) {
            this.tracker$dropsTransactor = context.getTransactor().ensureEntityDropTransactionEffect((Entity) (Object) this);
        }
    }

    protected @MonotonicNonNull EffectTransactor tracker$dropsTransactor = null;

    @Inject(method = "remove()V", at = @At("RETURN"))
    private void tracker$ensureDropEffectCompleted(final CallbackInfo ci) {
        final PhaseTracker instance = PhaseTracker.SERVER;
        if (!instance.onSidedThread()) {
            return;
        }
        if (((WorldBridge) this.level).bridge$isFake()) {
            return;
        }
        final PhaseContext<@NonNull ?> context = instance.getPhaseContext();
        if (!context.doesBlockEventTracking()) {
            return;
        }
        if (this.tracker$dropsTransactor != null) {
            this.tracker$dropsTransactor.close();
        }
    }

    @Override
    public boolean bridge$isWorldTracked() {
        return this.tracker$trackedInWorld;
    }

    @Override
    public void bridge$setWorldTracked(final boolean tracked) {
        this.tracker$trackedInWorld = tracked;
        // Since this is called during removeEntity from world, we can
        // post the removal event here, basically.
        if (!tracked && this.tracker$destructCause != null && ShouldFire.DESTRUCT_ENTITY_EVENT) {
            final Audience originalChannel = Audience.empty();
            SpongeCommon.post(SpongeEventFactory.createDestructEntityEvent(
                this.tracker$destructCause,
                originalChannel,
                Optional.of(originalChannel),
                Component.empty(),
                Component.empty(),
                (org.spongepowered.api.entity.Entity) this,
                false
            ));

            this.tracker$destructCause = null;
        }
    }

    @Override
    public boolean bridge$shouldTick() {
//        final ChunkBridge chunk = ((ActiveChunkReferantBridge) this).bridge$getActiveChunk();
//        // Don't tick if chunk is queued for unload or is in progress of being scheduled for unload
//        // See https://github.com/SpongePowered/SpongeVanilla/issues/344
//        return chunk == null || chunk.bridge$isActive();
        return true;
    }

    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return ((TrackableBridge) this.type).bridge$allowsBlockBulkCaptures();
    }

    @Override
    public void bridge$setAllowsBlockBulkCaptures(final boolean allowsBlockBulkCaptures) {
        ((TrackableBridge) this.type).bridge$setAllowsBlockBulkCaptures(allowsBlockBulkCaptures);
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return ((TrackableBridge) this.type).bridge$allowsBlockEventCreation();
    }

    @Override
    public void bridge$setAllowsBlockEventCreation(final boolean allowsBlockEventCreation) {
        ((TrackableBridge) this.type).bridge$setAllowsBlockEventCreation(allowsBlockEventCreation);
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return ((TrackableBridge) this.type).bridge$allowsEntityBulkCaptures();
    }

    @Override
    public void bridge$setAllowsEntityBulkCaptures(final boolean allowsEntityBulkCaptures) {
        ((TrackableBridge) this.type).bridge$setAllowsEntityBulkCaptures(allowsEntityBulkCaptures);
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return ((TrackableBridge) this.type).bridge$allowsEntityEventCreation();
    }

    @Override
    public void bridge$setAllowsEntityEventCreation(final boolean allowsEntityEventCreation) {
        ((TrackableBridge) this.type).bridge$setAllowsEntityEventCreation(allowsEntityEventCreation);
    }

    @Override
    public void bridge$refreshTrackerStates() {
        ((TrackableBridge) this.type).bridge$refreshTrackerStates();
    }

    @Override
    public void populateFrameModifier(final CauseStackManager.StackFrame frame, final EntityTickContext context) {

    }

    @Inject(method = "remove", at = @At(value = "RETURN"))
    private void impl$createDestructionEventOnDeath(final CallbackInfo ci) {
        if (ShouldFire.DESTRUCT_ENTITY_EVENT
                && !((WorldBridge) this.level).bridge$isFake()) {

            if (!((Entity) (Object) this instanceof LivingEntity)) {
                this.tracker$destructCause = PhaseTracker.getCauseStackManager().currentCause();
            } else if ((Entity) (Object) this instanceof ArmorStand) {
                SpongeCommonEventFactory.callDestructEntityEventDeath((ArmorStand) (Object) this, null);
            }
        }
    }
}
