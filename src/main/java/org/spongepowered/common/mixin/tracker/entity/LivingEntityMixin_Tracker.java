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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.DamageSource;
import net.minecraft.world.GameRules;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.OwnershipTrackedBridge;
import org.spongepowered.common.bridge.entity.LivingEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.entity.EntityDeathContext;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
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
    @Shadow public abstract CombatTracker shadow$getCombatTracker();
    @Shadow @Nullable public abstract LivingEntity shadow$getAttackingEntity();
    // @formatter:on
    private int tracker$deathEventsPosted;


    /**
     * @author i509VCB - February 17th, 2020 - 1.14.4
     *
     * @reason Enter phase state on entity death.
     * @param livingEntity The entity which is dying.
     */
    @Redirect(method = "baseTick()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;onDeathUpdate()V"))
    private void tracker$enterPhaseOnDeath(final LivingEntity livingEntity) {
        if (!((WorldBridge) this.world).bridge$isFake()) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame();
                 final PhaseContext<?> context = EntityPhase.State.DEATH_UPDATE.createPhaseContext(PhaseTracker.SERVER).source(livingEntity)) {
                context.buildAndSwitch();
                frame.pushCause(this);
                this.shadow$onDeathUpdate();
            }
        } else {
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
    @Redirect(method = "onDeathUpdate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getExperiencePoints(Lnet/minecraft/entity/player/PlayerEntity;)I"))
    protected int tracker$modifyExperiencePointsOnDeath(final LivingEntity entity, final PlayerEntity attackingPlayer) {
        return this.shadow$getExperiencePoints(attackingPlayer);
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

        // Double check that the PhaseTracker is already capturing the Death phase
        try (final EntityDeathContext context = this.tracker$createOrNullDeathPhase(isMainThread, cause)) {
            // We re-enter the state only if we aren't already in the death state. This can usually happen when
            // and only when the onDeath method is called outside of attackEntityFrom, which should never happen.
            // but then again, mods....
            if (context != null) {
                context.buildAndSwitch();
            }
            // Sponge End
            if (this.dead) {
                return;
            }

            final Entity entity = cause.getTrueSource();
            final LivingEntity entitylivingbase = this.shadow$getAttackingEntity();

            if (this.scoreValue >= 0 && entitylivingbase != null) {
                entitylivingbase.awardKillScore((LivingEntity) (Object) this, this.scoreValue, cause);
            }

            if (entity != null) {
                entity.onKillEntity((LivingEntity) (Object) this);
            }

            this.dead = true;
            this.shadow$getCombatTracker().reset();

            if (!this.world.isRemote) {
                int i = 0;

                if (entity instanceof PlayerEntity) {
                    // Sponge Start - use Forge hooks for the looting modifier.
                    //i = EnchantmentHelper.getLootingModifier((EntityLivingBase) entity);
                    i = SpongeImplHooks.getLootingEnchantmentModifier((LivingEntity) (Object) this, (LivingEntity) entity, cause);
                }

                if (this.shadow$canDropLoot() && this.world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
                    final boolean flag = this.recentlyHit > 0;
                    this.shadow$dropLoot(cause, flag);
                }

            }

            // Sponge Start - Don't send the state if this is a human. Fixes ghost items on client.
            if (!((LivingEntity) (Object) this instanceof HumanEntity)) {
                this.world.setEntityState((LivingEntity) (Object) this, (byte) 3);
            }

        }

        // Sponge End
    }

    @Override
    public void bridge$resetDeathEventsPosted() {
        this.tracker$deathEventsPosted = 0;
    }

    @Nullable
    private EntityDeathContext tracker$createOrNullDeathPhase(final boolean isMainThread, final DamageSource source) {
        final boolean tracksEntityDeaths;
        if (((WorldBridge) this.world).bridge$isFake() || !isMainThread) { // Short circuit to avoid erroring on handling
            return null;
        }
        final IPhaseState<?> state = PhaseTracker.getInstance().getCurrentContext().state;
        tracksEntityDeaths = !state.tracksEntityDeaths() && state != EntityPhase.State.DEATH;
        if (tracksEntityDeaths) {
            final EntityDeathContext context = EntityPhase.State.DEATH.createPhaseContext(PhaseTracker.SERVER)
                    .setDamageSource((org.spongepowered.api.event.cause.entity.damage.source.DamageSource) source)
                    .source(this);
            if (this instanceof OwnershipTrackedBridge) {
                ((OwnershipTrackedBridge) this).tracked$getNotifierReference().ifPresent(context::notifier);
                ((OwnershipTrackedBridge) this).tracked$getOwnerReference().ifPresent(context::owner);
            }
            return context;
        }
        return null;
    }

}
