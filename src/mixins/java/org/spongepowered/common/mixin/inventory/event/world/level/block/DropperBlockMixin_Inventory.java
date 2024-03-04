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
package org.spongepowered.common.mixin.inventory.event.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.accessor.world.level.block.entity.HopperBlockEntityAccessor;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.inventory.InventoryEventFactory;
import org.spongepowered.common.inventory.util.InventoryUtil;

@Mixin(DropperBlock.class)
public abstract class DropperBlockMixin_Inventory {

    @Inject(method = "dispenseFrom", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/DispenserBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private void impl$afterDispense(final ServerLevel worldIn, final BlockState state, final BlockPos pos, final CallbackInfo callbackInfo,
            final DispenserBlockEntity dispensertileentity, final BlockSource proxyblocksource, final int i, final ItemStack itemstack,
            final Direction direction, final Container iinventory, final ItemStack itemstack1) {
        // after setItem
        dispensertileentity.setItem(i, itemstack1);

        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            // Transfer worked if remainder is one less than the original stack
            if (itemstack1.getCount() == itemstack.getCount() - 1) {
                final TrackedInventoryBridge capture = InventoryUtil.forCapture(dispensertileentity);
                final Inventory sourceInv = ((Inventory) dispensertileentity);
                final SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, sourceInv, i, itemstack);
                InventoryEventFactory.callTransferPost(capture, sourceInv, ((Inventory) iinventory), itemstack, sourceSlotTransaction);
            }
        }

        // dont call setItem twice
        callbackInfo.cancel();
    }

    @Surrogate
    private void afterDispense(final ServerLevel worldIn, final BlockState state, final BlockPos pos, final CallbackInfo callbackInfo,
            final DispenserBlockEntity dispensertileentity, final BlockSource proxyblocksource, final int i, final ItemStack itemstack,
            final ItemStack itemstack1) {
        // after setItem
        dispensertileentity.setItem(i, itemstack1);

        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            // Transfer worked if remainder is one less than the original stack
            if (itemstack1.getCount() == itemstack.getCount() - 1) {
                final TrackedInventoryBridge capture = InventoryUtil.forCapture(dispensertileentity);
                final Inventory sourceInv = ((Inventory) dispensertileentity);
                final SlotTransaction sourceSlotTransaction = InventoryEventFactory.captureTransaction(capture, sourceInv, i, itemstack);
                final Direction enumfacing = worldIn.getBlockState(pos).getValue(DispenserBlock.FACING);
                final BlockPos blockpos = pos.relative(enumfacing);
                final Container iinventory = HopperBlockEntityAccessor.invoker$getContainerAt(worldIn, blockpos);
                InventoryEventFactory.callTransferPost(capture, sourceInv, ((Inventory) iinventory), itemstack, sourceSlotTransaction);
            }
        }

        // dont call setInventorySlotContents twice
        callbackInfo.cancel();
    }

    @Inject(method = "dispenseFrom", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"))
    private void onDispense(final ServerLevel world, final BlockState state, final BlockPos pos, final CallbackInfo ci,
            final DispenserBlockEntity dispensertileentity, final BlockSource proxyblocksource, final int i, final ItemStack itemstack,
            final Direction direction, final Container iinventory) {
        // Before putStackInInventoryAllSlots
        if (InventoryEventFactory.callTransferPre(((Inventory) dispensertileentity), ((Inventory) iinventory)).isCancelled()) {
            ci.cancel();
        }
    }
}
