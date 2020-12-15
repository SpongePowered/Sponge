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
package org.spongepowered.common.mixin.core.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.Humanoid;
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
import org.spongepowered.common.bridge.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.cause.entity.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.LivingEntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.ExperienceHolderUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements PlayerEntityBridge, GameProfileHolderBridge {

    // @formatter: off
    @Shadow @Final protected static DataParameter<Byte> DATA_PLAYER_MODE_CUSTOMISATION;
    @Shadow public int experienceLevel;
    @Shadow public int totalExperience;
    @Shadow public float experienceProgress;
    @Shadow @Final public PlayerAbilities abilities;
    @Shadow @Final public net.minecraft.entity.player.PlayerInventory inventory;
    @Shadow public Container containerMenu;
    @Shadow @Final public PlayerContainer inventoryMenu;
    @Shadow @Final private GameProfile gameProfile;

    @Shadow public abstract boolean shadow$isSpectator();
    @Shadow public abstract int shadow$getXpNeededForNextLevel();
    @Shadow @Nullable public abstract ItemEntity shadow$drop(final ItemStack droppedItem, final boolean dropAround, final boolean traceItem);
    @Shadow public abstract FoodStats shadow$getFoodData();
    @Shadow public abstract GameProfile shadow$getGameProfile();
    @Shadow public abstract Scoreboard shadow$getScoreboard();
    @Shadow public abstract boolean shadow$isCreative();
    @Shadow public boolean shadow$canHarmPlayer(final PlayerEntity other) {
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
    @Shadow public abstract ITextComponent shadow$getDisplayName();
    @Shadow protected abstract void shadow$removeEntitiesOnShoulder();
    @Shadow public abstract void shadow$awardStat(ResourceLocation stat);
    // @formatter: on

    private boolean impl$affectsSpawning = true;
    private boolean impl$shouldRestoreInventory = false;
    protected final boolean impl$isFake = ((PlatformEntityBridge) (PlayerEntity) (Object) this).bridge$isFakePlayer();

    @Override
    public boolean bridge$affectsSpawning() {
        return this.impl$affectsSpawning && !this.shadow$isSpectator() && !this.bridge$isUntargetable();
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

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSleeping()Z"))
    private boolean impl$postSleepingEvent(final PlayerEntity self) {
        if (self.isSleeping()) {
            if (!((WorldBridge) this.level).bridge$isFake()) {
                final CauseStackManager csm = PhaseTracker.getCauseStackManager();
                csm.pushCause(this);
                final BlockPos bedLocation = this.shadow$getSleepingPos().get();
                final BlockSnapshot snapshot = ((ServerWorld) this.level).createSnapshot(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
                SpongeCommon.postEvent(SpongeEventFactory.createSleepingEventTick(csm.getCurrentCause(), snapshot, (Humanoid) this));
                csm.popCause();
            }
            return true;
        }
        return false;
    }

    @Redirect(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getName()Lnet/minecraft/util/text/ITextComponent;"))
    private ITextComponent impl$useCustomNameIfSet(final PlayerEntity playerEntity) {
        if (playerEntity instanceof ServerPlayerEntity) {
            if (playerEntity.hasCustomName()) {
                return playerEntity.getCustomName();
            }

            return playerEntity.getName();
        }

        return playerEntity.getName();
    }

    @Redirect(method = "playSound(Lnet/minecraft/util/SoundEvent;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"))
    private void impl$playNoSoundToOthersIfVanished(final World world, final PlayerEntity player, final double x, final double y, final double z,
            final SoundEvent sound, final SoundCategory category, final float volume, final float pitch) {
        if (!this.bridge$isVanished()) {
            this.level.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @Redirect(method = "canUseGameMasterBlocks", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getPermissionLevel()I"))
    private int impl$checkPermissionForCommandBlock(final PlayerEntity playerEntity) {
        return ((Subject) this).hasPermission(Constants.Permissions.COMMAND_BLOCK_PERMISSION) ? Constants.Permissions.COMMAND_BLOCK_LEVEL : 0;
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
            target = "Lnet/minecraft/item/ItemStack;hasAdventureModePlaceTagForBlock(Lnet/minecraft/tags/ITagCollectionSupplier;Lnet/minecraft/util/CachedBlockInfo;)Z"))
    private boolean impl$callChangeBlockPre(final ItemStack stack, final ITagCollectionSupplier tagSupplier, final CachedBlockInfo cachedBlockInfo) {
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
                return !SpongeCommonEventFactory.callChangeBlockEventPre((ServerWorldBridge) this.level, cachedBlockInfo.getPos(), this).isCancelled();
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
    public void impl$onRightClickEntity(final Entity entityToInteractOn, final Hand hand, final CallbackInfoReturnable<ActionResultType> cir) {
        if (!((PlayerEntity) (Object) this instanceof ServerPlayerEntity)) {
            return;
        }

        final InteractEntityEvent.Secondary event = SpongeCommonEventFactory.callInteractEntityEventSecondary((ServerPlayerEntity) (Object) this,
                this.shadow$getItemInHand(hand), entityToInteractOn, hand, null);
        if (event.isCancelled()) {
            cir.setReturnValue(ActionResultType.FAIL);
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
        if (!PlatformHooks.getInstance().getEntityHooks().checkAttackEntity((PlayerEntity) (Object) this, targetEntity)) {
            return;
        }
        // Sponge End
        if (targetEntity.isAttackable()) {
            if (!targetEntity.skipAttackInteraction((PlayerEntity) (Object) this)) {
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

                final CreatureAttribute creatureAttribute = targetEntity instanceof LivingEntity
                    ? ((LivingEntity) targetEntity).getMobType()
                    : CreatureAttribute.UNDEFINED;
                final List<DamageFunction> enchantmentModifierFunctions = DamageEventHandler.createAttackEnchantmentFunction(this.shadow$getMainHandItem(), creatureAttribute, attackStrength);
                // This is kept for the post-damage event handling
                final List<DamageModifier> enchantmentModifiers = enchantmentModifierFunctions.stream()
                    .map(ModifierFunction::getModifier)
                    .collect(Collectors.toList());

                enchantmentDamage = (float) enchantmentModifierFunctions.stream()
                    .map(ModifierFunction::getFunction)
                    .mapToDouble(function -> function.applyAsDouble(originalBaseDamage))
                    .sum();
                originalFunctions.addAll(enchantmentModifierFunctions);
                // Sponge End

                originalFunctions.add(DamageEventHandler.provideCooldownAttackStrengthFunction((PlayerEntity) (Object) this, attackStrength));
                damage = damage * (0.2F + attackStrength * attackStrength * 0.8F);
                enchantmentDamage = enchantmentDamage * attackStrength;
                this.shadow$resetAttackStrengthTicker();

                if (damage > 0.0F || enchantmentDamage > 0.0F) {
                    final boolean isStrongAttack = attackStrength > 0.9F;
                    boolean isSprintingAttack = false;
                    boolean isCriticalAttack = false;
                    boolean isSweapingAttack = false;
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackBonus((PlayerEntity) (Object) this);

                    if (this.shadow$isSprinting() && isStrongAttack) {
                        // Sponge - Only play sound after the event has be thrown and not cancelled.
                        // this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.entity_player_attack_knockback, this.getSoundCategory(), 1.0F, 1.0F);
                        ++knockbackModifier;
                        isSprintingAttack = true;
                    }

                    isCriticalAttack = isStrongAttack && this.fallDistance > 0.0F && !this.onGround && !this.shadow$onClimbable() && !this.shadow$isInWater() && !this.shadow$hasEffect(Effects.BLINDNESS) && !this.shadow$isPassenger() && targetEntity instanceof LivingEntity;
                    isCriticalAttack = isCriticalAttack && !this.shadow$isSprinting();

                    if (isCriticalAttack) {
                        // Sponge Start - add critical attack tuple
                        // damage *= 1.5F; // Sponge - This is handled in the event
                        originalFunctions.add(DamageEventHandler.provideCriticalAttackTuple((PlayerEntity) (Object) this));
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
                    final DamageSource damageSource = DamageSource.playerAttack((PlayerEntity) (Object) this);
                    final boolean isMainthread = !this.level.isClientSide;
                    if (isMainthread) {
                        PhaseTracker.getInstance().pushCause(damageSource);
                    }
                    final Cause currentCause = isMainthread ? PhaseTracker.getInstance().getCurrentCause() : Cause.of(EventContext.empty(), damageSource);
                    final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(currentCause, (org.spongepowered.api.entity.Entity) targetEntity,
                        originalFunctions, knockbackModifier, originalBaseDamage);
                    SpongeCommon.postEvent(event);
                    if (isMainthread) {
                        PhaseTracker.getInstance().popCause();
                    }
                    if (event.isCancelled()) {
                        return;
                    }

                    damage = (float) event.getFinalOutputDamage();
                    // Sponge End

                    // Sponge Start - need final for later events
                    final double attackDamage = damage;
                    knockbackModifier = (int) event.getKnockbackModifier();
                    enchantmentDamage = (float) enchantmentModifiers.stream()
                        .mapToDouble(event::getOutputDamage)
                        .sum();
                    // Sponge End

                    float targetOriginalHealth = 0.0F;
                    boolean litEntityOnFire = false;
                    final int fireAspectModifier = EnchantmentHelper.getFireAspect((PlayerEntity) (Object) this);

                    if (targetEntity instanceof LivingEntity) {
                        targetOriginalHealth = ((LivingEntity) targetEntity).getHealth();

                        if (fireAspectModifier > 0 && !targetEntity.isOnFire()) {
                            litEntityOnFire = true;
                            targetEntity.setSecondsOnFire(1);
                        }
                    }

                    final net.minecraft.util.math.vector.Vector3d targetMotion = targetEntity.getDeltaMovement();
                    final boolean attackSucceeded = targetEntity.hurt(DamageSource.playerAttack((PlayerEntity) (Object) this), damage);

                    if (attackSucceeded) {
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity) targetEntity).knockback((float) knockbackModifier * 0.5F, (double) MathHelper
                                    .sin(this.yRot * 0.017453292F), (double) (-MathHelper.cos(this.yRot * 0.017453292F)));
                            } else {
                                targetEntity.push((double) (-MathHelper.sin(this.yRot * 0.017453292F) * (float) knockbackModifier * 0.5F), 0.1D, (double) (MathHelper.cos(this.yRot
                                        * 0.017453292F) * (float) knockbackModifier * 0.5F));
                            }

                            this.shadow$setDeltaMovement(this.shadow$getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
                            this.shadow$setSprinting(false);
                        }

                        if (isSweapingAttack) {
                            for (final LivingEntity livingEntity : this.level
                                    .getEntitiesOfClass(LivingEntity.class, targetEntity.getBoundingBox().inflate(1.0D, 0.25D, 1.0D))) {
                                if (livingEntity != (PlayerEntity) (Object) this && livingEntity != targetEntity && !this.shadow$isAlliedTo(livingEntity) && (!(livingEntity instanceof ArmorStandEntity) || !((ArmorStandEntity)livingEntity).isMarker()) && this.shadow$distanceToSqr(livingEntity) < 9.0D) {
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
                                            incoming -> EnchantmentHelper.getSweepingDamageRatio((PlayerEntity) (Object) this) * attackDamage);
                                        final List<DamageFunction> sweapingFunctions = new ArrayList<>();
                                        sweapingFunctions.add(sweapingFunction);
                                        final AttackEntityEvent sweepingAttackEvent = SpongeEventFactory.createAttackEntityEvent(
                                            currentCause, (org.spongepowered.api.entity.Entity) livingEntity,
                                            sweapingFunctions, 1, 1.0D);
                                        SpongeCommon.postEvent(sweepingAttackEvent);
                                        if (!sweepingAttackEvent.isCancelled()) {
                                            livingEntity.knockback((sweepingAttackEvent.getKnockbackModifier() * 0.4F,
                                                    (double) MathHelper.sin(this.yRot * ((float)Math.PI / 180F)),
                                                    (double) -MathHelper.cos(this.yRot * ((float)Math.PI / 180F)));

                                            livingEntity.hurt(DamageSource.playerAttack((PlayerEntity) (Object) this),
                                                (float) sweepingAttackEvent.getFinalOutputDamage());
                                        }
                                    }
                                    // Sponge End
                                }
                            }

                            this.level.playSound(null, this.shadow$getX(), this.shadow$getY(), this.shadow$getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.shadow$getSoundSource(), 1.0F, 1.0F);
                            this.shadow$sweepAttack();
                        }

                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.hurtMarked) {
                            ((ServerPlayerEntity) targetEntity).connection.send(new SEntityVelocityPacket(targetEntity));
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
                            EnchantmentHelper.doPostHurtEffects((LivingEntity) targetEntity, (PlayerEntity) (Object) this);
                        }

                        EnchantmentHelper.doPostDamageEffects((PlayerEntity) (Object) this, targetEntity);
                        final ItemStack itemstack1 = this.shadow$getMainHandItem();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity) targetEntity).parentMob;
                        }

                        if(!this.level.isClientSide && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                            itemstack1.hurtEnemy((LivingEntity) entity, (PlayerEntity) (Object) this);
                            if(itemstack1.isEmpty()) {
                                this.shadow$setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
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
                                ((net.minecraft.world.server.ServerWorld) this.level).sendParticles(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getX(), targetEntity.getY() + (double) (targetEntity.getBbHeight() * 0.5F), targetEntity.getZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
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
