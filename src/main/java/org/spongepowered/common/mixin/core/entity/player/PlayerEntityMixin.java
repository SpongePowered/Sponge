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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.FoodStats;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.ModifierFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageFunction;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.damage.DamageModifierTypes;
import org.spongepowered.api.event.cause.entity.damage.DamageTypes;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.ChangeEntityExperienceEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.living.ChangeLevelEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.LocationTargetingBridge;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.LivingEntityMixin;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntityMixin implements PlayerEntityBridge, LocationTargetingBridge {

    @Shadow public int experienceLevel;
    @Shadow public int experienceTotal;
    @Shadow public float experience;
    @Shadow public PlayerAbilities abilities;
    @Shadow public net.minecraft.entity.player.PlayerInventory inventory;
    @Shadow public BlockPos spawnPos;
    @Shadow public Container openContainer;

    @Shadow public abstract boolean shadow$isSpectator();
    @Shadow public abstract int shadow$xpBarCap();
    @Shadow public abstract float shadow$getCooledAttackStrength(float adjustTicks);
    @Shadow public abstract float shadow$getAIMoveSpeed();
    @Shadow public abstract void shadow$onCriticalHit(net.minecraft.entity.Entity entityHit);
    @Shadow public abstract void shadow$onEnchantmentCritical(net.minecraft.entity.Entity entityHit); // onEnchantmentCritical
    @Shadow public abstract void shadow$addExhaustion(float p_71020_1_);
    @Shadow public abstract void shadow$addStat(ResourceLocation stat, int amount);
    @Shadow public abstract void shadow$addStat(@Nullable Stat stat, int amount);
    @Shadow public abstract void shadow$addStat(ResourceLocation stat);
    @Shadow public abstract void shadow$resetCooldown();
    @Shadow public abstract void shadow$spawnSweepParticles(); //spawnSweepParticles()
    @Shadow public abstract void shadow$takeStat(Stat stat);
    @Shadow protected abstract void shadow$destroyVanishingCursedItems(); // Filter vanishing curse enchanted items
    @Shadow public void shadow$wakeUpPlayer(final boolean immediately, final boolean updateWorldFlag, final boolean setSpawn) {};
    @Shadow @Nullable public abstract ItemEntity shadow$dropItem(boolean dropAll); // Overridden in EntityPlayerMPMixin for tracking
    @Shadow @Nullable public abstract ItemEntity shadow$dropItem(final ItemStack droppedItem, final boolean dropAround, final boolean traceItem);
    @Shadow public abstract FoodStats shadow$getFoodStats();
    @Shadow public abstract GameProfile shadow$getGameProfile();
    @Shadow public abstract Scoreboard shadow$getWorldScoreboard();
    @Shadow public abstract void shadow$addExperienceLevel(int levels);
    @Shadow public abstract void shadow$addScore(int scoreIn);
    @Shadow protected abstract void shadow$spawnShoulderEntities();
    @Shadow public abstract boolean shadow$isCreative();
    @Shadow public boolean shadow$canAttackPlayer(final PlayerEntity other) {
        return false;
    }
    @Shadow public abstract SoundCategory shadow$getSoundCategory();
    @Shadow public abstract String shadow$getScoreboardName();

    private boolean impl$affectsSpawning = true;
    private Vector3d impl$targetedLocation = VecHelper.toVector3d(this.world.getSpawnPoint());
    private boolean impl$dontRecalculateExperience;
    private boolean impl$shouldRestoreInventory = false;
    protected final boolean impl$isFake = SpongeImplHooks.isFakePlayer((PlayerEntity) (Object) this);

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void impl$getDisplayNameWithParsing(final CallbackInfoReturnable<ITextComponent> ci) {
        ci.setReturnValue(LegacyTexts.parseComponent((StringTextComponent) ci.getReturnValue(), SpongeTexts.COLOR_CHAR));
    }

    @Override
    public int bridge$getExperienceSinceLevel() {
        return this.experienceTotal - ExperienceHolderUtils.xpAtLevel(this.experienceLevel);
    }

    @Override
    public void bridge$setExperienceSinceLevel(final int experience) {
        this.experienceTotal = ExperienceHolderUtils.xpAtLevel(this.experienceLevel) + experience;
        this.experience = (float) experience / this.shadow$xpBarCap();
    }


    @Override
    public void bridge$recalculateTotalExperience() {
        if (!this.impl$dontRecalculateExperience) {
            boolean isInaccurate = ExperienceHolderUtils.getLevelForExp(this.experienceTotal) != this.experienceLevel;
            if (!isInaccurate) {
                final float experienceLess = (this.bridge$getExperienceSinceLevel() - 0.5f) / this.shadow$xpBarCap();
                final float experienceMore = (this.bridge$getExperienceSinceLevel() + 0.5f) / this.shadow$xpBarCap();
                isInaccurate = this.experience < experienceLess || this.experience > experienceMore;
            }
            if (isInaccurate) {
                final int newExperienceInLevel = (int) (this.experience * this.shadow$xpBarCap());
                this.experienceTotal = ExperienceHolderUtils.xpAtLevel(this.experienceLevel) + newExperienceInLevel;
                this.experience = (float) newExperienceInLevel / this.shadow$xpBarCap();
            }
        }
    }

    /**
     * @author JBYoshi - October 11, 2017
     * @reason This makes the experience updating more accurate by using
     * integer-based calculations instead of floating-point calculations, which
     * are known to cause <a href="https://github.com/SpongePowered/SpongeVanilla/issues/340">
     * rounding errors.</a>
     */
    @Inject(method = "addExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void onAddExperienceLevels(final int levels, final CallbackInfo ci) {
        if (((WorldBridge) this.world).bridge$isFake()) {
            return;
        }
        if (!this.impl$dontRecalculateExperience) {
            final int newLevel = Math.max(this.experienceLevel + levels, 0);
            this.postEventAndUpdateExperience(ExperienceHolderUtils.xpAtLevel(newLevel)
                    + (int) (this.experience * ExperienceHolderUtils.getExpBetweenLevels(newLevel)));
            ci.cancel();
        }
    }

    @Inject(method = "onEnchant", at = @At("RETURN"))
    private void onEnchantChangeExperienceLevels(final ItemStack item, final int levels, final CallbackInfo ci) {
        this.bridge$recalculateTotalExperience();
    }

    /**
     * @author JBYoshi - May 17, 2017
     * @author i509VCB - February 17th, 2020 - 1.14.4
     * @reason This makes the experience updating more accurate and disables
     * the totalExperience recalculation above for this method, which would
     * otherwise have weird intermediate states.
     */
    @Overwrite
    public void giveExperiencePoints(int amount) {
        this.shadow$addScore(amount);
        final int i = Integer.MAX_VALUE - this.experienceTotal;

        if (amount > i) {
            amount = i;
        }

        if (((WorldBridge) this.world).bridge$isFake()) {
            this.experience += (float)amount / (float)this.shadow$xpBarCap();

            for(this.experienceTotal += amount; this.experience >= 1.0F; this.experience /= (float)this.shadow$xpBarCap()) {
                this.experience = (this.experience - 1.0F) * (float)this.shadow$xpBarCap();
                this.shadow$addExperienceLevel(1);
            }
        } else {
            this.postEventAndUpdateExperience(this.experienceTotal + amount);
        }
    }

    private void postEventAndUpdateExperience(final int finalExperience) {
        final SpongeExperienceHolderData data = new SpongeExperienceHolderData();
        data.setTotalExp(finalExperience);
        final ImmutableSpongeExperienceHolderData
            immutable =
            new ImmutableSpongeExperienceHolderData(this.experienceLevel, this.experienceTotal, this.bridge$getExperienceSinceLevel());
        final ChangeEntityExperienceEvent event = SpongeEventFactory.createChangeEntityExperienceEvent(
                Sponge.getCauseStackManager().getCurrentCause(), immutable, data, (Player) this);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            return;
        }
        int finalLevel = event.getFinalData().level().get();
        if (finalLevel != this.experienceLevel) {
            @SuppressWarnings("deprecation") final ChangeLevelEvent levelEvent = SpongeEventFactory.createChangeLevelEventTargetPlayer(
                    Sponge.getCauseStackManager().getCurrentCause(), this.experienceLevel, finalLevel, (Player) this);
            SpongeImpl.postEvent(levelEvent);
            if (levelEvent.isCancelled()) {
                return;
            }
            if (levelEvent.getLevel() != finalLevel) {
                finalLevel = levelEvent.getLevel();
                event.getFinalData().set(Keys.EXPERIENCE_LEVEL, finalLevel);
            }
            if (finalLevel != this.experienceLevel) {
                this.impl$dontRecalculateExperience = true;
                try {
                    this.shadow$addExperienceLevel(finalLevel - this.experienceLevel);
                } finally {
                    this.impl$dontRecalculateExperience = false;
                }
            }
        }
        this.experience = (float) event.getFinalData().experienceSinceLevel().get()
                / ExperienceHolderUtils.getExpBetweenLevels(finalLevel);
        this.experienceTotal = event.getFinalData().totalExperience().get();
        this.experienceLevel = finalLevel;
    }

    @Inject(method = "readAdditional", at = @At("RETURN"))
    private void recalculateXpOnLoad(final CompoundNBT compound, final CallbackInfo ci) {
        // Fix the mistakes of /xp commands past.
        this.bridge$recalculateTotalExperience();
    }

    /**
     * @author blood - May 12th, 2016
     * @author i509VCB - February 17th, 2020 - Update to 1.14.4
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles player death events.
     */
    @Overwrite
    @Override
    public void onDeath(final DamageSource cause) {
        // Sponge start - Fire DestructEntityEvent.Death
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().onMainThread();
        final Optional<DestructEntityEvent.Death>
                event = SpongeCommonEventFactory.callDestructEntityEventDeath((PlayerEntity) (Object) this, cause, isMainThread);
        if (event.map(Cancellable::isCancelled).orElse(true)) {
            return;
        }
        // Sponge end
        super.onDeath(cause);

        this.shadow$setPosition(this.posX, this.posY, this.posZ);
        if (!this.shadow$isSpectator()) {
            this.shadow$spawnDrops(cause);
        }

        if (cause != null) {
            this.shadow$setMotion((-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * ((float)Math.PI / 180F)) * 0.1F), 0.1F, (-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * ((float) Math.PI / 180F)) * 0.1F));
        } else {
            this.shadow$setMotion(0.0D, 0.1D, 0.0D);
        }

        this.shadow$addStat(Stats.DEATHS);
        this.shadow$takeStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        this.shadow$takeStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        this.shadow$extinguish();
        this.shadow$setFlag(0, false);
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSleeping()Z"))
    private boolean impl$postSleepingEvent(final PlayerEntity self) {
        if (self.isSleeping()) {
            if (!((WorldBridge) this.world).bridge$isFake()) {
                final CauseStackManager csm = Sponge.getCauseStackManager();
                csm.pushCause(this);
                final BlockPos bedLocation = this.spawnPos;
                final BlockSnapshot snapshot = ((org.spongepowered.api.world.World) this.world).createSnapshot(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
                SpongeImpl.postEvent(SpongeEventFactory.createSleepingEventTick(csm.getCurrentCause(), snapshot, (Humanoid) this));
                csm.popCause();
            }
            return true;
        }
        return false;
    }

    /**
     * @author gabizou - January 4th, 2016
     * @author i509VCB - January 17th, 2020 - 1.14.4
     *
     * This prevents sounds from being sent to the server by players who are vanish.
     */
    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"))
    private void impl$vanishPlaySound(final World world, final PlayerEntity player, final double x, final double y, final double z, final SoundEvent sound, final SoundCategory category, final float volume, final float pitch) {
        if (!this.bridge$isVanished()) {
            this.world.playSound(player, x, y, z, sound, category, volume, pitch);
        }
    }

    @Override
    public boolean bridge$affectsSpawning() {
        return this.impl$affectsSpawning && !this.shadow$isSpectator();
    }

    @Override
    public void bridge$setAffectsSpawning(final boolean affectsSpawning) {
        this.impl$affectsSpawning = affectsSpawning;
    }

    @Override
    public Vector3d bridge$getTargetedLocation() {
        return this.impl$targetedLocation;
    }

    @Override
    public void bridge$setTargetedLocation(@Nullable final Vector3d vec) {
        this.impl$targetedLocation = vec != null ? vec : VecHelper.toVector3d(this.world.getSpawnPoint());
        //noinspection ConstantConditions
        if (!((PlayerEntity) (Object) this instanceof ServerPlayerEntity)) {
            this.world.setSpawnPoint(VecHelper.toBlockPos(this.impl$targetedLocation));
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
     *
     * @param stack The item stack in use
     * @param tagManager The tag manager
     * @param cachedBlockInfo The cached block info.
     * @param pos The target position
     * @param facing The facing direction of the player
     * @param sameStack The very same stack as the first parameter
     * @return Check if the player is a fake player, if it is, then just do
     *  the same return, otherwise, throw an event first and then return if the
     *  event is cancelled, or the stack.canPlaceOn
     */
    @Redirect(method = "canPlayerEdit",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;canPlaceOn(Lnet/minecraft/tags/NetworkTagManager;Lnet/minecraft/util/CachedBlockInfo;)Z"))
    private boolean impl$canEditSpongeThrowChangePreEvent(
        final ItemStack stack, final NetworkTagManager tagManager, final CachedBlockInfo cachedBlockInfo, final BlockPos pos, final Direction facing, final ItemStack sameStack) {
        // Lazy evaluation, if the stack isn't placeable anyways, might as well not
        // call the logic.
        if (!stack.canPlaceOn(tagManager, cachedBlockInfo)) {
            return false;
        }
        // If we're going to throw an event, then do it.
        // Just sanity checks, if the player is not in a managed world, then don't bother either.
        // some fake players may exist in pseudo worlds as well, which means we don't want to
        // process on them since the world is not a valid world to plugins.
        if (this.world instanceof WorldBridge && !((WorldBridge) this.world).bridge$isFake() && ShouldFire.CHANGE_BLOCK_EVENT_PRE) {
            // Note that this can potentially cause phase contexts to auto populate frames
            // we shouldn't rely so much on them, but sometimes the extra information is provided
            // through this method.
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                // Go ahead and add the item stack in use, just in the event the current phase contexts don't provide
                // that information.
                frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(stack));
                // Then go ahead and call the event and return if it was cancelled
                // if it was cancelled, then there should be no changes needed to roll back
                return !SpongeCommonEventFactory.callChangeBlockEventPre((ServerWorldBridge) this.world, pos, this).isCancelled();
            }
        }
        // Otherwise, if all else is ignored, or we're not throwing events, we're just going to return the
        // default value: true.
        return true;
    }

    /**
     * @author gabizou - June 13th, 2016
     * @reason Reverts the method to flow through our systems, Forge patches
     * this to throw an ItemTossEvent, but we'll be throwing it regardless in
     * SpongeForge's handling.
     *
     * @param itemStackIn
     * @param unused
     * @return
     */
    @Overwrite
    @Nullable
    public ItemEntity dropItem(final ItemStack itemStackIn, final boolean unused) {
        return this.shadow$dropItem(itemStackIn, false, false);
    }

    /**
     * @author gabizou - June 4th, 2016
     * @author i509VCB - February 17th, 2020 - 1.14.4
     *
     * @reason When a player drops an item, all methods flow through here instead of {@link Entity#entityDropItem(IItemProvider, int)}
     * because of the idea of {@code dropAround} and {@code traceItem}.
     *
     * @param droppedItem The item to drop
     * @param dropAround If true, the item is dropped around the player, otherwise thrown in front of the player
     * @param traceItem If true, the item is thrown as the player
     * @return The entity, if spawned correctly and not captured in any way
     */
    @Nullable
    @Overwrite
    public ItemEntity dropItem(final ItemStack droppedItem, final boolean dropAround, final boolean traceItem) {
        if (droppedItem.isEmpty()) {
            return null;
        }
        // Sponge Start - redirect to our handling to capture and throw events.
        if (!((WorldBridge) this.world).bridge$isFake()) {
            ((PlayerEntityBridge) this).bridge$shouldRestoreInventory(false);
            final PlayerEntity player = (PlayerEntity) (PlayerEntityBridge) this;

            final double posX1 = player.posX;
            final double posY1 = player.posY - 0.3 + player.getEyeHeight();
            final double posZ1 = player.posZ;
            // Now the real fun begins.
            final ItemStack item;
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(droppedItem);
            final List<ItemStackSnapshot> original = new ArrayList<>();
            original.add(snapshot);

            final PhaseContext<?> phaseContext = PhaseTracker.getInstance().getCurrentContext();
            @SuppressWarnings("RawTypeCanBeGeneric") final IPhaseState currentState = phaseContext.state;

            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

                item = SpongeCommonEventFactory.throwDropItemAndConstructEvent((PlayerEntity) (PlayerEntityBridge) this, posX1, posY1, posZ1, snapshot, original, frame);

                if (item == null || item.isEmpty()) {
                    return null;
                }


                // Here is where we would potentially perform item pre-merging (merge the item stacks with previously captured item stacks
                // and only if those stacks can be stacked (count increased). Otherwise, we'll just continue to throw the entity item.
                // For now, due to refactoring a majority of all of this code, pre-merging is disabled entirely.

                final ItemEntity itemEntity = new ItemEntity(player.world, posX1, posY1, posZ1, droppedItem);
                itemEntity.setPickupDelay(40);

                if (traceItem) {
                    itemEntity.setThrowerId(player.getUniqueID());
                }

                final Random random = player.getRNG();
                if (dropAround) {
                    final float f = random.nextFloat() * 0.5F;
                    final float f1 = random.nextFloat() * ((float) Math.PI * 2F);
                    itemEntity.setMotion(-MathHelper.sin(f1) * f, 0.2F, MathHelper.cos(f1) * f);
                } else {
                    float f8 = MathHelper.sin(this.rotationPitch * ((float)Math.PI / 180F));
                    float f2 = MathHelper.cos(this.rotationPitch * ((float)Math.PI / 180F));
                    float f3 = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
                    float f4 = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
                    float f5 = this.rand.nextFloat() * ((float)Math.PI * 2F);
                    float f6 = 0.02F * this.rand.nextFloat();
                    itemEntity.setMotion((double)(-f3 * f2 * 0.3F) + Math.cos(f5) * (double)f6, (-f8 * 0.3F + 0.1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), (double)(f4 * f2 * 0.3F) + Math.sin(f5) * (double)f6);
                }
                // FIFTH - Capture the entity maybe?
                if (currentState.spawnItemOrCapture(phaseContext, (PlayerEntity) (PlayerEntityBridge) this, itemEntity)) {
                    return itemEntity;
                }
                // TODO - Investigate whether player drops are adding to the stat list in captures.
                final ItemStack stack = itemEntity.getItem();
                player.world.addEntity(itemEntity);

                if (traceItem) {
                    if (!stack.isEmpty()) {
                        player.addStat(Stats.ITEM_DROPPED.get(stack.getItem()), droppedItem.getCount());
                    }

                    player.addStat(Stats.DROP);
                }

                return itemEntity;
            }
        }
        // Sponge end
        final double d0 = this.posY - 0.30000001192092896D + (double) this.shadow$getEyeHeight();
        final ItemEntity itemEntity = new ItemEntity(this.world, this.posX, d0, this.posZ, droppedItem);
        itemEntity.setPickupDelay(40);

        if (traceItem) {
            itemEntity.setThrowerId(this.shadow$getUniqueID());
        }

        if (dropAround) {
            final float f = this.rand.nextFloat() * 0.5F;
            final float f1 = this.rand.nextFloat() * ((float) Math.PI * 2F);
            itemEntity.setMotion(-MathHelper.sin(f1) * f, 0.2F, MathHelper.cos(f1) * f);
        } else {
            float f8 = MathHelper.sin(this.rotationPitch * ((float)Math.PI / 180F));
            float f2 = MathHelper.cos(this.rotationPitch * ((float)Math.PI / 180F));
            float f3 = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
            float f4 = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
            float f5 = this.rand.nextFloat() * ((float)Math.PI * 2F);
            float f6 = 0.02F * this.rand.nextFloat();
            itemEntity.setMotion((double)(-f3 * f2 * 0.3F) + Math.cos(f5) * (double)f6, (-f8 * 0.3F + 0.1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), (double)(f4 * f2 * 0.3F) + Math.sin(f5) * (double)f6);
        }

        final ItemStack itemstack = this.dropItemAndGetStack(itemEntity);

        if (traceItem) {
            if (itemstack != null && !itemstack.isEmpty()) { // Sponge - add null check
                this.shadow$addStat(Stats.ITEM_DROPPED.get(itemstack.getItem()), droppedItem.getCount());
            }

            this.shadow$addStat(Stats.DROP);
        }

        return itemEntity;
    }

    /**
     * @author gabizou - June 4th, 2016
     * @reason Overwrites the original logic to simply pass through to the
     * PhaseTracker.
     *
     * @param entity The entity item to spawn
     * @return The itemstack
     */
    @SuppressWarnings("OverwriteModifiers") // This is a MinecraftDev thing, since forge elevates the modifier to public
    @Overwrite
    @Nullable
    public ItemStack dropItemAndGetStack(final ItemEntity entity) {
        this.world.addEntity0(entity);
        return entity.getItem();
    }

    /**
     * @author dualspiral - October 7th, 2016
     *
     * @reason When setting {@link SpongeHealthData#setHealth(double)} to 0, {@link #onDeath(DamageSource)} was
     * not being called. This check bypasses some of the checks that prevent the superclass method being called
     * when the {@link DamageSourceRegistryModule#IGNORED_DAMAGE_SOURCE} is being used.
     */
    @Inject(method = "attackEntityFrom", cancellable = true, at = @At(value = "HEAD"))
    private void onAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        if (source == DamageSourceRegistryModule.IGNORED_DAMAGE_SOURCE) {
            // Taken from the original method, wake the player up if they are about to die.
            if (this.shadow$isSleeping() && !this.world.isRemote) {
                this.shadow$wakeUpPlayer(true, true, false);
            }

            // We just throw it to the superclass method so that we can potentially get the
            // onDeath method.
            cir.setReturnValue(super.attackEntityFrom(source, amount));
        }
    }

    /**
     * @author gabizou - April 8th, 2016
     * @author gabizou - April 11th, 2016 - Update for 1.9 - This enitre method was rewritten
     * @author i509VCB - February 15th, 2020 - Update for 1.14.4
     *
     * @reason Rewrites the attackTargetEntityWithCurrentItem to throw an {@link AttackEntityEvent} prior
     * to the ensuing {@link DamageEntityEvent}. This should cover all cases where players are
     * attacking entities and those entities override {@link LivingEntity#attackEntityFrom(DamageSource, float)}
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void attackTargetEntityWithCurrentItem(final Entity targetEntity) {
        // Sponge Start - Add SpongeImpl hook to override in forge as necessary
        if (!SpongeImplHooks.checkAttackEntity((PlayerEntity) (Object) this, targetEntity)) {
            return;
        }
        // Sponge End
        if (targetEntity.canBeAttackedWithItem()) {
            if (!targetEntity.hitByEntity((PlayerEntity) (Object) this)) {
                // Sponge Start - Prepare our event values
                // float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                final double originalBaseDamage = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
                float damage = (float) originalBaseDamage;
                // Sponge End
                float enchantmentDamage = 0.0F;

                // Sponge Start - Redirect getting enchantments for our damage event handlers
                // if (targetEntity instanceof LivingEntity) {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((LivingEntity) targetEntity).getCreatureAttribute());
                // } else {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
                // }
                final float attackStrength = this.shadow$getCooledAttackStrength(0.5F);

                final List<ModifierFunction<DamageModifier>> originalFunctions = new ArrayList<>();

                final CreatureAttribute creatureAttribute = targetEntity instanceof LivingEntity
                                                                ? ((LivingEntity) targetEntity).getCreatureAttribute()
                                                                : CreatureAttribute.UNDEFINED;
                final List<DamageFunction> enchantmentModifierFunctions = DamageEventHandler.createAttackEnchantmentFunction(this.shadow$getHeldItemMainhand(), creatureAttribute, attackStrength);
                // This is kept for the post-damage event handling
                final List<DamageModifier> enchantmentModifiers = enchantmentModifierFunctions.stream().map(ModifierFunction::getModifier).collect(Collectors.toList());

                enchantmentDamage = (float) enchantmentModifierFunctions.stream()
                        .map(ModifierFunction::getFunction)
                        .mapToDouble(function -> function.applyAsDouble(originalBaseDamage))
                        .sum();
                originalFunctions.addAll(enchantmentModifierFunctions);
                // Sponge End

                originalFunctions.add(DamageEventHandler.provideCooldownAttackStrengthFunction((PlayerEntity) (Object) this, attackStrength));
                damage = damage * (0.2F + attackStrength * attackStrength * 0.8F);
                enchantmentDamage = enchantmentDamage * attackStrength;
                this.shadow$resetCooldown();

                if (damage > 0.0F || enchantmentDamage > 0.0F) {
                    final boolean isStrongAttack = attackStrength > 0.9F;
                    boolean isSprintingAttack = false;
                    boolean isCriticalAttack = false;
                    boolean isSweapingAttack = false;
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackModifier((PlayerEntity) (Object) this);

                    if (this.shadow$isSprinting() && isStrongAttack) {
                        // Sponge - Only play sound after the event has be thrown and not cancelled.
                        // this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.entity_player_attack_knockback, this.getSoundCategory(), 1.0F, 1.0F);
                        ++knockbackModifier;
                        isSprintingAttack = true;
                    }

                    isCriticalAttack = isStrongAttack && this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.shadow$isInWater() && !this.isPotionActive(Effects.BLINDNESS) && !this.shadow$isPassenger() && targetEntity instanceof LivingEntity;
                    isCriticalAttack = isCriticalAttack && !this.shadow$isSprinting();

                    if (isCriticalAttack) {
                        // Sponge Start - add critical attack tuple
                        // damage *= 1.5F; // Sponge - This is handled in the event
                        originalFunctions.add(DamageEventHandler.provideCriticalAttackTuple((PlayerEntity) (Object) this));
                        // Sponge End
                    }

                    // damage = damage + enchantmentDamage; // Sponge - We don't need this since our event will re-assign the damage to deal
                    final double distanceWalkedDelta = (double) (this.distanceWalkedModified - this.prevDistanceWalkedModified);

                    final ItemStack heldItem = this.getHeldItem(Hand.MAIN_HAND);
                    if (isStrongAttack && !isCriticalAttack && !isSprintingAttack && this.onGround && distanceWalkedDelta < (double) this.shadow$getAIMoveSpeed()) {
                        final ItemStack itemstack = heldItem;

                        if (itemstack.getItem() instanceof SwordItem) {
                            isSweapingAttack = true;
                        }
                    }

                    // Sponge Start - Create the event and throw it
                    final DamageSource damageSource = DamageSource.causePlayerDamage((PlayerEntity) (Object) this);
                    final boolean isMainthread = !this.world.isRemote;
                    if (isMainthread) {
                        Sponge.getCauseStackManager().pushCause(damageSource);
                    }
                    final Cause currentCause = isMainthread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), damageSource);
                    final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(currentCause, (org.spongepowered.api.entity.Entity) targetEntity,
                        originalFunctions, knockbackModifier, originalBaseDamage);
                    SpongeImpl.postEvent(event);
                    if (isMainthread) {
                        Sponge.getCauseStackManager().popCause();
                    }
                    if (event.isCancelled()) {
                        return;
                    }

                    damage = (float) event.getFinalOutputDamage();
                    // sponge - need final for later events
                    final double attackDamage = damage;
                    knockbackModifier = event.getKnockbackModifier();
                    enchantmentDamage = (float) enchantmentModifiers.stream()
                            .mapToDouble(event::getOutputDamage)
                            .sum();
                    // Sponge End

                    float targetOriginalHealth = 0.0F;
                    boolean litEntityOnFire = false;
                    final int fireAspectModifier = EnchantmentHelper.getFireAspectModifier((PlayerEntity) (Object) this);

                    if (targetEntity instanceof LivingEntity) {
                        targetOriginalHealth = ((LivingEntity) targetEntity).getHealth();

                        if (fireAspectModifier > 0 && !targetEntity.isBurning()) {
                            litEntityOnFire = true;
                            targetEntity.setFire(1);
                        }
                    }

                    final Vec3d targetMotion = targetEntity.getMotion();
                    final boolean attackSucceeded = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) (Object) this), damage);

                    if (attackSucceeded) {
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof LivingEntity) {
                                ((LivingEntity) targetEntity).knockBack((PlayerEntity) (Object) this, (float) knockbackModifier * 0.5F, (double) MathHelper.sin(this.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                            } else {
                                targetEntity.addVelocity((double) (-MathHelper.sin(this.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F), 0.1D, (double) (MathHelper.cos(this.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F));
                            }

                            this.shadow$setMotion(this.shadow$getMotion().mul(0.6D, 1.0D, 0.6D));
                            this.shadow$setSprinting(false);
                        }

                        if (isSweapingAttack) {
                            for (final LivingEntity livingEntity : this.world.getEntitiesWithinAABB(LivingEntity.class, targetEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
                                if (livingEntity != (PlayerEntity) (Object) this && livingEntity != targetEntity && !this.shadow$isOnSameTeam(livingEntity) && (!(livingEntity instanceof ArmorStandEntity) || !((ArmorStandEntity)livingEntity).hasMarker()) && this.shadow$getDistanceSq(livingEntity) < 9.0D) {
                                    // Sponge Start - Do a small event for these entities
                                    // livingEntity.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                                    // livingEntity.attackEntityFrom(DamageSource.causePlayerDamage(this), 1.0F);
                                    final EntityDamageSource sweepingAttackSource = EntityDamageSource.builder().entity((Player) this).type(DamageTypes.SWEEPING_ATTACK.get()).build();
                                    try (final CauseStackManager.StackFrame frame = isMainthread ? Sponge.getCauseStackManager().pushCauseFrame() : null) {
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
                                                .type(DamageModifierTypes.SWEEPING.get())
                                                .build(),
                                            incoming -> EnchantmentHelper.getSweepingDamageRatio((PlayerEntity) (Object) this) * attackDamage);
                                        final List<DamageFunction> sweapingFunctions = new ArrayList<>();
                                        sweapingFunctions.add(sweapingFunction);
                                        final AttackEntityEvent sweepingAttackEvent = SpongeEventFactory.createAttackEntityEvent(
                                            currentCause, (org.spongepowered.api.entity.Entity) livingEntity,
                                            sweapingFunctions, 1, 1.0D);
                                        SpongeImpl.postEvent(sweepingAttackEvent);
                                        if (!sweepingAttackEvent.isCancelled()) {
                                            livingEntity
                                                .knockBack((PlayerEntity) (Object) this, sweepingAttackEvent.getKnockbackModifier() * 0.4F,
                                                    (double) MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F)),
                                                    (double) -MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F)));

                                            livingEntity.attackEntityFrom(DamageSource.causePlayerDamage((PlayerEntity) (Object) this),
                                                (float) sweepingAttackEvent.getFinalOutputDamage());
                                        }
                                    }
                                    // Sponge End
                                }
                            }

                            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.shadow$getSoundCategory(), 1.0F, 1.0F);
                            this.shadow$spawnSweepParticles();
                        }

                        if (targetEntity instanceof ServerPlayerEntity && targetEntity.velocityChanged) {
                            ((ServerPlayerEntity) targetEntity).connection.sendPacket(new SEntityVelocityPacket(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.setMotion(targetMotion);
                        }

                        if (isCriticalAttack) {
                            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.shadow$getSoundCategory(), 1.0F, 1.0F);
                            this.shadow$onCriticalHit(targetEntity);
                        }

                        if (!isCriticalAttack && !isSweapingAttack) {
                            if (isStrongAttack) {
                                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.shadow$getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK , this.shadow$getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (enchantmentDamage > 0.0F) {
                            this.shadow$onEnchantmentCritical(targetEntity);
                        }

                        this.shadow$setLastAttackedEntity(targetEntity);

                        if (targetEntity instanceof LivingEntity) {
                            EnchantmentHelper.applyThornEnchantments((LivingEntity) targetEntity, (PlayerEntity) (Object) this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments((PlayerEntity) (Object) this, targetEntity);
                        final ItemStack itemstack1 = this.shadow$getHeldItemMainhand();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof EnderDragonPartEntity) {
                            entity = ((EnderDragonPartEntity) targetEntity).dragon;
                        }

                        if(!this.world.isRemote && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                            itemstack1.hitEntity((LivingEntity) entity, (PlayerEntity) (Object) this);
                            if(itemstack1.isEmpty()) {
                                this.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (targetEntity instanceof LivingEntity) {
                            final float f5 = targetOriginalHealth - ((LivingEntity) targetEntity).getHealth();
                            this.shadow$addStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (fireAspectModifier > 0) {
                                targetEntity.setFire(fireAspectModifier * 4);
                            }

                            if (this.world instanceof ServerWorld && f5 > 2.0F) {
                                final int k = (int) ((double) f5 * 0.5D);
                                ((ServerWorld) this.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double) (targetEntity.getHeight() * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D);
                            }
                        }

                        this.shadow$addExhaustion(0.1F);
                    } else {
                        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.shadow$getSoundCategory(), 1.0F, 1.0F);

                        if (litEntityOnFire) {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
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
    public boolean impl$isImmuneToFireForIgniteEvent() {
        return this.shadow$isSpectator() || this.shadow$isCreative();
    }

}
