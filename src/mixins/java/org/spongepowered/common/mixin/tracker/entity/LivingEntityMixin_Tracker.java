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
package org.spongepowered.common.mixin.tracker.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import org.checkerframework.checker.nullness.qual.NonNull;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.LivingEntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_Tracker extends EntityMixin_Tracker implements LivingEntityBridge {
    // @formatter:off
    @Shadow protected boolean dead;
    @Shadow protected int scoreValue;
    @Shadow protected int recentlyHit;

    @Shadow protected abstract void shadow$onDeathUpdate();
    @Shadow protected abstract int shadow$getExperiencePoints(PlayerEntity player);
    @Shadow protected abstract boolean shadow$canDropLoot();
    @Shadow protected abstract void shadow$dropLoot(DamageSource damageSourceIn, boolean p_213354_2_);
    @Shadow protected abstract void shadow$dropInventory();
    @Shadow public abstract CombatTracker shadow$getCombatTracker();
    @Shadow @Nullable public abstract LivingEntity shadow$getAttackingEntity();
    @Shadow public void shadow$onDeath(final DamageSource cause) {}
    @Shadow public abstract boolean shadow$isSleeping();
    @Shadow public abstract void shadow$wakeUp();
    @Shadow protected abstract void shadow$spawnDrops(DamageSource damageSourceIn);
    @Shadow protected abstract void shadow$createWitherRose(@Nullable LivingEntity p_226298_1_);
    @Shadow public float attackedAtYaw;

    @Shadow public abstract ItemStack getHeldItem(Hand p_184586_1_);

    // @formatter:on
    private int tracker$deathEventsPosted;


    /**
     * We can enter in to the entity drops transaction here which will
     * successfully batch the side effects (this is effectively a singular side
     * effect/transaction instead of a pipeline) to perform some things around
     * the entity's death. This successfully records the transactions associated
     * with this entity entering into the death state.
     *
     * @author i509VCB - February 17th, 2020 - 1.14.4
     * @author gabizou - August 30th, 2020 - 1.15.2
     *
     * @param livingEntity The entity which is dying.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "baseTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;onDeathUpdate()V"))
    private void tracker$enterPhaseOnDeath(final LivingEntity livingEntity) {
        final PhaseTracker instance = PhaseTracker.SERVER;
        if (!instance.onSidedThread()) {
            this.shadow$onDeathUpdate();
            return;
        }
        if (((WorldBridge) this.world).bridge$isFake()) {
            this.shadow$onDeathUpdate();
            return;
        }
        final PhaseContext<@NonNull ?> context = instance.getPhaseContext();
        if (!((IPhaseState) context.state).doesBlockEventTracking(context)) {
            this.shadow$onDeathUpdate();
            return;
        }
        try (final EffectTransactor transactor = context.getTransactor().ensureEntityDropTransactionEffect((LivingEntity) (Object) this)) {
            this.shadow$onDeathUpdate();
        }
    }


    /**
     * @author gabizou - February 23rd, 2020 - Minecraft 1.14.3
     * @reason This is overridden for the Players to be able to return 0
     * if they are cancelling drops from events.
     * @param entity
     * @param attackingPlayer
     * @return
     */
    @Redirect(method = "dropExperience()V",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getExperiencePoints(Lnet/minecraft/entity/player/PlayerEntity;)I"))
    protected int tracker$modifyExperiencePointsOnDeath(final LivingEntity entity, final PlayerEntity attackingPlayer) {
        return this.shadow$getExperiencePoints(attackingPlayer);
    }

    /**
     * @author gabizou - March 8th, 2020 - Minecraft 1.14.3
     * @reason Instead of inlining the onDeath method with the main mixin, we can "toggle"
     * the usage of the death state control in the tracker mixin.
     * @param thisEntity
     * @param cause
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "attackEntityFrom",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;onDeath(Lnet/minecraft/util/DamageSource;)V"
        )
    )
    private void tracker$wrapOnDeathWithState(final LivingEntity thisEntity, final DamageSource cause) {
        // Sponge Start - notify the cause tracker
        final PhaseTracker instance = PhaseTracker.SERVER;
        if (!instance.onSidedThread()) {
            return;
        }
        if (((WorldBridge) this.world).bridge$isFake()) {
            return;
        }
        final PhaseContext<@NonNull ?> context = instance.getPhaseContext();
        if (!((IPhaseState) context.state).doesBlockEventTracking(context)) {
            return;
        }
        try (final EffectTransactor transactor = context.getTransactor().ensureEntityDropTransactionEffect((LivingEntity) (Object) this)) {
            // Create new EntityDeathTransaction
            // Add new EntityDeathEffect
            this.shadow$onDeath(cause);
        }
        // Sponge End
    }

    @Redirect(method = "spawnDrops",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;dropInventory()V"
            )
    )
    private void tracker$dropInventory(final LivingEntity thisEntity) {
        if (thisEntity instanceof PlayerEntityBridge && ((PlayerEntityBridge) thisEntity).bridge$keepInventory()) {
            return;
        }
        this.shadow$dropInventory();
    }

    /**
     * @author blood - May 12th, 2016
     * @author gabizou - June 4th, 2016 - Update for 1.9.4 and Cause Tracker changes
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles all living entity death events
     * (except players). Note should be taken that normally, there are lists for drops captured, however, the drops
     * are retroactively captured by the PhaseTracker and associated through the different phases, depending on
     * how they are handled, so no lists and flags on the entities themselves are needed as they are tracked through
     * the {@link PhaseContext} of the current {@link IPhaseState} at which this method is called. The compatibility
     * for Forge's events to throw are handled specially in SpongeForge.
     */
    @SuppressWarnings("ConstantConditions")
    @Overwrite
    public void onDeath(final DamageSource cause) {
        // Sponge Start - Call our event, and forge's event
        // This will transitively call the forge event
        final boolean isMainThread = !((WorldBridge) this.world).bridge$isFake() || Sponge.isServerAvailable() && Sponge.getServer().onMainThread();
        if (!this.dead) { // isDead should be set later on in this method so we aren't re-throwing the events.
            if (isMainThread && this.tracker$deathEventsPosted <= Constants.Sponge.MAX_DEATH_EVENTS_BEFORE_GIVING_UP) {
                // ignore because some moron is not resetting the entity.
                this.tracker$deathEventsPosted++;
                if (SpongeCommonEventFactory.callDestructEntityEventDeath((LivingEntity) (Object) this, cause, isMainThread).map(Cancellable::isCancelled).orElse(true)) {
                    // Since the forge event is cancellable
                    return;
                }
            }
        } else {
            this.tracker$deathEventsPosted = 0;
        }
        // Sponge End

        if (this.dead || this.removed) {
            return;
        }

        final Entity entity = cause.getTrueSource();
        final LivingEntity livingEntity = this.shadow$getAttackingEntity();

        if (this.scoreValue >= 0 && livingEntity != null) {
            livingEntity.awardKillScore((LivingEntity) (Object) this, this.scoreValue, cause);
        }

        if (entity != null) {
            entity.onKillEntity((LivingEntity) (Object) this);
        }

        if (this.shadow$isSleeping()) {
            this.shadow$wakeUp();
        }

        this.dead = true;
        this.shadow$getCombatTracker().reset();

        if (!this.world.isRemote) {
            // TODO - Determine what Forge needs here
            this.shadow$spawnDrops(cause);
            this.shadow$createWitherRose(livingEntity);

        }

        // Sponge Start - Don't send the state if this is a human. Fixes ghost items on client.
        if (!((LivingEntity) (Object) this instanceof HumanEntity)) {
            this.world.setEntityState((LivingEntity) (Object) this, (byte) 3);
        }
        // Sponge End
        this.shadow$setPose(Pose.DYING);
    }

    @Override
    public void bridge$resetDeathEventsPosted() {
        this.tracker$deathEventsPosted = 0;
    }


}
