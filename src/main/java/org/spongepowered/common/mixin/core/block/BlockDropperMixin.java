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
package org.spongepowered.common.mixin.core.block;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDropper;
import net.minecraft.dispenser.BlockSourceImpl;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.inventory.TrackedInventoryBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;

import javax.annotation.Nullable;

@Mixin(BlockDropper.class)
public abstract class BlockDropperMixin {

    @Inject(method = "dispense", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/TileEntityDispenser;setInventorySlotContents(ILnet/minecraft/item/ItemStack;)V"))
    private void afterDispense(final World worldIn, final BlockPos pos, final CallbackInfo callbackInfo,
            final BlockSourceImpl blocksourceimpl, final TileEntityDispenser tileentitydispenser, final int i, final ItemStack itemstack,
            final EnumFacing enumfacing, final BlockPos blockpos, final IInventory iinventory, final ItemStack itemstack1) {
        // after setInventorySlotContents
        tileentitydispenser.func_70299_a(i, itemstack1);
        // Transfer worked if remainder is one less than the original stack
        if (itemstack1.func_190916_E() == itemstack.func_190916_E() - 1) {
            final TrackedInventoryBridge capture = impl$forCapture(tileentitydispenser);
            final Inventory sourceInv = ((Inventory) tileentitydispenser);
            SpongeCommonEventFactory.captureTransaction(capture, sourceInv, i, itemstack);
            SpongeCommonEventFactory.callTransferPost(capture, sourceInv, ((Inventory) iinventory));
        }
        callbackInfo.cancel();
    }

    @Surrogate
    private void afterDispense(final World worldIn, final BlockPos pos, final CallbackInfo callbackInfo,
            final BlockSourceImpl blocksourceimpl, final TileEntityDispenser tileentitydispenser, final int i, final ItemStack itemstack,
            final ItemStack itemstack1) {
        // after setInventorySlotContents
        tileentitydispenser.func_70299_a(i, itemstack1);
        // Transfer worked if remainder is one less than the original stack
        if (itemstack1.func_190916_E() == itemstack.func_190916_E() - 1) {
            final TrackedInventoryBridge capture = impl$forCapture(tileentitydispenser);
            final Inventory sourceInv = ((Inventory) tileentitydispenser);
            SpongeCommonEventFactory.captureTransaction(capture, sourceInv, i, itemstack);
            final EnumFacing enumfacing = worldIn.func_180495_p(pos).func_177229_b(BlockDispenser.field_176441_a);
            final BlockPos blockpos = pos.func_177972_a(enumfacing);
            final IInventory iinventory = TileEntityHopper.func_145893_b(worldIn, (double)blockpos.func_177958_n(), (double)blockpos.func_177956_o(), (double)blockpos.func_177952_p());
            SpongeCommonEventFactory.callTransferPost(capture, sourceInv, ((Inventory) iinventory));
        }
        callbackInfo.cancel();
    }

    @Inject(method = "dispense", cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/tileentity/TileEntityHopper;putStackInInventoryAllSlots(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/item/ItemStack;"))
    private void onDispense(final World world, final BlockPos pos, final CallbackInfo ci,
            final BlockSourceImpl blocksourceimpl, final TileEntityDispenser tileentitydispenser, final int i, final ItemStack itemstack,
            final EnumFacing enumfacing, final BlockPos blockpos, final IInventory iinventory) {
        // Before putStackInInventoryAllSlots
        if (SpongeCommonEventFactory.callTransferPre(((Inventory) tileentitydispenser), ((Inventory) iinventory)).isCancelled()) {
            ci.cancel();
        }
    }

    @Nullable
    private static TrackedInventoryBridge impl$forCapture(final Object toCapture) {
        if (toCapture instanceof TrackedInventoryBridge) {
            return ((TrackedInventoryBridge) toCapture);
        }
        return null;
    }
}
