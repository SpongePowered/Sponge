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

import com.flowpowered.math.vector.Vector3d;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
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
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
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
import org.spongepowered.common.bridge.entity.player.EntityPlayerBridge;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeExperienceHolderData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.processor.common.ExperienceHolderUtils;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.damage.DamageEventHandler;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.bridge.LocationTargetingBridge;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.EntityLivingBaseMixin;
import org.spongepowered.common.registry.type.event.DamageSourceRegistryModule;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBaseMixin implements EntityPlayerBridge, LocationTargetingBridge {

    @Shadow public Container inventoryContainer;
    @Shadow public Container openContainer;
    @Shadow public int experienceLevel;
    @Shadow public int experienceTotal;
    @Shadow public float experience;
    @Shadow public PlayerCapabilities capabilities;
    @Shadow public InventoryPlayer inventory;
    @Shadow public BlockPos bedLocation;

    @Shadow public abstract boolean isPlayerSleeping();
    @Shadow public abstract boolean isSpectator();
    @Shadow public abstract int xpBarCap();
    @Shadow public abstract float getCooledAttackStrength(float adjustTicks);
    @Shadow public abstract float getAIMoveSpeed();
    @Shadow public abstract void onCriticalHit(net.minecraft.entity.Entity entityHit);
    @Shadow public abstract void onEnchantmentCritical(net.minecraft.entity.Entity entityHit); // onEnchantmentCritical
    @Shadow public abstract void addExhaustion(float p_71020_1_);
    @Shadow public abstract void addStat(@Nullable StatBase stat, int amount);
    @Shadow public abstract void addStat(StatBase stat);
    @Shadow public abstract void resetCooldown();
    @Shadow public abstract void spawnSweepParticles(); //spawnSweepParticles()
    @Shadow public abstract void takeStat(StatBase stat);
    @Shadow protected abstract void destroyVanishingCursedItems(); // Filter vanishing curse enchanted items
    @Shadow public void wakeUpPlayer(final boolean immediately, final boolean updateWorldFlag, final boolean setSpawn) {};
    @Shadow @Nullable public abstract EntityItem dropItem(boolean dropAll); // Overridden in EntityPlayerMPMixin for tracking
    @Shadow public abstract FoodStats getFoodStats();
    @Shadow public abstract GameProfile getGameProfile();
    @Shadow public abstract Scoreboard getWorldScoreboard();
    @Shadow public abstract String shadow$getName();
    @Shadow @Nullable public abstract Team getTeam();
    @Shadow public abstract void addExperienceLevel(int levels);
    @Shadow public abstract void addScore(int scoreIn);

    @Shadow protected abstract void spawnShoulderEntities();
    @Shadow public abstract boolean isCreative();

    @Shadow public boolean canAttackPlayer(final EntityPlayer other) {
        return false;
    }

    private boolean affectsSpawning = true;
    private Vector3d targetedLocation = VecHelper.toVector3d(this.world.getSpawnPoint());
    private boolean dontRecalculateExperience;
    private boolean shouldRestoreInventory = false;
    protected final boolean isFake = SpongeImplHooks.isFakePlayer((EntityPlayer) (Object) this);



    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void impl$getDisplayNameWithParsing(final CallbackInfoReturnable<ITextComponent> ci) {
        ci.setReturnValue(LegacyTexts.parseComponent((TextComponentString) ci.getReturnValue(), SpongeTexts.COLOR_CHAR));
    }

    @Override
    public int bridge$getExperienceSinceLevel() {
        return this.experienceTotal - ExperienceHolderUtils.xpAtLevel(this.experienceLevel);
    }

    @Override
    public void bridge$setExperienceSinceLevel(final int experience) {
        this.experienceTotal = ExperienceHolderUtils.xpAtLevel(this.experienceLevel) + experience;
        this.experience = (float) experience / this.xpBarCap();
    }


    @Override
    public void bridge$recalculateTotalExperience() {
        if (!this.dontRecalculateExperience) {
            boolean isInaccurate = ExperienceHolderUtils.getLevelForExp(this.experienceTotal) != this.experienceLevel;
            if (!isInaccurate) {
                final float experienceLess = (this.bridge$getExperienceSinceLevel() - 0.5f) / this.xpBarCap();
                final float experienceMore = (this.bridge$getExperienceSinceLevel() + 0.5f) / this.xpBarCap();
                isInaccurate = this.experience < experienceLess || this.experience > experienceMore;
            }
            if (isInaccurate) {
                final int newExperienceInLevel = (int) (this.experience * this.xpBarCap());
                this.experienceTotal = ExperienceHolderUtils.xpAtLevel(this.experienceLevel) + newExperienceInLevel;
                this.experience = (float) newExperienceInLevel / this.xpBarCap();
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
        if (!this.dontRecalculateExperience) {
            final int newLevel = this.experienceLevel + levels;
            postEventAndUpdateExperience(newLevel < 0 ? 0 : ExperienceHolderUtils.xpAtLevel(newLevel)
                    + (int) (this.experience * ExperienceHolderUtils.getExpBetweenLevels(newLevel)));
            ci.cancel();
        }
    }

    @Inject(method = "onEnchant", at = @At("RETURN"))
    private void onEnchantChangeExperienceLevels(final ItemStack item, final int levels, final CallbackInfo ci) {
        bridge$recalculateTotalExperience();
    }

    /**
     * @author JBYoshi - May 17, 2017
     * @reason This makes the experience updating more accurate and disables
     * the totalExperience recalculation above for this method, which would
     * otherwise have weird intermediate states.
     */
    @Overwrite
    public void addExperience(int amount) {
        this.addScore(amount);
        final int i = Integer.MAX_VALUE - this.experienceTotal;

        if (amount > i) {
            amount = i;
        }

        if (((WorldBridge) this.world).bridge$isFake()) {
            this.experience += (float)amount / (float)this.xpBarCap();

            for(this.experienceTotal += amount; this.experience >= 1.0F; this.experience /= (float)this.xpBarCap()) {
                this.experience = (this.experience - 1.0F) * (float)this.xpBarCap();
                this.addExperienceLevel(1);
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
            @SuppressWarnings("deprecation") final org.spongepowered.api.event.entity.living.humanoid.ChangeLevelEvent levelEvent = SpongeEventFactory.createChangeLevelEventTargetPlayer(
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
                this.dontRecalculateExperience = true;
                try {
                    addExperienceLevel(finalLevel - this.experienceLevel);
                } finally {
                    this.dontRecalculateExperience = false;
                }
            }
        }
        this.experience = (float) event.getFinalData().experienceSinceLevel().get()
                / ExperienceHolderUtils.getExpBetweenLevels(finalLevel);
        this.experienceTotal = event.getFinalData().totalExperience().get();
        this.experienceLevel = finalLevel;
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    private void impl$fixEntityProperties(final NBTTagCompound compound, final CallbackInfo ci) {
        // Fix the mistakes of /xp commands past.
        bridge$recalculateTotalExperience();

        // Fix isDead not getting set back to true when a player that hasn't respawned after dying reconnects.
        if (getHealth() <= 0.0F) {
            this.isDead = true;
        }
    }

    /**
     * @author blood - May 12th, 2016
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles player death events.
     */
    @Overwrite
    @Override
    public void onDeath(final DamageSource cause) {
        final boolean isMainThread = Sponge.isServerAvailable() && Sponge.getServer().isMainThread();
        final Optional<DestructEntityEvent.Death>
                event = SpongeCommonEventFactory.callDestructEntityEventDeath((EntityPlayer) (Object) this, cause, isMainThread);
        if (event.map(Cancellable::isCancelled).orElse(true)) {
            return;
        }
        super.onDeath(cause);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.10000000149011612D;

        if (this.shadow$getName().equals("Notch")) {
            this.dropItem(new ItemStack(Items.APPLE, 1), true, false);
        }

        if (!this.world.getGameRules().getBoolean("keepInventory") && !this.isSpectator()) {
            this.destroyVanishingCursedItems();
            this.inventory.dropAllItems();
        }

        if (cause != null) {
            this.motionX = (double) (-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F);
            this.motionZ = (double) (-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * 0.017453292F) * 0.1F);
        } else {
            this.motionX = this.motionZ = 0.0D;
        }

        this.addStat(StatList.DEATHS);
        this.takeStat(StatList.TIME_SINCE_DEATH);
        this.extinguish();
        this.setFlag(0, false);
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;"))
    private List<Entity> onSpongeGetEntitiesWithinAABBExcludingEntity(final World world, Entity entityIn, AxisAlignedBB bb) {
        if (this.bridge$isVanished() && this.bridge$isUncollideable()) {
            return Collections.emptyList();
        }
        return world.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isPlayerSleeping()Z"))
    private boolean onSpongeIsPlayerSleeping(final EntityPlayer self) {
        if (self.isPlayerSleeping()) {
            if (!((WorldBridge) this.world).bridge$isFake()) {
                final CauseStackManager csm = Sponge.getCauseStackManager();
                csm.pushCause(this);
                final BlockPos bedLocation = this.bedLocation;
                final BlockSnapshot snapshot = ((org.spongepowered.api.world.World) this.world).createSnapshot(bedLocation.getX(), bedLocation.getY(), bedLocation.getZ());
                SpongeImpl.postEvent(SpongeEventFactory.createSleepingEventTick(csm.getCurrentCause(), snapshot, (org.spongepowered.api.entity.Entity) this));
                csm.popCause();
            }
            return true;
        }
        return false;
    }

    /**
     * @author gabizou - January 4th, 2016
     *
     * This prevents sounds from being sent to the server by players who are vanish.
     */
    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/EntityPlayer;DDDLnet/minecraft/util/SoundEvent;Lnet/minecraft/util/SoundCategory;FF)V"))
    private void spongePlaySound(final World world, final EntityPlayer player, final double d1, final double d2, final double d3, final SoundEvent sound, final SoundCategory category, final float volume, final float pitch) {
        if (!this.bridge$isVanished()) {
            this.world.playSound(player, d1, d2, d3, sound, category, volume, pitch);
        }
    }

    @Override
    public boolean bridge$affectsSpawning() {
        return this.affectsSpawning && !this.isSpectator() && !this.bridge$isUntargetable();
    }

    @Override
    public void bridge$setAffectsSpawning(final boolean affectsSpawning) {
        this.affectsSpawning = affectsSpawning;
    }

    @Override
    public Vector3d bridge$getTargetedLocation() {
        return this.targetedLocation;
    }

    @Override
    public void bridge$setTargetedLocation(@Nullable final Vector3d vec) {
        this.targetedLocation = vec != null ? vec : VecHelper.toVector3d(this.world.getSpawnPoint());
        //noinspection ConstantConditions
        if (!((EntityPlayer) (Object) this instanceof EntityPlayerMP)) {
            this.world.setSpawnPoint(VecHelper.toBlockPos(this.targetedLocation));
        }
    }

    /**
     * @author gabizou - September 4th, 2018
     * @reason Bucket placement and other placements can be "detected"
     * for pre change events prior to them actually processing their logic,
     * this in effect can prevent item duplication issues when the block
     * changes are cancelled, but inventory is already modified. It would
     * be considered that during interaction packets, inventory is monitored,
     * however, sometimes that isn't enough.
     *
     * @param stack The item stack in use
     * @param block The target block
     * @param pos The target position
     * @param facing The facing direction of the player
     * @param sameStack The very same stack as the first parameter
     * @return Check if the player is a fake player, if it is, then just do
     *  the same return, otherwise, throw an event first and then return if the
     *  event is cancelled, or the stack.canPlaceOn
     */
    @Redirect(method = "canPlayerEdit", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;canPlaceOn(Lnet/minecraft/block/Block;)Z"))
    private boolean canEditSpongeThrowChangePreEvent(
        final ItemStack stack, final Block block, final BlockPos pos, final EnumFacing facing, final ItemStack sameStack) {
        // Lazy evaluation, if the stack isn't placeable anyways, might as well not
        // call the logic.
        if (!stack.canPlaceOn(block)) {
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
                return !SpongeCommonEventFactory.callChangeBlockEventPre((WorldServerBridge) this.world, pos, this).isCancelled();
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
    public EntityItem dropItem(final ItemStack itemStackIn, final boolean unused) {
        return this.dropItem(itemStackIn, false, false);
    }

    /**
     * @author gabizou - June 4th, 2016
     * @reason When a player drops an item, all methods flow through here instead of {@link Entity#dropItem(Item, int)}
     * because of the idea of {@code dropAround} and {@code traceItem}.
     *
     * @param droppedItem The item to drop
     * @param dropAround If true, the item is dropped around the player, otherwise thrown in front of the player
     * @param traceItem If true, the item is thrown as the player
     * @return The entity, if spawned correctly and not captured in any way
     */
    @Nullable
    @Overwrite
    public EntityItem dropItem(final ItemStack droppedItem, final boolean dropAround, final boolean traceItem) {
        if (droppedItem.isEmpty()) {
            return null;
        }
        // Sponge Start - redirect to our handling to capture and throw events.
        if (!((WorldBridge) this.world).bridge$isFake()) {
            ((EntityPlayerBridge) this).bridge$shouldRestoreInventory(false);
            final EntityPlayer player = (EntityPlayer) (EntityPlayerBridge) this;

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

                item = SpongeCommonEventFactory.throwDropItemAndConstructEvent((EntityPlayer) (EntityPlayerBridge) this, posX1, posY1, posZ1, snapshot, original, frame);

                if (item == null || item.isEmpty()) {
                    return null;
                }


                // Here is where we would potentially perform item pre-merging (merge the item stacks with previously captured item stacks
                // and only if those stacks can be stacked (count increased). Otherwise, we'll just continue to throw the entity item.
                // For now, due to refactoring a majority of all of this code, pre-merging is disabled entirely.

                final EntityItem entityitem = new EntityItem(player.world, posX1, posY1, posZ1, droppedItem);
                entityitem.setPickupDelay(40);

                if (traceItem) {
                    entityitem.setThrower(player.getName());
                }

                final Random random = player.getRNG();
                if (dropAround) {
                    final float f = random.nextFloat() * 0.5F;
                    final float f1 = random.nextFloat() * ((float) Math.PI * 2F);
                    entityitem.motionX = -MathHelper.sin(f1) * f;
                    entityitem.motionZ = MathHelper.cos(f1) * f;
                    entityitem.motionY = 0.20000000298023224D;
                } else {
                    float f2 = 0.3F;
                    entityitem.motionX = -MathHelper.sin(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
                    entityitem.motionZ = MathHelper.cos(player.rotationYaw * 0.017453292F) * MathHelper.cos(player.rotationPitch * 0.017453292F) * f2;
                    entityitem.motionY = - MathHelper.sin(player.rotationPitch * 0.017453292F) * f2 + 0.1F;
                    final float f3 = random.nextFloat() * ((float) Math.PI * 2F);
                    f2 = 0.02F * random.nextFloat();
                    entityitem.motionX += Math.cos(f3) * f2;
                    entityitem.motionY += (random.nextFloat() - random.nextFloat()) * 0.1F;
                    entityitem.motionZ += Math.sin(f3) * f2;
                }
                // FIFTH - Capture the entity maybe?
                if (currentState.spawnItemOrCapture(phaseContext, (EntityPlayer) (EntityPlayerBridge) this, entityitem)) {
                    return entityitem;
                }
                // TODO - Investigate whether player drops are adding to the stat list in captures.
                final ItemStack stack = entityitem.getItem();
                player.world.spawnEntity(entityitem);

                if (traceItem) {
                    if (!stack.isEmpty()) {
                        player.addStat(StatList.getDroppedObjectStats(stack.getItem()), droppedItem.getCount());
                    }

                    player.addStat(StatList.DROP);
                }

                return entityitem;
            }
        }
        // Sponge end
        final double d0 = this.posY - 0.30000001192092896D + (double) this.getEyeHeight();
        final EntityItem entityitem = new EntityItem(this.world, this.posX, d0, this.posZ, droppedItem);
        entityitem.setPickupDelay(40);

        if (traceItem) {
            entityitem.setThrower(this.shadow$getName());
        }

        if (dropAround) {
            final float f = this.rand.nextFloat() * 0.5F;
            final float f1 = this.rand.nextFloat() * ((float) Math.PI * 2F);
            entityitem.motionX = (double) (-MathHelper.sin(f1) * f);
            entityitem.motionZ = (double) (MathHelper.cos(f1) * f);
            entityitem.motionY = 0.20000000298023224D;
        } else {
            float f2 = 0.3F;
            entityitem.motionX =
                (double) (-MathHelper.sin(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f2);
            entityitem.motionZ =
                (double) (MathHelper.cos(this.rotationYaw * 0.017453292F) * MathHelper.cos(this.rotationPitch * 0.017453292F) * f2);
            entityitem.motionY = (double) (-MathHelper.sin(this.rotationPitch * 0.017453292F) * f2 + 0.1F);
            final float f3 = this.rand.nextFloat() * ((float) Math.PI * 2F);
            f2 = 0.02F * this.rand.nextFloat();
            entityitem.motionX += Math.cos((double) f3) * (double) f2;
            entityitem.motionY += (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
            entityitem.motionZ += Math.sin((double) f3) * (double) f2;
        }

        final ItemStack itemstack = this.dropItemAndGetStack(entityitem);

        if (traceItem) {
            if (itemstack != null && !itemstack.isEmpty()) { // Sponge - add null check
                this.addStat(StatList.getDroppedObjectStats(itemstack.getItem()), droppedItem.getCount());
            }

            this.addStat(StatList.DROP);
        }

        return entityitem;
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
    public ItemStack dropItemAndGetStack(final EntityItem entity) {
        this.world.spawnEntity(entity);
        return entity.getItem();
    }

    @Redirect(method = "collideWithPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onCollideWithPlayer(Lnet/minecraft/entity/player/EntityPlayer;)V")) // collideWithPlayer
    private void onPlayerCollideEntity(final net.minecraft.entity.Entity entity, final EntityPlayer player) {
        entity.onCollideWithPlayer(player);
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
            if (this.isPlayerSleeping() && !this.world.isRemote) {
                this.wakeUpPlayer(true, true, false);
            }

            // We just throw it to the superclass method so that we can potentially get the
            // onDeath method.
            cir.setReturnValue(super.attackEntityFrom(source, amount));
        }
    }

    /**
     * @author gabizou - April 8th, 2016
     * @author gabizou - April 11th, 2016 - Update for 1.9 - This enitre method was rewritten
     *
     *
     * @reason Rewrites the attackTargetEntityWithCurrentItem to throw an {@link AttackEntityEvent} prior
     * to the ensuing {@link DamageEntityEvent}. This should cover all cases where players are
     * attacking entities and those entities override {@link EntityLivingBase#attackEntityFrom(DamageSource, float)}
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
        if (!SpongeImplHooks.checkAttackEntity((EntityPlayer) (Object) this, targetEntity)) {
            return;
        }
        // Sponge End
        if (targetEntity.canBeAttackedWithItem()) {
            if (!targetEntity.hitByEntity((EntityPlayer) (Object) this)) {
                // Sponge Start - Prepare our event values
                // float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                final double originalBaseDamage = this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float damage = (float) originalBaseDamage;
                // Sponge End
                float enchantmentDamage = 0.0F;

                // Spogne Start - Redirect getting enchantments for our damage event handlers
                // if (targetEntity instanceof EntityLivingBase) {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
                // } else {
                //     enchantmentDamage = EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                // }
                final float attackStrength = this.getCooledAttackStrength(0.5F);

                final List<ModifierFunction<DamageModifier>> originalFunctions = new ArrayList<>();

                final EnumCreatureAttribute creatureAttribute = targetEntity instanceof EntityLivingBase
                                                                ? ((EntityLivingBase) targetEntity).getCreatureAttribute()
                                                                : EnumCreatureAttribute.UNDEFINED;
                final List<DamageFunction> enchantmentModifierFunctions = DamageEventHandler.createAttackEnchantmentFunction(this.getHeldItemMainhand(), creatureAttribute, attackStrength);
                // This is kept for the post-damage event handling
                final List<DamageModifier> enchantmentModifiers = enchantmentModifierFunctions.stream().map(ModifierFunction::getModifier).collect(Collectors.toList());

                enchantmentDamage = (float) enchantmentModifierFunctions.stream()
                        .map(ModifierFunction::getFunction)
                        .mapToDouble(function -> function.applyAsDouble(originalBaseDamage))
                        .sum();
                originalFunctions.addAll(enchantmentModifierFunctions);
                // Sponge End

                originalFunctions.add(DamageEventHandler.provideCooldownAttackStrengthFunction((EntityPlayer) (Object) this, attackStrength));
                damage = damage * (0.2F + attackStrength * attackStrength * 0.8F);
                enchantmentDamage = enchantmentDamage * attackStrength;
                this.resetCooldown();

                if (damage > 0.0F || enchantmentDamage > 0.0F) {
                    final boolean isStrongAttack = attackStrength > 0.9F;
                    boolean isSprintingAttack = false;
                    boolean isCriticalAttack = false;
                    boolean isSweapingAttack = false;
                    int knockbackModifier = 0;
                    knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackModifier((EntityPlayer) (Object) this);

                    if (this.isSprinting() && isStrongAttack) {
                        // Sponge - Only play sound after the event has be thrown and not cancelled.
                        // this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, SoundEvents.entity_player_attack_knockback, this.getSoundCategory(), 1.0F, 1.0F);
                        ++knockbackModifier;
                        isSprintingAttack = true;
                    }

                    isCriticalAttack = isStrongAttack && this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.shadow$isInWater() && !this.isPotionActive(MobEffects.BLINDNESS) && !this.isRiding() && targetEntity instanceof EntityLivingBase;
                    isCriticalAttack = isCriticalAttack && !this.isSprinting();

                    if (isCriticalAttack) {
                        // Sponge Start - add critical attack tuple
                        // damage *= 1.5F; // Sponge - This is handled in the event
                        originalFunctions.add(DamageEventHandler.provideCriticalAttackTuple((EntityPlayer) (Object) this));
                        // Sponge End
                    }

                    // damage = damage + enchantmentDamage; // Sponge - We don't need this since our event will re-assign the damage to deal
                    final double distanceWalkedDelta = (double) (this.distanceWalkedModified - this.prevDistanceWalkedModified);

                    final ItemStack heldItem = this.getHeldItem(EnumHand.MAIN_HAND);
                    if (isStrongAttack && !isCriticalAttack && !isSprintingAttack && this.onGround && distanceWalkedDelta < (double) this.getAIMoveSpeed()) {
                        final ItemStack itemstack = heldItem;

                        if (itemstack.getItem() instanceof ItemSword) {
                            isSweapingAttack = true;
                        }
                    }

                    // Sponge Start - Create the event and throw it
                    final DamageSource damageSource = DamageSource.causePlayerDamage((EntityPlayer) (Object) this);
                    final boolean isMainthread = !this.world.isRemote;
                    if (isMainthread) {
                        Sponge.getCauseStackManager().pushCause(damageSource);
                    }
                    final Cause attackCause = isMainthread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), damageSource);
                    final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(attackCause, originalFunctions,
                        (org.spongepowered.api.entity.Entity) targetEntity, knockbackModifier, originalBaseDamage);
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
                    final int fireAspectModifier = EnchantmentHelper.getFireAspectModifier((EntityPlayer) (Object) this);

                    if (targetEntity instanceof EntityLivingBase) {
                        targetOriginalHealth = ((EntityLivingBase) targetEntity).getHealth();

                        if (fireAspectModifier > 0 && !targetEntity.isBurning()) {
                            litEntityOnFire = true;
                            targetEntity.setFire(1);
                        }
                    }

                    final double targetMotionX = targetEntity.motionX;
                    final double targetMotionY = targetEntity.motionY;
                    final double targetMotionZ = targetEntity.motionZ;
                    final boolean attackSucceeded = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) (Object) this), damage);

                    if (attackSucceeded) {
                        if (knockbackModifier > 0) {
                            if (targetEntity instanceof EntityLivingBase) {
                                ((EntityLivingBase) targetEntity).knockBack((EntityPlayer) (Object) this, (float) knockbackModifier * 0.5F, (double) MathHelper.sin(this.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                            } else {
                                targetEntity.addVelocity((double) (-MathHelper.sin(this.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F), 0.1D, (double) (MathHelper.cos(this.rotationYaw * 0.017453292F) * (float) knockbackModifier * 0.5F));
                            }

                            this.motionX *= 0.6D;
                            this.motionZ *= 0.6D;
                            this.setSprinting(false);
                        }

                        if (isSweapingAttack) {
                            for (final EntityLivingBase entitylivingbase : this.world.getEntitiesWithinAABB(EntityLivingBase.class, targetEntity.getEntityBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
                                if (entitylivingbase != (EntityPlayer) (Object) this && entitylivingbase != targetEntity && !this.isOnSameTeam(entitylivingbase) && this.getDistanceSq(entitylivingbase) < 9.0D) {
                                    // Sponge Start - Do a small event for these entities
                                    // entitylivingbase.knockBack(this, 0.4F, (double)MathHelper.sin(this.rotationYaw * 0.017453292F), (double)(-MathHelper.cos(this.rotationYaw * 0.017453292F)));
                                    // entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(this), 1.0F);
                                    final EntityDamageSource sweepingAttackSource = EntityDamageSource.builder().entity((Player) this).type(DamageTypes.SWEEPING_ATTACK).build();
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
                                                .type(DamageModifierTypes.SWEEPING)
                                                .build(),
                                            incoming -> EnchantmentHelper.getSweepingDamageRatio((EntityPlayer) (Object) this) * attackDamage);
                                        final List<DamageFunction> sweapingFunctions = new ArrayList<>();
                                        sweapingFunctions.add(sweapingFunction);
                                        final Cause sweepingCause = isMainthread ? Sponge.getCauseStackManager().getCurrentCause() : Cause.of(EventContext.empty(), sweepingAttackSource);
                                        final AttackEntityEvent sweepingAttackEvent = SpongeEventFactory.createAttackEntityEvent(
                                            sweepingCause,
                                            sweapingFunctions, (org.spongepowered.api.entity.Entity) entitylivingbase, 1, 1.0D);
                                        SpongeImpl.postEvent(sweepingAttackEvent);
                                        if (!sweepingAttackEvent.isCancelled()) {
                                            entitylivingbase
                                                .knockBack((EntityPlayer) (Object) this, sweepingAttackEvent.getKnockbackModifier() * 0.4F,
                                                    (double) MathHelper.sin(this.rotationYaw * 0.017453292F),
                                                    (double) -MathHelper.cos(this.rotationYaw * 0.017453292F));
                                            entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) (Object) this),
                                                (float) sweepingAttackEvent.getFinalOutputDamage());
                                        }
                                    }
                                    // Sponge End
                                }
                            }

                            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, this.getSoundCategory(), 1.0F, 1.0F);
                            this.spawnSweepParticles();
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                            ((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = targetMotionX;
                            targetEntity.motionY = targetMotionY;
                            targetEntity.motionZ = targetMotionZ;
                        }

                        if (isCriticalAttack) {
                            this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, this.getSoundCategory(), 1.0F, 1.0F);
                            this.onCriticalHit(targetEntity);
                        }

                        if (!isCriticalAttack && !isSweapingAttack) {
                            if (isStrongAttack) {
                                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, this.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK , this.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (enchantmentDamage > 0.0F) {
                            this.onEnchantmentCritical(targetEntity);
                        }

                        this.setLastAttackedEntity(targetEntity);

                        if (targetEntity instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, (EntityPlayer) (Object) this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments((EntityPlayer) (Object) this, targetEntity);
                        final ItemStack itemstack1 = this.getHeldItemMainhand();
                        Entity entity = targetEntity;

                        if (targetEntity instanceof MultiPartEntityPart) {
                            final IEntityMultiPart ientitymultipart = ((MultiPartEntityPart) targetEntity).parent;

                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if(!itemstack1.isEmpty() && entity instanceof EntityLivingBase) {
                            itemstack1.hitEntity((EntityLivingBase) entity, (EntityPlayer) (Object) this);
                            if(itemstack1.isEmpty()) {
                                this.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase) {
                            final float f5 = targetOriginalHealth - ((EntityLivingBase) targetEntity).getHealth();
                            this.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));

                            if (fireAspectModifier > 0) {
                                targetEntity.setFire(fireAspectModifier * 4);
                            }

                            if (this.world instanceof WorldServer && f5 > 2.0F) {
                                final int k = (int) ((double) f5 * 0.5D);
                                ((WorldServer) this.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double) (targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1D, 0.0D, 0.1D, 0.2D, new int[0]);
                            }
                        }

                        this.addExhaustion(0.3F);
                    } else {
                        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, this.getSoundCategory(), 1.0F, 1.0F);

                        if (litEntityOnFire) {
                            targetEntity.extinguish();
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "setItemStackToSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/NonNullList;set(ILjava/lang/Object;)Ljava/lang/Object;"))
    private void onSetItemStackToSlot(final EntityEquipmentSlot slotIn, final ItemStack stack, final CallbackInfo ci)
    {
        if (((TrackedInventoryBridge) this.inventory).bridge$capturingInventory()) {
            if (slotIn == EntityEquipmentSlot.MAINHAND) {
                final ItemStack orig = this.inventory.mainInventory.get(this.inventory.currentItem);
                final Slot slot = ((PlayerInventory) this.inventory).getMain().getHotbar().getSlot(SlotIndex.of(this.inventory.currentItem)).get();
                ((TrackedInventoryBridge) this.inventory).bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            } else if (slotIn == EntityEquipmentSlot.OFFHAND) {
                final ItemStack orig = this.inventory.offHandInventory.get(0);
                final Slot slot = ((PlayerInventory) this.inventory).getOffhand();
                ((TrackedInventoryBridge) this.inventory).bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            } else if (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                final ItemStack orig = this.inventory.armorInventory.get(slotIn.getIndex());
                final Slot slot = ((PlayerInventory) this.inventory).getEquipment().getSlot(SlotIndex.of(slotIn.getIndex())).get();
                ((TrackedInventoryBridge) this.inventory).bridge$getCapturedSlotTransactions().add(new SlotTransaction(slot, ItemStackUtil.snapshotOf(orig), ItemStackUtil.snapshotOf(stack)));
            }
        }
    }

    @Redirect(method = "setDead", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/inventory/Container;onContainerClosed(Lnet/minecraft/entity/player/EntityPlayer;)V"))
    private void onOnContainerClosed(final Container container, final EntityPlayer player) {
        // Corner case where the server is shutting down on the client, the enitty player mp is also being killed off.
        if (Sponge.isServerAvailable() && SpongeImplHooks.isClientAvailable() && Sponge.getGame().getState() == GameState.SERVER_STOPPING) {
            container.onContainerClosed(player);
            return;
        }
        if (player instanceof EntityPlayerMP ) {
            final EntityPlayerMP serverPlayer = (EntityPlayerMP) player;


            try (final PhaseContext<?> ctx = PacketPhase.General.CLOSE_WINDOW.createPhaseContext()
                    .source(serverPlayer)
                    .packetPlayer(serverPlayer)
                    .openContainer(container)) {
                // intentionally missing the lastCursor to not double throw close event
                ctx.buildAndSwitch();
                final ItemStackSnapshot cursor = ItemStackUtil.snapshotOf(this.inventory.getItemStack());
                container.onContainerClosed(player);
                SpongeCommonEventFactory.callInteractInventoryCloseEvent(this.openContainer, serverPlayer, cursor, ItemStackSnapshot.NONE, false);
            }
        } else {
            // Proceed as normal with client code
            container.onContainerClosed(player);
        }
    }

    @Override
    public void bridge$shouldRestoreInventory(final boolean restore) {
        this.shouldRestoreInventory = restore;
    }

    @Override
    public boolean bridge$shouldRestoreInventory() {
        return this.shouldRestoreInventory;
    }

    @Override
    public boolean spongeImpl$isImmuneToFireForIgniteEvent() {
        return this.isSpectator() || this.isCreative();
    }

}
