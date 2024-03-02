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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.bridge.world.entity.LivingEntityBridge;
import org.spongepowered.common.bridge.world.entity.PlatformLivingEntityBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.cause.entity.damage.SpongeDamageSources;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.inventory.PlayerInventoryTransaction;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.DamageEventUtil;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ConstantConditions")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements LivingEntityBridge, PlatformLivingEntityBridge {

    // @formatter:off
    @Shadow protected int useItemRemaining;
    @Shadow protected boolean dead;
    @Shadow protected int deathScore;
    @Shadow protected ItemStack useItem;
    @Shadow @Nullable private DamageSource lastDamageSource;
    @Shadow private long lastDamageStamp;

    @Shadow public abstract AttributeInstance shadow$getAttribute(Holder<Attribute> attribute);
    @Shadow public abstract void shadow$setHealth(float health);
    @Shadow public abstract void shadow$setAbsorptionAmount(float amount);
    @Shadow public abstract void shadow$setItemInHand(InteractionHand hand, @Nullable ItemStack stack);
    @Shadow public abstract void shadow$stopUsingItem();
    @Shadow public abstract int shadow$getUseItemRemainingTicks();
    @Shadow public abstract float shadow$getAbsorptionAmount();
    @Shadow public abstract float shadow$getHealth();
    @Shadow public abstract boolean shadow$hasEffect(Holder<MobEffect> potion);
    @Shadow public abstract ItemStack shadow$getItemBySlot(EquipmentSlot slotIn);
    @Shadow public abstract ItemStack shadow$getMainHandItem();
    @Shadow public abstract CombatTracker shadow$getCombatTracker();
    @Shadow public void shadow$kill() { }
    @Shadow public abstract InteractionHand shadow$getUsedItemHand();
    @Shadow protected abstract void shadow$hurtCurrentlyUsedShield(float p_184590_1_);
    @Shadow protected abstract void shadow$blockUsingShield(LivingEntity p_190629_1_);
    @Shadow public abstract Optional<BlockPos> shadow$getSleepingPos();
    @Shadow protected abstract void shadow$spawnItemParticles(ItemStack stack, int count);
    @Shadow public abstract boolean shadow$onClimbable();
    @Shadow public abstract void shadow$setSprinting(boolean sprinting);
    @Shadow public abstract void shadow$setLastHurtMob(Entity entityIn);
    @Shadow protected abstract void shadow$hurtArmor(DamageSource source, float damage);
    @Shadow public abstract ItemStack shadow$getItemInHand(InteractionHand hand);
    @Shadow protected abstract void shadow$dropEquipment();
    @Shadow protected abstract void shadow$dropAllDeathLoot(DamageSource damageSourceIn);
    @Shadow @Nullable public abstract LivingEntity shadow$getKillCredit();
    @Shadow protected abstract void shadow$createWitherRose(@Nullable LivingEntity p_226298_1_);
    @Shadow  public abstract Collection<MobEffectInstance> shadow$getActiveEffects();
    @Shadow public abstract float shadow$getMaxHealth();
    @Shadow public abstract AttributeMap shadow$getAttributes();
    @Shadow public abstract void shadow$clearSleepingPos();
    @Shadow protected abstract float shadow$getDamageAfterArmorAbsorb(DamageSource param0, float param1);
    @Shadow protected abstract float shadow$getDamageAfterMagicAbsorb(DamageSource param0, float param1);
    @Shadow public abstract void shadow$stopRiding();

    // @formatter:on

    @Nullable private ItemStack impl$activeItemStackCopy;
    @Nullable private Vector3d impl$preTeleportPosition;
    private int impl$deathEventsPosted;

    @Override
    public boolean bridge$damageEntity(final DamageSource damageSource, float damage) {
        if (this.shadow$isInvulnerableTo(damageSource)) {
            return false;
        }
        final boolean isHuman = (LivingEntity) (Object) this instanceof Player;
        // Sponge Start - Call platform hook for adjusting damage
        damage = this.bridge$applyModDamage((LivingEntity) (Object) this, damageSource, damage);
        // Sponge End
        final float originalDamage = damage;
        if (damage <= 0) {
            return false;
        }

        final List<DamageFunction> originalFunctions = new ArrayList<>();
        final Optional<DamageFunction> hardHatFunction =
                DamageEventUtil.createHardHatModifier((LivingEntity) (Object) this, damageSource);
        final Optional<DamageFunction> armorFunction =
                DamageEventUtil.createArmorModifiers((LivingEntity) (Object) this, damageSource);
        final Optional<DamageFunction> resistanceFunction =
                DamageEventUtil.createResistanceModifier((LivingEntity) (Object) this, damageSource);
        final Optional<List<DamageFunction>> armorEnchantments =
                DamageEventUtil.createEnchantmentModifiers((LivingEntity) (Object) this, damageSource);
        final Optional<DamageFunction> absorptionFunction =
                DamageEventUtil.createAbsorptionModifier((LivingEntity) (Object) this);
        final Optional<DamageFunction> shieldFunction =
                DamageEventUtil.createShieldFunction((LivingEntity) (Object) this, damageSource, damage);

        hardHatFunction.ifPresent(originalFunctions::add);

        shieldFunction.ifPresent(originalFunctions::add);

        armorFunction.ifPresent(originalFunctions::add);

        resistanceFunction.ifPresent(originalFunctions::add);

        armorEnchantments.ifPresent(originalFunctions::addAll);

        absorptionFunction.ifPresent(originalFunctions::add);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            DamageEventUtil.generateCauseFor(damageSource, frame);

            final DamageEntityEvent event = SpongeEventFactory
                    .createDamageEntityEvent(frame.currentCause(), (org.spongepowered.api.entity.Entity) this, originalFunctions,
                            originalDamage);
            if (damageSource
                    != SpongeDamageSources.IGNORED) { // Basically, don't throw an event if it's our own damage source
                SpongeCommon.post(event);
            }
            if (event.isCancelled()) {
                return false;
            }

            damage = (float) event.finalDamage();

            // Sponge Start - Allow the platform to adjust damage before applying armor/etc
            damage = this.bridge$applyModDamageBeforeFunctions((LivingEntity) (Object) this, damageSource, damage);
            // Sponge End

            // Helmet
            final ItemStack helmet = this.shadow$getItemBySlot(EquipmentSlot.HEAD);
            // We still sanity check if a mod is calling to damage the entity with an anvil or falling block
            // without using our mixin redirects in EntityFallingBlockMixin.
            if ((damageSource.getDirectEntity() instanceof FallingBlock || damageSource.is(DamageTypeTags.DAMAGES_HELMET)) && !helmet.isEmpty()) {
                helmet.hurtAndBreak((int) (event.baseDamage() * 4.0F + this.random.nextFloat() * event.baseDamage() * 2.0F),
                        (LivingEntity) (Object) this, EquipmentSlot.HEAD
                );
            }

            boolean hurtStack = false;
            // Shield
            if (shieldFunction.isPresent()) {
                this.shadow$hurtCurrentlyUsedShield((float) event.baseDamage());
                hurtStack = true;
                if (!damageSource.is(DamageTypeTags.IS_PROJECTILE)) {
                    final Entity entity = damageSource.getDirectEntity();

                    if (entity instanceof LivingEntity) {
                        this.shadow$blockUsingShield((LivingEntity) entity);
                    }
                }
            }

            // Armor
            if (!damageSource.is(DamageTypeTags.BYPASSES_ARMOR) && armorFunction.isPresent()) {
                this.shadow$hurtArmor(damageSource, (float) event.baseDamage());
                hurtStack = true;
            }

            // Sponge start - log inventory change due to taking damage
            if (hurtStack && isHuman) {
                PhaseTracker.SERVER.getPhaseContext().getTransactor().logPlayerInventoryChange((Player) (Object) this, PlayerInventoryTransaction.EventCreator.STANDARD);
                ((Player) (Object) this).inventoryMenu.broadcastChanges(); // capture
            }
            // Sponge end

            // Resistance modifier post calculation
            if (resistanceFunction.isPresent()) {
                final float f2 = (float) event.damage(resistanceFunction.get().modifier()) - damage;
                if (f2 > 0.0F && f2 < 3.4028235E37F) {
                    if (((LivingEntity) (Object) this) instanceof net.minecraft.server.level.ServerPlayer) {
                        ((net.minecraft.server.level.ServerPlayer) ((LivingEntity) (Object) this)).awardStat(Stats.DAMAGE_RESISTED, Math.round(f2 * 10.0F));
                    } else if (damageSource.getEntity() instanceof net.minecraft.server.level.ServerPlayer) {
                        ((net.minecraft.server.level.ServerPlayer) damageSource.getEntity()).awardStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(f2 * 10.0F));
                    }
                }
            }


            double absorptionModifier = absorptionFunction.map(function -> event.damage(function.modifier())).orElse(0d);
            if (absorptionFunction.isPresent()) {
                absorptionModifier = event.damage(absorptionFunction.get().modifier());

            }

            final float f = (float) event.finalDamage() - (float) absorptionModifier;
            this.shadow$setAbsorptionAmount(Math.max(this.shadow$getAbsorptionAmount() + (float) absorptionModifier, 0.0F));
            if (f > 0.0F && f < 3.4028235E37F && ((LivingEntity) (Object) this) instanceof net.minecraft.server.level.ServerPlayer) {
                ((Player) (Object) this).awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
            }
            if (damage != 0.0F) {
                if (isHuman) {
                    ((Player) (Object) this).causeFoodExhaustion(damageSource.getFoodExhaustion());
                }
                final float f2 = this.shadow$getHealth();

                this.shadow$setHealth(f2 - damage);
                this.shadow$getCombatTracker().recordDamage(damageSource, damage);

                if (isHuman) {
                    if (damage < 3.4028235E37F) {
                        ((Player) (Object) this).awardStat(Stats.DAMAGE_TAKEN, Math.round(damage * 10.0F));
                    }
                    return true;
                }

                this.shadow$setAbsorptionAmount(this.shadow$getAbsorptionAmount() - damage);
            }
            return true;
        }
    }

    /**
     * Due to cancelling death events, "healing" the entity is the only way to cancel the death, but we still
     * want to reset the death event counter. This is the simplest way to get it working with forge mods who
     * do not have access to Sponge's API.
     */
    @Inject(method = "setHealth",
        at = @At("HEAD"))
    private void impl$resetDeathEventCounter(final float health, final CallbackInfo info) {
        if (this.shadow$getHealth() <= 0 && health > 0) {
            this.impl$deathEventsPosted = 0;
        }
    }

    @Redirect(method = "dropExperience()V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getExperienceReward()I"))
    protected int impl$exposeGetExperienceForDeath(final LivingEntity entity) {
        return this.bridge$getExperiencePointsOnDeath(entity);
    }

    /**
     * @author bloodmc
     * @author zidane
     * @reason This shouldn't be used internally but a mod may still call it so we simply reroute to our hook.
     */
    @Overwrite
    protected void actuallyHurt(final DamageSource damageSource, final float damage) {
        this.bridge$damageEntity(damageSource, damage);
    }

    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void impl$throwDestructEntityDeath(final DamageSource cause, final CallbackInfo ci) {
        final boolean throwEvent = !((LevelBridge) this.shadow$level()).bridge$isFake() && Sponge.isServerAvailable() && Sponge.server().onMainThread();
        if (!this.dead) { // isDead should be set later on in this method so we aren't re-throwing the events.
            if (throwEvent && this.impl$deathEventsPosted <= Constants.Sponge.MAX_DEATH_EVENTS_BEFORE_GIVING_UP) {
                // ignore because some moron is not resetting the entity.
                this.impl$deathEventsPosted++;
                if (SpongeCommonEventFactory.callDestructEntityEventDeath((LivingEntity) (Object) this, cause).isCancelled()) {
                    // Since the forge event is cancellable
                    ci.cancel();
                }
            }
        } else {
            this.impl$deathEventsPosted = 0;
        }
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"),
            cancellable = true)
    private void impl$doNotSendStateForHumans(final DamageSource cause, final CallbackInfo ci) {
        if (((LivingEntity) (Object) this) instanceof HumanEntity) {
            ci.cancel();
        }
    }

    @Redirect(method = "dropAllDeathLoot",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;dropEquipment()V"
            )
    )
    private void tracker$dropInventory(final LivingEntity thisEntity) {
        if (thisEntity instanceof PlayerBridge && ((PlayerBridge) thisEntity).bridge$keepInventory()) {
            return;
        }
        this.shadow$dropEquipment();
    }

    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void impl$pushEntitiesIfNotVanished(final CallbackInfo ci) {
        if (this.bridge$vanishState().ignoresCollisions()) {
            ci.cancel();
        }
    }

    @Redirect(method = "triggerItemUseEffects",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;spawnItemParticles(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void impl$hideItemParticlesIfVanished(final LivingEntity livingEntity, final ItemStack stack, final int count) {
        if (this.bridge$vanishState().createsParticles()) {
            this.shadow$spawnItemParticles(stack, count);
        }
    }

    @Inject(method = "randomTeleport", at = @At("HEAD"))
    private void impl$snapshotPositionBeforeVanillaTeleportLogic(final double x, final double y, final double z, final boolean changeState,
                                                                 final CallbackInfoReturnable<Boolean> cir) {
        this.impl$preTeleportPosition = new Vector3d(this.shadow$getX(), this.shadow$getY(), this.shadow$getZ());
    }

    @Inject(method = "randomTeleport", at = @At(value = "RETURN", ordinal = 0, shift = At.Shift.BY, by = 2), cancellable = true)
    private void impl$callMoveEntityEventForTeleport(final double x, final double y, final double z, final boolean changeState,
                                                     final CallbackInfoReturnable<Boolean> cir) {
        if (!ShouldFire.MOVE_ENTITY_EVENT) {
            return;
        }

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);

            // ENTITY_TELEPORT is our fallback context
            if (!frame.currentContext().containsKey(EventContextKeys.MOVEMENT_TYPE)) {
                frame.addContext(EventContextKeys.MOVEMENT_TYPE, MovementTypes.ENTITY_TELEPORT);
            }

            final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(frame.currentCause(),
                    (org.spongepowered.api.entity.Entity) this, this.impl$preTeleportPosition, new Vector3d(this.shadow$getX(), this.shadow$getY(),
                            this.shadow$getZ()),
                    new Vector3d(x, y, z));

            if (SpongeCommon.post(event)) {
                this.shadow$teleportTo(this.impl$preTeleportPosition.x(), this.impl$preTeleportPosition.y(),
                        this.impl$preTeleportPosition.z());
                cir.setReturnValue(false);
                return;
            }

            this.shadow$teleportTo(event.destinationPosition().x(), event.destinationPosition().y(),
                    event.destinationPosition().z());
        }
    }

    /**
     * @author gabizou - January 4th, 2016
     * @reason This allows invisiblity to ignore entity collisions.
     */
    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void impl$ifVanishedDoNotPick(final CallbackInfoReturnable<Boolean> cir) {
        if (this.bridge$vanishState().ignoresCollisions()) {
            cir.setReturnValue(false);
        }
    }

    @Redirect(
        method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"
        )
    )
    private void impl$ignoreExperienceLevelSoundsWhileVanished(final net.minecraft.world.level.Level world,
        final net.minecraft.world.entity.player.Player player, final double x, final double y, final double z,
        final SoundEvent sound, final SoundSource category, final float volume, final float pitch
    ) {
        if (!this.bridge$vanishState().createsSounds()) {
            return;
        }
        world.playSound(player, x, y, z, sound, category, volume, pitch);
    }

    @Redirect(method = "checkFallDamage",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private <T extends ParticleOptions> int impl$vanishSpawnParticleForFallState(
            final ServerLevel serverLevel, final T options, final double xCoord, final double yCoord, final double zCoord, final int numberOfParticles,
            final double xOffset,
            final double yOffset,
            final double zOffset,
            final double particleSpeed
    ) {
        if (this.bridge$vanishState().createsParticles()) {
            return serverLevel.sendParticles(options, xCoord, yCoord, zCoord, numberOfParticles, xOffset, yOffset, zOffset, particleSpeed);
        }
        return 0;
    }

    @Inject(method = "broadcastBreakEvent(Lnet/minecraft/world/entity/EquipmentSlot;)V", at = @At("HEAD"), cancellable = true)
    private void impl$vanishDoesNotBroadcastBreakEvents(final EquipmentSlot slot, final CallbackInfo ci) {
        if (this.bridge$vanishState().invisible()) {
            ci.cancel();
        }
    }

    @Inject(method = "updatingUsingItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;stopUsingItem()V"))
    protected void impl$updateHealthForUseFinish(final CallbackInfo ci) {
    }

    // Data delegated methods

    // Start implementation of UseItemstackEvent

    @Inject(method = "startUsingItem",
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD,
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/LivingEntity;useItem:Lnet/minecraft/world/item/ItemStack;"))
    private void impl$onSetActiveItemStack(final InteractionHand hand, final CallbackInfo ci, final ItemStack stack) {
        if (this.shadow$level().isClientSide) {
            return;
        }

        final UseItemStackEvent.Start event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final HandType handType = (HandType) (Object) hand;
            this.impl$addSelfToFrame(frame, snapshot, handType);
            final Ticks useDuration = SpongeTicks.ticksOrInfinite(stack.getUseDuration());
            event = SpongeEventFactory.createUseItemStackEventStart(PhaseTracker.getCauseStackManager().currentCause(),
                useDuration, useDuration, snapshot);
        }

        if (SpongeCommon.post(event)) {
            ci.cancel();
        } else {
            this.useItemRemaining = SpongeTicks.toSaturatedIntOrInfinite(event.remainingDuration());
        }
    }

    @Redirect(method = "startUsingItem",
        at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/LivingEntity;useItemRemaining:I"))
    private void impl$getItemDuration(final LivingEntity this$0, final int count) {
        if (this.shadow$level().isClientSide) {
            this.useItemRemaining = count;
        }
        // If we're on the server, do nothing, since we already set this field on onSetActiveItemStack
    }

    // A helper method for firing UseItemStackEvent sub-events
    // This ensures that the cause and context for these events
    // always have OWNER and NOTIFIER set (if possible),
    // as well as USED_ITEM and USED_HAND
    private void impl$addSelfToFrame(final CauseStackManager.StackFrame frame, final ItemStackSnapshot snapshot, final HandType hand) {
        frame.addContext(EventContextKeys.USED_HAND, hand);
        this.impl$addSelfToFrame(frame, snapshot);
    }

    private void impl$addSelfToFrame(final CauseStackManager.StackFrame frame, final ItemStackSnapshot snapshot) {
        frame.pushCause(this);
        frame.addContext(EventContextKeys.USED_ITEM, snapshot);
        if (this instanceof ServerPlayer) {
            frame.addContext(EventContextKeys.CREATOR, ((ServerPlayer) this).uniqueId());
            frame.addContext(EventContextKeys.NOTIFIER, ((ServerPlayer) this).uniqueId());
        }
    }

    @Redirect(method = "updateUsingItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getUseItemRemainingTicks()I",
            ordinal = 0))
    private int impl$onGetRemainingItemDuration(final LivingEntity self) {
        if (this.shadow$level().isClientSide) {
            return self.getUseItemRemainingTicks();
        }

        final UseItemStackEvent.Tick event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.useItem);
            final HandType handType = (HandType) (Object) this.shadow$getUsedItemHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            final Ticks useItemRemainingTicks = SpongeTicks.ticksOrInfinite(this.useItemRemaining);
            event = SpongeEventFactory.createUseItemStackEventTick(PhaseTracker.getCauseStackManager().currentCause(),
                useItemRemainingTicks, useItemRemainingTicks, snapshot);
            SpongeCommon.post(event);
        }
        // Because the item usage will only finish if useItemRemaining == 0 and decrements it first, it should be >= 1
        this.useItemRemaining = event.remainingDuration().isInfinite()
                ? Constants.TickConversions.INFINITE_TICKS
                : Math.max(SpongeTicks.toSaturatedIntOrInfinite(event.remainingDuration()), 1);

        if (event.isCancelled()) {
            // Get prepared for some cool hacks: We're within the condition for updateItemUse
            // So if we don't want it to call the method we just pass a value that makes the
            // condition evaluate to false, so an integer >= 25
            return 26;
        }

        return this.shadow$getUseItemRemainingTicks();
    }

    @Inject(method = "updateUsingItem",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;useItemRemaining:I", opcode = Opcodes.PUTFIELD), cancellable = true)
    private void impl$dontReduceInfiniteRemainingItemDuration(final CallbackInfo ci) {
        if (this.useItemRemaining == Constants.TickConversions.INFINITE_TICKS) {
            ci.cancel();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "completeUsingItem",
        cancellable = true,
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;triggerItemUseEffects(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void impl$onUpdateItemUse(final CallbackInfo ci) {
        if (this.shadow$level().isClientSide) {
            return;
        }


        final UseItemStackEvent.Finish event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.useItem);
            final HandType handType = (HandType) (Object) this.shadow$getUsedItemHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            final Ticks useItemRemainingTicks = SpongeTicks.ticksOrInfinite(this.useItemRemaining);
            event = SpongeEventFactory.createUseItemStackEventFinish(PhaseTracker.getCauseStackManager().currentCause(),
                    useItemRemainingTicks, useItemRemainingTicks, snapshot);
        }
        SpongeCommon.post(event);
        if (event.remainingDuration().isInfinite() || event.remainingDuration().ticks() > 0) {
            this.useItemRemaining = SpongeTicks.toSaturatedIntOrInfinite(event.remainingDuration());
            ci.cancel();
        } else if (event.isCancelled()) {
            this.shadow$stopUsingItem();
            ci.cancel();
        } else {
            this.impl$activeItemStackCopy = this.useItem.copy();
        }
    }

    @Redirect(method = "completeUsingItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setItemInHand(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void impl$onSetHeldItem(final LivingEntity self, final InteractionHand hand, final ItemStack stack) {
        if (this.shadow$level().isClientSide) {
            self.setItemInHand(hand, stack);
            return;
        }

        // Unforunately, ItemFood calls ItemStack#shrink in Item#onItemUseFinish.
        // To ensure that we provide the original ItemStack in the event,
        // we make a copy of in our onUpdateItemUse redirect
        // If the event or transaction is cancelled, we make sure to explicitly
        // set the copy back in the player's hand, since it may have been already
        // modified if an ItemFood is being used.

        final ItemStackSnapshot activeItemStackSnapshot = ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy == null ? ItemStack.EMPTY : this.impl$activeItemStackCopy);


        final UseItemStackEvent.Replace event;
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack == null ? ItemStack.EMPTY : stack);
            final HandType handType = (HandType) (Object) hand;
            this.impl$addSelfToFrame(frame, activeItemStackSnapshot, handType);
            final Ticks useItemRemainingTicks = SpongeTicks.ticksOrInfinite(this.useItemRemaining);
            event = SpongeEventFactory.createUseItemStackEventReplace(PhaseTracker.getCauseStackManager().currentCause(),
                    useItemRemainingTicks, useItemRemainingTicks, activeItemStackSnapshot,
                new Transaction<>(ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy), snapshot));
        }

        if (SpongeCommon.post(event)) {
            this.shadow$setItemInHand(hand, this.impl$activeItemStackCopy.copy());
            return;
        }

        if (!event.itemStackResult().isValid()) {
            this.shadow$setItemInHand(hand, this.impl$activeItemStackCopy.copy());
            return;
        }

        this.shadow$setItemInHand(hand, ItemStackUtil.fromSnapshotToNative(event.itemStackResult().finalReplacement()));
    }

    @SuppressWarnings("ConstantConditions")
    @Redirect(method = "releaseUsingItem",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;releaseUsing(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"))
    // stopActiveHand
    private void impl$onStopPlayerUsing(final ItemStack stack, final net.minecraft.world.level.Level world, final LivingEntity self, final int duration) {
        if (this.shadow$level().isClientSide) {
            stack.releaseUsing(world, self, duration);
            return;
        }
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(stack);
            final HandType handType = (HandType) (Object) this.shadow$getUsedItemHand();
            this.impl$addSelfToFrame(frame, snapshot, handType);
            final Ticks ticksDuration = SpongeTicks.ticksOrInfinite(duration);
            if (!SpongeCommon.post(SpongeEventFactory.createUseItemStackEventStop(PhaseTracker.getCauseStackManager().currentCause(),
                ticksDuration, ticksDuration, snapshot))) {
                stack.releaseUsing(world, self, duration);
                if (self instanceof net.minecraft.server.level.ServerPlayer) {
                    // Log Change and capture SlotTransactions
                    PhaseTracker.SERVER.getPhaseContext().getTransactor().logPlayerInventoryChange(((net.minecraft.server.level.ServerPlayer) self), PlayerInventoryTransaction.EventCreator.STANDARD);
                    ((net.minecraft.server.level.ServerPlayer) self).inventoryMenu.broadcastChanges();
                }
            }
        }
    }

    @Inject(method = "stopUsingItem",
        at = @At("HEAD"))
    private void impl$onResetActiveHand(final CallbackInfo ci) {
        if (this.shadow$level().isClientSide) {
            return;
        }

        // If we finished using an item, impl$activeItemStackCopy will be non-null
        // However, if a player stopped using an item early, impl$activeItemStackCopy will not be set
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(this.impl$activeItemStackCopy != null ? this.impl$activeItemStackCopy : this.useItem);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            this.impl$addSelfToFrame(frame, snapshot);
            final Ticks useItemRemainingTicks = SpongeTicks.ticksOrInfinite(this.useItemRemaining);
            SpongeCommon.post(SpongeEventFactory.createUseItemStackEventReset(PhaseTracker.getCauseStackManager().currentCause(),
                    useItemRemainingTicks, useItemRemainingTicks, snapshot));
        }
        this.impl$activeItemStackCopy = null;
    }

    // End implementation of UseItemStackEvent

    @Inject(method = "canBeSeenAsEnemy", at = @At("HEAD"), cancellable = true)
    private void impl$makeVanishable(final CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof VanishableBridge
                && ((VanishableBridge) this).bridge$vanishState().untargetable()) {
            // Sponge: Take into account untargetability from vanishing
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canBeSeenByAnyone", at = @At("HEAD"), cancellable = true)
    private void impl$ifVanishedCantBeSeenByAnyone(final CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof VanishableBridge
            && ((VanishableBridge) this).bridge$vanishState().untargetable()) {
            // Sponge: Take into account untargetability from vanishing
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "stopSleeping", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;clearSleepingPos()V"))
    private void impl$callFinishSleepingEvent(final CallbackInfo ci) {
        if (this.shadow$level().isClientSide) {
            return;
        }

        final Optional<BlockPos> sleepingPos = this.shadow$getSleepingPos();
        if (!sleepingPos.isPresent()) {
            return;
        }
        final BlockSnapshot snapshot = ((ServerWorld) this.shadow$level()).createSnapshot(sleepingPos.get().getX(), sleepingPos.get().getY(), sleepingPos.get().getZ());
        final Cause currentCause = Sponge.server().causeStackManager().currentCause();
        final ServerLocation loc = ServerLocation.of((ServerWorld) this.shadow$level(), VecHelper.toVector3d(this.shadow$position()));
        final Vector3d rot = ((Living) this).rotation();
        final SleepingEvent.Finish event = SpongeEventFactory.createSleepingEventFinish(currentCause, loc, loc, rot, rot, snapshot, (Living) this);
        Sponge.eventManager().post(event);
        this.shadow$clearSleepingPos();
        if (event.toLocation().world() != this.shadow$level()) {
            throw new UnsupportedOperationException("World change is not supported here.");
        }
        this.shadow$setPos(event.toLocation().x(), event.toLocation().y(), event.toLocation().z());
        ((Living) this).setRotation(event.toRotation());
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;recheckStatus()V", shift = At.Shift.AFTER))
    private void impl$clearLastDamageSource(final CallbackInfo ci) {
        //Fix for MC-270896 - Players leak the last entity they took damage from
        if (this.lastDamageSource != null && this.shadow$level().getGameTime() - this.lastDamageStamp > 40L) {
            this.lastDamageSource = null;
        }
    }

}
