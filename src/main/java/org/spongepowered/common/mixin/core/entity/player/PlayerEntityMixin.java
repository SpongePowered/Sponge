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
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
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
import org.spongepowered.common.data.provider.entity.player.ExperienceHolderUtils;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.core.entity.LivingEntityMixin;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3d;

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
    @Shadow @Nullable public abstract ItemEntity shadow$dropItem(final ItemStack droppedItem, final boolean dropAround, final boolean traceItem);
    @Shadow public abstract FoodStats shadow$getFoodStats();
    @Shadow public abstract GameProfile shadow$getGameProfile();
    @Shadow public abstract Scoreboard shadow$getWorldScoreboard();
    @Shadow public abstract boolean shadow$isCreative();
    @Shadow public boolean shadow$canAttackPlayer(final PlayerEntity other) {
        return false;
    }
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

    @Inject(method = "onEnchant", at = @At("RETURN"))
    private void onEnchantChangeExperienceLevels(final ItemStack item, final int levels, final CallbackInfo ci) {
        this.bridge$recalculateTotalExperience();
    }

    @Inject(method = "readAdditional", at = @At("RETURN"))
    private void recalculateXpOnLoad(final CompoundNBT compound, final CallbackInfo ci) {
        // Fix the mistakes of /xp commands past.
        this.bridge$recalculateTotalExperience();
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
