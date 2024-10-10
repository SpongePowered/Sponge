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
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.scores.PlayerTeam;
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
import org.spongepowered.common.bridge.DelegatingConfigTrackableBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.world.entity.TrackableEntityBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;

import java.util.Optional;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin_Tracker implements DelegatingConfigTrackableBridge, TrackableEntityBridge {

    // @formatter:off
    @Shadow @Final private EntityType<?> type;
    @Shadow @Final protected RandomSource random;
    @Shadow private EntityInLevelCallback levelCallback;

    @Shadow @Nullable public abstract PlayerTeam getTeam();
    @Shadow public abstract UUID shadow$getUUID();
    @Shadow public abstract double shadow$getEyeY();
    @Shadow public abstract double shadow$getX();
    @Shadow public abstract double shadow$getY();
    @Shadow public abstract double shadow$getZ();
    @Shadow public abstract void shadow$remove(Entity.RemovalReason reason);
    @Shadow public abstract float shadow$getXRot();
    @Shadow public abstract float shadow$getYRot();
    @Shadow public abstract boolean shadow$isEffectiveAi();

    @Shadow public abstract Level shadow$level();

    //@formatter:on

    protected @MonotonicNonNull EffectTransactor tracker$dropsTransactor = null;

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
        if (((LevelBridge) this.shadow$level()).bridge$isFake()) {
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

    @Inject(method = "setRemoved(Lnet/minecraft/world/entity/Entity$RemovalReason;)V", at = @At("RETURN"))
    private void tracker$ensureDropEffectCompleted(final Entity.RemovalReason reason, final CallbackInfo ci) {
        final PhaseTracker instance = PhaseTracker.SERVER;
        if (!instance.onSidedThread()) {
            return;
        }
        if (((LevelBridge) this.shadow$level()).bridge$isFake()) {
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
    public final void tracker$populateFrameInTickContext(final CauseStackManager.StackFrame frame, final EntityTickContext context) {
        this.tracker$populateDeathContextIfNeeded(frame, context);
    }

    protected void tracker$populateDeathContextIfNeeded(final CauseStackManager.StackFrame frame,
        final EntityTickContext context) {
    }

    @Inject(method = "setRemoved", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;stopRiding()V"))
    private void impl$createDestructionEventOnDeath(final CallbackInfo ci) {
        if (ShouldFire.DESTRUCT_ENTITY_EVENT && !((LevelBridge) this.shadow$level()).bridge$isFake()
                && this.levelCallback != EntityInLevelCallback.NULL) {

            if (!((Entity) (Object) this instanceof LivingEntity)) {
                final Cause cause = PhaseTracker.getCauseStackManager().currentCause();
                final Audience originalChannel = Audience.empty();
                SpongeCommon.post(SpongeEventFactory.createDestructEntityEvent(
                    cause,
                    originalChannel,
                    Optional.of(originalChannel),
                    Component.empty(),
                    Component.empty(),
                    (org.spongepowered.api.entity.Entity) this,
                    false
                ));
            }
        }
    }

    @Override
    public TrackableBridge bridge$trackingConfigDelegate() {
        return (TrackableBridge) this.type;
    }
}
