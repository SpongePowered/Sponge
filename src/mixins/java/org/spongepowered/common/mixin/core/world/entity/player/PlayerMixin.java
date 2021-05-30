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
package org.spongepowered.common.mixin.core.world.entity.player;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagContainer;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player.BedSleepingProblem;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.ModifierFunction;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.authlib.GameProfileHolderBridge;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerBridge;
import org.spongepowered.common.bridge.server.level.ServerLevelBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.cause.entity.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.world.entity.LivingEntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.ExperienceHolderUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(net.minecraft.world.entity.player.Player.class)
public abstract class PlayerMixin extends LivingEntityMixin implements PlayerBridge, GameProfileHolderBridge {

    // @formatter: off
    @Shadow @Final protected static EntityDataAccessor<Byte> DATA_PLAYER_MODE_CUSTOMISATION;
    @Shadow public int experienceLevel;
    @Shadow public int totalExperience;
    @Shadow public float experienceProgress;
    @Shadow @Final public Abilities abilities;
    @Shadow @Final public net.minecraft.world.entity.player.Inventory inventory;
    @Shadow public AbstractContainerMenu containerMenu;
    @Shadow @Final public InventoryMenu inventoryMenu;
    @Shadow @Final private GameProfile gameProfile;

    @Shadow public abstract boolean shadow$isSpectator();
    @Shadow public abstract int shadow$getXpNeededForNextLevel();
    @Shadow @Nullable public abstract ItemEntity shadow$drop(final ItemStack droppedItem, final boolean dropAround, final boolean traceItem);
    @Shadow public abstract FoodData shadow$getFoodData();
    @Shadow public abstract GameProfile shadow$getGameProfile();
    @Shadow public abstract Scoreboard shadow$getScoreboard();
    @Shadow public abstract boolean shadow$isCreative();
    @Shadow public boolean shadow$canHarmPlayer(final net.minecraft.world.entity.player.Player other) {
        return false;
    }
    @Shadow public abstract String shadow$getScoreboardName();
    @Shadow public abstract float shadow$getSpeed();
    @Shadow public abstract void shadow$resetAttackStrengthTicker();
    @Shadow public abstract float shadow$getAttackStrengthScale(float adjustTicks);
    @Shadow public abstract void shadow$sweepAttack();
    @Shadow public abstract void shadow$crit(Entity entityHit);
    @Shadow public abstract void shadow$magicCrit(Entity entityHit);
    @Shadow public abstract void shadow$awardStat(Stat<?> stat);
    @Shadow public abstract void shadow$awardStat(ResourceLocation stat, int amount);
    @Shadow public abstract void shadow$causeFoodExhaustion(float exhaustion);
    @Shadow public abstract Component shadow$getDisplayName();
    @Shadow protected abstract void shadow$removeEntitiesOnShoulder();
    @Shadow public abstract void shadow$awardStat(ResourceLocation stat);
    @Shadow public Either<BedSleepingProblem, Unit> shadow$startSleepInBed(BlockPos param0) {
        return null; // Shadowed
    }
    // @formatter: on

    private boolean impl$affectsSpawning = true;
    private boolean impl$shouldRestoreInventory = false;
    protected final boolean impl$isFake = ((PlatformEntityBridge) (net.minecraft.world.entity.player.Player) (Object) this).bridge$isFakePlayer();

    @Override
    public boolean bridge$affectsSpawning() {
        return this.impl$affectsSpawning && !this.shadow$isSpectator() && !this.bridge$isVanishPreventsTargeting();
    }

    @Override
    public void bridge$setAffectsSpawning(final boolean affectsSpawning) {
        this.impl$affectsSpawning = affectsSpawning;
    }

    @Override
    public void bridge$shouldRestoreInventory(final boolean restore) {
        this.impl$shouldRestoreInventory = restore;
    }

    @Override
    public boolean bridge$shouldRestoreInventory() {
        return this.impl$shouldRestoreInventory;
    }

    @Override
    public GameProfile bridge$getGameProfile() {
        return this.gameProfile;
    }

    @Override
    public int bridge$getExperienceSinceLevel() {
        return this.totalExperience - ExperienceHolderUtil.xpAtLevel(this.experienceLevel);
    }

