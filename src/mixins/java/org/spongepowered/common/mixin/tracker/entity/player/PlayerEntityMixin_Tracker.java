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
package org.spongepowered.common.mixin.tracker.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.stats.Stat;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.tracker.entity.LivingEntityMixin_Tracker;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin_Tracker extends LivingEntityMixin_Tracker {

    //@formatter:off
    @Shadow @Final public PlayerInventory inventory;

    @Shadow public abstract void shadow$addStat(Stat<?> stat);
    @Shadow public abstract void shadow$addStat(ResourceLocation stat);
    @Shadow public abstract ITextComponent shadow$getDisplayName();
    @Shadow protected abstract void shadow$spawnShoulderEntities();
    @Shadow protected abstract void shadow$destroyVanishingCursedItems();
    @Shadow public abstract String shadow$getScoreboardName();
    @Shadow public abstract Scoreboard shadow$getWorldScoreboard();
    @Shadow public abstract boolean shadow$isSpectator();
    @Shadow public abstract void shadow$takeStat(Stat<?> stat);
    @Shadow public abstract void shadow$addStat(Stat<?> stat, int amount);
    //@formatter:on

    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    public void impl$callDestructEntityDeath(DamageSource cause, CallbackInfo ci) {
        if (this.shadow$isServerWorld()) {
            // Sponge start - Fire DestructEntityEvent.Death
            final DestructEntityEvent.Death event = SpongeCommonEventFactory.callDestructEntityEventDeath((PlayerEntity) (Object) this, cause);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    /**
     * @author gabizou - June 4th, 2016
     * @author i509VCB - February 17th, 2020 - 1.14.4
     *
     * @reason When a player drops an item, all methods flow through here instead of {@link Entity#entityDropItem(IItemProvider, int)}
     * because of the idea of {@code dropAround} and {@code traceItem}.
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

            final double posX1 = player.getPosX();
            final double posY1 = player.getPosY() - 0.3 + player.getEyeHeight();
            final double posZ1 = player.getPosZ();
            // Now the real fun begins.
            final ItemStack item;
            final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(droppedItem);
            final List<ItemStackSnapshot> original = new ArrayList<>();
            original.add(snapshot);

            try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {

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
                    final float f8 = MathHelper.sin(this.rotationPitch * ((float)Math.PI / 180F));
                    final float f2 = MathHelper.cos(this.rotationPitch * ((float)Math.PI / 180F));
                    final float f3 = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
                    final float f4 = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
                    final float f5 = this.rand.nextFloat() * ((float)Math.PI * 2F);
                    final float f6 = 0.02F * this.rand.nextFloat();
                    itemEntity.setMotion((double)(-f3 * f2 * 0.3F) + Math.cos(f5) * (double)f6, (-f8 * 0.3F + 0.1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), (double)(f4 * f2 * 0.3F) + Math.sin(f5) * (double)f6);
                }

                return itemEntity;
            }
        }
        // Sponge end
        final double d0 = this.posY - 0.30000001192092896D + (double) this.getEyeHeight();
        final ItemEntity itemEntity = new ItemEntity(this.world, this.posX, d0, this.posZ, droppedItem);
        itemEntity.setPickupDelay(40);

        if (traceItem) {
            itemEntity.setThrowerId(this.getUniqueID());
        }

        if (dropAround) {
            final float f = this.rand.nextFloat() * 0.5F;
            final float f1 = this.rand.nextFloat() * ((float) Math.PI * 2F);
            itemEntity.setMotion(-MathHelper.sin(f1) * f, 0.2F, MathHelper.cos(f1) * f);
        } else {
            final float f8 = MathHelper.sin(this.rotationPitch * ((float)Math.PI / 180F));
            final float f2 = MathHelper.cos(this.rotationPitch * ((float)Math.PI / 180F));
            final float f3 = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
            final float f4 = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
            final float f5 = this.rand.nextFloat() * ((float)Math.PI * 2F);
            final float f6 = 0.02F * this.rand.nextFloat();
            itemEntity.setMotion((double)(-f3 * f2 * 0.3F) + Math.cos(f5) * (double)f6, (-f8 * 0.3F + 0.1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F), (double)(f4 * f2 * 0.3F) + Math.sin(f5) * (double)f6);
        }

        return itemEntity;
    }
}