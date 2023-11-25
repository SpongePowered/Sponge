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
package org.spongepowered.common.mixin.tracker.server.level;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.world.entity.PlatformEntityBridge;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.mixin.tracker.world.entity.player.PlayerMixin_Tracker;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin_Tracker extends PlayerMixin_Tracker {

    /**
     * @author gabizou - June 4th, 2016
     * @author i509VCB - February 17th, 2020 - 1.14.4
     * @author gabizou - December 31st, 2021 - 1.16.5
     * @reason We inject a construct event for the item drop and conveniently
     * can redirect the super call.
     */
    @Redirect(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"
        )
    )
    @Nullable
    private ItemEntity tracker$throwItemDrop(
        final Player thisPlayer, final ItemStack droppedItem, final boolean dropAround, final boolean traceItem
    ) {
        if (droppedItem.isEmpty()) {
            return null;
        }
        if (((PlatformEntityBridge) this).bridge$isFakePlayer()) {
            return super.shadow$drop(droppedItem, dropAround, traceItem);
        }
        if (((LevelBridge) this.shadow$level()).bridge$isFake()) {
            return super.shadow$drop(droppedItem, dropAround, traceItem);
        }

        final double posX1 = this.shadow$getX();
        final double posY1 = this.shadow$getEyeY() - (double) 0.3F;
        final double posZ1 = this.shadow$getZ();
        // Now the real fun begins.
        final ItemStack item;
        final ItemStackSnapshot snapshot = ItemStackUtil.snapshotOf(droppedItem);
        final List<ItemStackSnapshot> original = new ArrayList<>();
        original.add(snapshot);

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {

            item = SpongeCommonEventFactory.throwDropItemAndConstructEvent(
                (ServerPlayer) (Object) this, posX1, posY1, posZ1, snapshot, original, frame);

            if (item == null || item.isEmpty()) {
                return null;
            }

            // Here is where we would potentially perform item pre-merging (merge the item stacks with previously captured item stacks
            // and only if those stacks can be stacked (count increased). Otherwise, we'll just continue to throw the entity item.
            // For now, due to refactoring a majority of all of this code, pre-merging is disabled entirely.

            final ItemEntity itemEntity = new ItemEntity(this.shadow$level(), posX1, posY1, posZ1, item);
            itemEntity.setPickUpDelay(40);

            if (traceItem) {
                itemEntity.setThrower((ServerPlayer) (Object) this);
            }

            if (dropAround) {
                final float f = this.random.nextFloat() * 0.5F;
                final float f1 = this.random.nextFloat() * ((float) Math.PI * 2F);
                itemEntity.setDeltaMovement(-Mth.sin(f1) * f, 0.2F, Mth.cos(f1) * f);
            } else {
                final float f8 = Mth.sin(this.shadow$getXRot() * ((float) Math.PI / 180F));
                final float f2 = Mth.cos(this.shadow$getXRot() * ((float) Math.PI / 180F));
                final float f3 = Mth.sin(this.shadow$getYRot() * ((float) Math.PI / 180F));
                final float f4 = Mth.cos(this.shadow$getYRot() * ((float) Math.PI / 180F));
                final float f5 = this.random.nextFloat() * ((float) Math.PI * 2F);
                final float f6 = 0.02F * this.random.nextFloat();
                itemEntity.setDeltaMovement(
                    (double) (-f3 * f2 * 0.3F) + Math.cos(f5) * (double) f6,
                    (-f8 * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                    (double) (f4 * f2 * 0.3F) + Math.sin(f5) * (double) f6
                );
            }
            return itemEntity;
        }
    }
}