    @Override
    public void bridge$setExperienceSinceLevel(final int experience) {
        this.totalExperience = ExperienceHolderUtil.xpAtLevel(this.experienceLevel) + experience;
        this.experienceProgress = (float) experience / this.shadow$getXpNeededForNextLevel();
    }

    /*
    @Redirect(method = "livingTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;"))
    private List<Entity> impl$ignoreOtherEntitiesWhenUncollideable(final World world, Entity entityIn, AxisAlignedBB bb) {
        if (this.bridge$isUncollideable()) {
            return Collections.emptyList();
        }
        return world.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
    }
*/

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSleeping()Z"))
    private boolean impl$postSleepingEvent(final net.minecraft.world.entity.player.Player self) {
        if (self.isSleeping()) {
            if (!((WorldBridge) this.level).bridge$isFake()) {
                final CauseStackManager csm = PhaseTracker.getCauseStackManager();
                csm.pushCause(this);
                final BlockPos bedLocation = this.shadow$getSleepingPos().get();
                final BlockSnapshot snapshot = ((ServerWorld) this.level).createSnapshot(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
                SpongeCommon.postEvent(SpongeEventFactory.createSleepingEventTick(csm.currentCause(), snapshot, (Living) this));
                csm.popCause();
            }
            return true;
        }
        return false;
    }

    @Redirect(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getName()Lnet/minecraft/network/chat/Component;"))
    private Component impl$useCustomNameIfSet(final net.minecraft.world.entity.player.Player playerEntity) {
        if (playerEntity instanceof ServerPlayer) {
            if (playerEntity.hasCustomName()) {
                return playerEntity.getCustomName();
            }

            return playerEntity.getName();
        }

        return playerEntity.getName();
    }

    @Redirect(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void impl$playNoSoundToOthersIfVanished(final Level world, final net.minecraft.world.entity.player.Player player, final double x, final double y, final double z,
            final SoundEvent sound, final SoundSource category, final float volume, final float pitch) {
        if (!this.bridge$isVanished()) {
            this.level.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @Redirect(method = "canUseGameMasterBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getPermissionLevel()I"))
    private int impl$checkPermissionForCommandBlock(final net.minecraft.world.entity.player.Player playerEntity) {
        if (this instanceof Subject) {
            return ((Subject) this).hasPermission(Constants.Permissions.COMMAND_BLOCK_PERMISSION) ? Constants.Permissions.COMMAND_BLOCK_LEVEL : 0;
        } else {
            return this.shadow$getPermissionLevel();
        }
    }

    /**
     * @author gabizou - September 4th, 2018
     * @author i509VCB - February 17th, 2020 - 1.14.4
     * @reason Bucket placement and other placements can be "detected"
     * for pre change events prior to them actually processing their logic,
     * this in effect can prevent item duplication issues when the block
     * changes are cancelled, but inventory is already modified. It would
     * be considered that during interaction packets, inventory is monitored,
     * however, sometimes that isn't enough.
     * @return Check if the player is a fake player, if it is, then just do
     *  the same return, otherwise, throw an event first and then return if the
     *  event is cancelled, or the stack.canPlaceOn
     */
    @Redirect(method = "mayUseItemAt",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;hasAdventureModePlaceTagForBlock(Lnet/minecraft/tags/TagContainer;Lnet/minecraft/world/level/block/state/pattern/BlockInWorld;)Z"))
    private boolean impl$callChangeBlockPre(final ItemStack stack, final TagContainer tagSupplier, final BlockInWorld cachedBlockInfo) {
        // Lazy evaluation, if the stack isn't placeable anyways, might as well not
        // call the logic.
        if (!stack.hasAdventureModePlaceTagForBlock(tagSupplier, cachedBlockInfo)) {
            return false;
        }
        // If we're going to throw an event, then do it.
        // Just sanity checks, if the player is not in a managed world, then don't bother either.
        // some fake players may exist in pseudo worlds as well, which means we don't want to
        // process on them since the world is not a valid world to plugins.
        if (this.level instanceof WorldBridge && !((WorldBridge) this.level).bridge$isFake() && ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            // Note that this can potentially cause phase contexts to auto populate frames
            // we shouldn't rely so much on them, but sometimes the extra information is provided
            // through this method.
            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
                // Go ahead and add the item stack in use, just in the event the current phase contexts don't provide
                // that information.
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(stack));
                // Then go ahead and call the event and return if it was cancelled
                // if it was cancelled, then there should be no changes needed to roll back
                return !SpongeCommonEventFactory.callChangeBlockEventPre((ServerLevelBridge) this.level, cachedBlockInfo.getPos(), this).isCancelled();
            }
        }
        // Otherwise, if all else is ignored, or we're not throwing events, we're just going to return the
        // default value: true.
        return true;
    }

    /**
     * @author gabizou - June 13th, 2016
     * @author zidane - November 21st, 2020
     * @reason Reverts the method to flow through our systems, Forge patches
     * this to throw an ItemTossEvent, but we'll be throwing it regardless in
     * SpongeForge's handling.
     */
    @Overwrite
    @Nullable
    public ItemEntity drop(final ItemStack itemStackIn, final boolean unused) {
        return this.shadow$drop(itemStackIn, false, false);
    }

    @Inject(method = "getFireImmuneTicks", at = @At(value = "HEAD"), cancellable = true)
    private void impl$useCustomFireImmuneTicks(final CallbackInfoReturnable<Integer> ci) {
        if (this.impl$hasCustomFireImmuneTicks) {
            ci.setReturnValue((int) this.impl$fireImmuneTicks);
        }
    }

    @Inject(method = "interactOn", at = @At(value = "HEAD"), cancellable = true)
    public void impl$onRightClickEntity(final Entity entityToInteractOn, final InteractionHand hand, final CallbackInfoReturnable<InteractionResult> cir) {
        if (!((net.minecraft.world.entity.player.Player) (Object) this instanceof ServerPlayer)) {
            return;
        }

        final InteractEntityEvent.Secondary event = SpongeCommonEventFactory.callInteractEntityEventSecondary((ServerPlayer) (Object) this,
                this.shadow$getItemInHand(hand), entityToInteractOn, hand, null);
        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    /**
     * @author gabizou - April 8th, 2016
     * @author gabizou - April 11th, 2016 - Update for 1.9 - This enitre method was rewritten
     * @author i509VCB - February 15th, 2020 - Update for 1.14.4
     * @author gabizou - November 15th, 2020 - Update for 1.15.2
     *
     * @reason Rewrites the attack to throw an {@link AttackEntityEvent} prior
     * to the ensuing {@link org.spongepowered.api.event.entity.DamageEntityEvent}. This should cover all cases where players are
     * attacking entities and those entities override {@link LivingEntity#hurt(DamageSource, float)}
     * and effectively bypass our damage event hooks.
     *
     * LVT Rename Table:
     * float f        | damage               |
     * float f1       | enchantmentDamage    |
     * float f2       | attackStrength       |
     * boolean flag   | isStrongAttack       |
     * boolean flag1  | isSprintingAttack    |
     * boolean flag2  | isCriticalAttack     | Whether critical particles will spawn and of course, multiply the output damage
     * boolean flag3  | isSweapingAttack     | Whether the player is sweaping an attack and will deal AoE damage
     * int i          | knockbackModifier    | The knockback modifier, must be set from the event after it has been thrown
     * float f4       | targetOriginalHealth | This is initially set as the entity original health
     * boolean flag4  | litEntityOnFire      | This is an internal flag to where if the attack failed, the entity is no longer set on fire
     * int j          | fireAspectModifier   | Literally just to check that the weapon used has fire aspect enchantments
     * double d0      | distanceWalkedDelta  | This checks that the distance walked delta is more than the normal walking speed to evaluate if you're making a sweaping attack
     * double d1      | targetMotionX        | Current target entity motion x vector
     * double d2      | targetMotionY        | Current target entity motion y vector
     * double d3      | targetMotionZ        | Current target entity motion z vector
     * boolean flag5  | attackSucceeded      | Whether the attack event succeeded
     *
     * @param targetEntity The target entity
     */
    @Overwrite
    public void attack(final Entity targetEntity) {
        // Sponge Start - Add SpongeImpl hook to override in forge as necessary
        if (!PlatformHooks.INSTANCE.getEntityHooks().checkAttackEntity((net.minecraft.world.entity.player.Player) (Object) this, targetEntity)) {
            return;
        }
        // Sponge End
        if (targetEntity.isAttackable()) {
            if (!targetEntity.skipAttackInteraction((net.minecraft.world.entity.player.Player) (Object) this)) {
                // Sponge Start - Prepare our event values
                // float damage = (float) this.getEntityAttribute(Attributes.ATTACK_DAMAGE).getAttributeValue();
                final double originalBaseDamage = this.shadow$getAttribute(Attributes.ATTACK_DAMAGE).getValue();
                float damage = (float) originalBaseDamage;
                // Sponge End
                float enchantmentDamage = 0.0F;

                // Sponge Start - Redirect getting enchantments for our damage event handlers
                // if (targetEntity instanceof LivingEntity) {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((LivingEntity) targetEntity).getCreatureAttribute());
                // } else {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
                // }
                final float attackStrength = this.shadow$getAttackStrengthScale(0.5F);

                final List<ModifierFunction<DamageModifier>> originalFunctions = new ArrayList<>();

                final MobType creatureAttribute = targetEntity instanceof LivingEntity
                    ? ((LivingEntity) targetEntity).getMobType()
                    : MobType.UNDEFINED;
                final List<DamageFunction> enchantmentModifierFunctions = DamageEventHandler.createAttackEnchantmentFunction(this.shadow$getMainHandItem(), creatureAttribute, attackStrength);
                // This is kept for the post-damage event handling
                final List<DamageModifier> enchantmentModifiers = enchantmentModifierFunctions.stream()
                    .map(ModifierFunction::modifier)
                    .collect(Collectors.toList());

                enchantmentDamage = (float) enchantmentModifierFunctions.stream()
                    .map(ModifierFunction::function)
                    .mapToDouble(function -> function.applyAsDouble(originalBaseDamage))
                    .sum();
                originalFunctions.addAll(enchantmentModifierFunctions);
                // Sponge End

                originalFunctions.add(DamageEventHandler.provideCooldownAttackStrengthFunction((net.minecraft.world.entity.player.Player) (Object) this, attackStrength));
                damage = damage * (0.2F + attackStrength * attackStrength * 0.8F);
                enchantmentDamage = enchantmentDamage * attackStrength;
                this.shadow$resetAttackStrengthTicker();

                if (damage > 0.0F || enchantmentDamage > 0.0F) {
                    final boolean isStrongAttack = attackStrength > 0.9F;
                    boolean isSprintingAttack = false;
                    boolean isCriticalAttack = false;
                    boolean isSweapingAttack = false;
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackBonus((net.minecraft.world.entity.player.Player) (Object) this);

                    if (this.shadow$isSprinting() && isStrongAttack) {
                        // Sponge - Only play sound after the event has be thrown and not cancelled.
                        // this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.entity_player_attack_knockback, this.getSoundCategory(), 1.0F, 1.0F);
                        ++knockbackModifier;
                        isSprintingAttack = true;
                    }

                    isCriticalAttack = isStrongAttack && this.fallDistance > 0.0F && !this.onGround && !this.shadow$onClimbable() && !this.shadow$isInWater() && !this.shadow$hasEffect(MobEffects.BLINDNESS) && !this.shadow$isPassenger() && targetEntity instanceof LivingEntity;
                    isCriticalAttack = isCriticalAttack && !this.shadow$isSprinting();

                    if (isCriticalAttack) {
                        // Sponge Start - add critical attack tuple
                        // damage *= 1.5F; // Sponge - This is handled in the event
                        originalFunctions.add(DamageEventHandler.provideCriticalAttackTuple((net.minecraft.world.entity.player.Player) (Object) this));
                        // Sponge End
                    }

                    // damage = damage + enchantmentDamage; // Sponge - We don't need this since our event will re-assign the damage to deal
                    final double distanceWalkedDelta = (double) (this.walkDist - this.walkDistO);

                    final ItemStack heldItem = this.shadow$getMainHandItem();
                    if (isStrongAttack && !isCriticalAttack && !isSprintingAttack && this.onGround && distanceWalkedDelta < (double) this.shadow$getSpeed()) {
                        final ItemStack itemstack = heldItem;

                        if (itemstack.getItem() instanceof SwordItem) {
                            isSweapingAttack = true;
                        }
                    }

                    // Sponge Start - Create the event and throw it
                    final DamageSource damageSource = DamageSource.playerAttack((net.minecraft.world.entity.player.Player) (Object) this);
                    final boolean isMainthread = !this.level.isClientSide;
                    if (isMainthread) {
                        PhaseTracker.getInstance().pushCause(damageSource);
                    }
                    final Cause currentCause = isMainthread ? PhaseTracker.getInstance().currentCause() : Cause.of(EventContext.empty(), damageSource);
                    final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(currentCause, (org.spongepowered.api.entity.Entity) targetEntity,
                        originalFunctions, knockbackModifier, originalBaseDamage);
                    SpongeCommon.postEvent(event);
                    if (isMainthread) {
                        PhaseTracker.getInstance().popCause();
                    }
                    if (event.isCancelled()) {
                        return;
                    }

                    damage = (float) event.finalOutputDamage();
                    // Sponge End

                    // Sponge Start - need final for later events
                    final double attackDamage = damage;
                    knockbackModifier = (int) event.knockbackModifier();
                    enchantmentDamage = (float) enchantmentModifiers.stream()
                        .mapToDouble(event::outputDamage)
                        .sum();
                    // Sponge End

                    float targetOriginalHealth = 0.0F;
                    boolean litEntityOnFire = false;
                    final int fireAspectModifier = EnchantmentHelper.getFireAspect((net.minecraft.world.entity.player.Player) (Object) this);

                    if (targetEntity instanceof LivingEntity) {
                        targetOriginalHealth = ((LivingEntity) targetEntity).getHealth();

                        if (fireAspectModifier > 0 && !targetEntity.isOnFire()) {
                            litEntityOnFire = true;
                            targetEntity.setSecondsOnFire(1);
                        }
                    }

                    final net.minecraft.world.phys.Vec3 targetMotion = targetEntity.getDeltaMovement();
                    final boolean attackSucceeded = targetEntity.hurt(DamageSource.playerAttack((net.minecraft.world.entity.player.Player) (Object) this), damage);

                    if (attackSucceeded) {
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity) targetEntity).knockback((float) knockbackModifier * 0.5F, (double) Mth
                                    .sin(this.yRot * 0.017453292F), (double) (-Mth.cos(this.yRot * 0.017453292F)));
                            } else {
                                targetEntity.push((double) (-Mth.sin(this.yRot * 0.017453292F) * (float) knockbackModifier * 0.5F), 0.1D, (double) (Mth.cos(this.yRot
                                        * 0.017453292F) * (float) knockbackModifier * 0.5F));
                            }

                            this.shadow$setDeltaMovement(this.shadow$getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                            this.shadow$setSprinting(false);
                        }

                        if (isSweapingAttack) {
                            for (final LivingEntity livingEntity : this.level
                                    .getEntitiesOfClass(LivingEntity.class, targetEntity.getBoundingBox().inflate(1.0D, 0.25D, 1.0D))) {
                                if (livingEntity != (net.minecraft.world.entity.player.Player) (Object) this && livingEntity != targetEntity && !this.shadow$isAlliedTo(livingEntity) && (!(livingEntity instanceof ArmorStand) || !((ArmorStand)livingEntity).isMarker()) && this.shadow$distanceToSqr(livingEntity) < 9.0D) {
                                    // Sponge Start - Do a small event for these entities
                                    // livingEntity.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                                    // livingEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), 1.0F);
                                    final EntityDamageSource sweepingAttackSource = EntityDamageSource.builder()
                                        .entity((Player) this)
                                        .type(DamageTypes.SWEEPING_ATTACK)
                                        .build();
                                    try (final CauseStackManager.StackFrame frame = isMainthread ? PhaseTracker.getInstance().pushCauseFrame() : null) {
                                        if (isMainthread) {
                                            frame.pushCause(sweepingAttackSource);
                                        }
                                        final ItemStackSnapshot heldSnapshot = ItemStackUtil.snapshotOf(heldItem);
                                        if (isMainthread) {
                                            frame.addContext(EventContextKeys.WEAPON, heldSnapshot);
                                        }
                                        final DamageFunction sweapingFunction = DamageFunction.of(DamageModifier.builder()
                                                .cause(Cause.of(EventContext.empty(), heldSnapshot))
                                                .item(heldSnapshot)
                                                .type(DamageModifierTypes.SWEEPING)
                                                .build(),
                                            incoming -> EnchantmentHelper.getSweepingDamageRatio((net.minecraft.world.entity.player.Player) (Object) this) * attackDamage);
                                        final List<DamageFunction> sweapingFunctions = new ArrayList<>();
                                        sweapingFunctions.add(sweapingFunction);
                                        final AttackEntityEvent sweepingAttackEvent = SpongeEventFactory.createAttackEntityEvent(
                                            currentCause, (org.spongepowered.api.entity.Entity) livingEntity,
                                            sweapingFunctions, 1, 1.0D);
                                        SpongeCommon.postEvent(sweepingAttackEvent);
                                        if (!sweepingAttackEvent.isCancelled()) {
                                            livingEntity.knockback(sweepingAttackEvent.knockbackModifier() * 0.4F,
                                                    (double) Mth.sin(this.yRot * ((float)Math.PI / 180F)),
                                                    (double) -Mth.cos(this.yRot * ((float)Math.PI / 180F)));

                                            livingEntity.hurt(DamageSource.playerAttack((net.minecraft.world.entity.player.Player) (Object) this),
                                                (float) sweepingAttackEvent.finalOutputDamage());
                                        }
                                    }
                                    // Sponge End
                                }
                            }

                            this.level.playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.shadow$getSoundSource(), 1.0F, 1.0F);
                            this.shadow$sweepAttack();
                        }

                        if (targetEntity instanceof ServerPlayer && targetEntity.hurtMarked) {
                            ((ServerPlayer) targetEntity).connection.send(new ClientboundSetEntityMotionPacket(targetEntity));
                            targetEntity.hurtMarked = false;
                            targetEntity.setDeltaMovement(targetMotion);
                        }

                        if (isCriticalAttack) {
                            this.level.playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_CRIT, this.shadow$getSoundSource(), 1.0F, 1.0F);
                            this.shadow$crit(targetEntity);
                        }

                        if (!isCriticalAttack && !isSweapingAttack) {
                            if (isStrongAttack) {
                                this.level.playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_STRONG, this.shadow$getSoundSource(), 1.0F, 1.0F);
                            } else {
                                this.level.playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_WEAK , this.shadow$getSoundSource(), 1.0F, 1.0F);
                            }
                        }

                        if (enchantmentDamage > 0.0F) {
                            this.shadow$magicCrit(targetEntity);
                        }

                        this.shadow$setLastHurtMob(targetEntity);

                        if (targetEntity instanceof LivingEntity) {
                            EnchantmentHelper.doPostHurtEffects((LivingEntity) targetEntity, (net.minecraft.world.entity.player.Player) (Object) this);
                        }

                        EnchantmentHelper.doPostDamageEffects((net.minecraft.world.entity.player.Player) (Object) this, targetEntity);
                        final ItemStack itemstack1 = this.shadow$getMainHandItem();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof EnderDragonPart) {
                            entity = ((EnderDragonPart) targetEntity).parentMob;
                        }

                        if(!this.level.isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                            itemstack1.hurtEnemy((LivingEntity) entity, (net.minecraft.world.entity.player.Player) (Object) this);
                            if(itemstack1.isEmpty()) {
                                this.shadow$setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (targetEntity instanceof LivingEntity) {
                            final float f5 = targetOriginalHealth - ((LivingEntity) targetEntity).getHealth();
                            this.shadow$awardStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (fireAspectModifier > 0) {
                                targetEntity.setSecondsOnFire(fireAspectModifier * 4);
                            }

                            if (this.level instanceof ServerWorld && f5 > 2.0F) {
                                final int k = (int) ((double) f5 * 0.5D);
                                ((net.minecraft.server.level.ServerLevel) this.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getX(), targetEntity.getY() + (double) (targetEntity.getBbHeight() * 0.5F), targetEntity.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        this.shadow$causeFoodExhaustion(0.1F);
                    } else {
                        this.level.playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE, this.shadow$getSoundSource(), 1.0F, 1.0F);

                        if (litEntityOnFire) {
                            targetEntity.clearFire();
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean impl$canCallIgniteEntityEvent() {
        return !this.shadow$isSpectator() && !this.shadow$isCreative();
    }
}
