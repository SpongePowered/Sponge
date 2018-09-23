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
package org.spongepowered.common.mixin.core.tileentity;

import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.event.SpongeCommonEventFactory.toInventory;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.block.tileentity.carrier.Hopper;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.tileentity.CooldownData;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinInventory;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.item.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.inventory.lens.Fabric;
import org.spongepowered.common.item.inventory.lens.SlotProvider;
import org.spongepowered.common.item.inventory.lens.comp.GridInventoryLens;
import org.spongepowered.common.item.inventory.lens.impl.ReusableLens;
import org.spongepowered.common.item.inventory.lens.impl.collections.SlotLensCollection;
import org.spongepowered.common.item.inventory.lens.impl.comp.GridInventoryLensImpl;
import org.spongepowered.common.item.inventory.util.InventoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

@SuppressWarnings("rawtypes")
@NonnullByDefault
@Mixin(TileEntityHopper.class)
public abstract class MixinTileEntityHopper extends MixinTileEntityLockableLoot implements Hopper, IMixinInventory {

    @Shadow public int transferCooldown;
    @Shadow private static ItemStack insertStack(IInventory source, IInventory destination, ItemStack stack, int index, EnumFacing direction) {
        throw new AbstractMethodError("Shadow");
    }
    @Shadow public static IInventory getSourceInventory(IHopper hopper) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow private static boolean isInventoryEmpty(IInventory inventoryIn, EnumFacing side) {
        throw new AbstractMethodError("Shadow");
    }

    @Shadow protected abstract boolean isInventoryFull(IInventory inventoryIn, EnumFacing side);

    public List<SlotTransaction> capturedTransactions = new ArrayList<>();

    @Override
    public List<SlotTransaction> getCapturedTransactions() {
        return this.capturedTransactions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ReusableLens<?> generateLens(Fabric fabric, InventoryAdapter adapter) {
        return ReusableLens.getLens(GridInventoryLens.class, ((InventoryAdapter) this), this::generateSlotProvider, this::generateRootLens);
    }

    @SuppressWarnings("unchecked")
    private SlotProvider generateSlotProvider() {
        return new SlotLensCollection.Builder().add(5).build();
    }

    @SuppressWarnings("unchecked")
    private GridInventoryLens generateRootLens(SlotProvider slots) {
        Class<? extends InventoryAdapter> thisClass = ((Class) this.getClass());
        return new GridInventoryLensImpl(0, 5, 1, thisClass, slots);
    }

    @Inject(method = "putDropInInventoryAllSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityItem;getItem()Lnet/minecraft/item/ItemStack;"))
    private static void onPutDrop(IInventory inventory, IInventory hopper, EntityItem entityItem, CallbackInfoReturnable<Boolean> callbackInfo) {
        ((IMixinEntity) entityItem).getCreatorUser().ifPresent(owner -> {
            if (inventory instanceof TileEntity) {
                TileEntity te = (TileEntity) inventory;
                IMixinChunk spongeChunk = ((IMixinTileEntity) te).getActiveChunk();
                spongeChunk.addTrackedBlockPosition(te.getBlockType(), te.getPos(), owner, PlayerTracker.Type.NOTIFIER);
            }
        });
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = super.toContainer();
        return container.set(of("TransferCooldown"), this.transferCooldown);
    }

    @Override
    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
        super.supplyVanillaManipulators(manipulators);
        Optional<CooldownData> cooldownData = get(CooldownData.class);
        if (cooldownData.isPresent()) {
            manipulators.add(cooldownData.get());
        }
    }

    // Call PreEvents

    @Redirect(method = "pullItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryEmpty(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/EnumFacing;)Z"))
    private static boolean onIsInventoryEmpty(IInventory inventory, EnumFacing facing, IHopper hopper) {
        boolean result = isInventoryEmpty(inventory, facing);
        if (result || !ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            return result;
        }
        return SpongeCommonEventFactory.callTransferPre(toInventory(inventory), toInventory(hopper)).isCancelled();
    }

    @Redirect(method = "transferItemsOut", at = @At(value = "INVOKE",
              target = "Lnet/minecraft/tileentity/TileEntityHopper;isInventoryFull(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/util/EnumFacing;)Z"))
    private boolean onIsInventoryFull(TileEntityHopper hopper, IInventory inventory, EnumFacing enumfacing) {
        boolean result = this.isInventoryFull(inventory, enumfacing);
        if (result || !ShouldFire.TRANSFER_INVENTORY_EVENT_PRE) {
            return result;
        }
        return SpongeCommonEventFactory.callTransferPre(toInventory(hopper), toInventory(inventory)).isCancelled();
    }

    // Capture Transactions

    @Redirect(remap = false, method = "putStackInInventoryAllSlots", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/tileentity/TileEntityHopper;insertStack(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/EnumFacing;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack onInsertStack(IInventory source, IInventory destination, ItemStack stack, int index, EnumFacing direction) {
        ItemStack result = insertStack(source, destination, stack, index, direction);
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST) {
            if (result.isEmpty()) {
                Inventory inv = InventoryUtil.toInventory(destination, null);
                Optional<org.spongepowered.api.item.inventory.Slot> sl = inv.getSlot(SlotIndex.of(index));
                if (sl.isPresent()) {
                    // We don't actually use this transaction we just have to save the modified Slot somewhere
                    InventoryUtil.forCapture(source).getCapturedTransactions().add(new SlotTransaction(sl.get(), ItemStackSnapshot.NONE, ItemStackSnapshot.NONE));
                }
            }
        }
        return result;
    }

    // Post Captured Transactions

    @Inject(method = "transferItemsOut", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 1))
    private void afterPutStackInSlots(CallbackInfoReturnable<Boolean> cir, IInventory iInventory, EnumFacing enumFacing, int i, ItemStack itemStack, ItemStack itemStack1) {
        // after putStackInInventoryAllSlots if the transfer worked
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST && itemStack1.isEmpty()) {
            ItemStack insertStack = itemStack.copy();
            insertStack.setCount(1);

            List<SlotTransaction> list = InventoryUtil.forCapture(this).getCapturedTransactions();
            if (!list.isEmpty()) {
                Slot dSlot = list.get(0).getSlot();
                list.clear();
                Inventory sInv = toInventory(this);
                Optional<Slot> sSlot = sInv.getSlot(SlotIndex.of(i));
                if (sSlot.isPresent()) {
                    Inventory dInv = toInventory(iInventory);
                    SpongeCommonEventFactory.callTransferPost(sInv, dInv, sSlot.get(), dSlot, insertStack);
                }
            }
        }
    }


    @Inject(method = "pullItemFromSlot", locals = LocalCapture.CAPTURE_FAILEXCEPTION,
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/inventory/IInventory;markDirty()V"))
    private static void afterPullItemFromSlot(IHopper hopper, IInventory iInventory, int index, EnumFacing direction, CallbackInfoReturnable<Boolean> cir,
            ItemStack itemStack, ItemStack itemStack1, ItemStack itemStack2) {
        // after putStackInInventoryAllSlots if the transfer worked
        if (ShouldFire.TRANSFER_INVENTORY_EVENT_POST && itemStack2.isEmpty()) {

            ItemStack insertStack = itemStack1.copy();
            insertStack.setCount(1);

            List<SlotTransaction> list = InventoryUtil.forCapture(hopper).getCapturedTransactions();
            if (!list.isEmpty()) {
                Slot dSlot = list.get(0).getSlot();
                list.clear();
                Inventory sInv = toInventory(hopper);
                Optional<Slot> sSlot = sInv.getSlot(SlotIndex.of(index));
                if (sSlot.isPresent()) {
                    Inventory dInv = toInventory(iInventory);
                    SpongeCommonEventFactory.callTransferPost(sInv, dInv, sSlot.get(), dSlot, insertStack);
                }
            }
        }
    }

    @Redirect(method = "putDropInInventoryAllSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntityHopper;putStackInInventoryAllSlots(Lnet/minecraft/inventory/IInventory;Lnet/minecraft/inventory/IInventory;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/item/ItemStack;"))
    private static ItemStack onPutStackInInventoryAllSlots(IInventory source, IInventory destination, ItemStack stack, EnumFacing direction, IInventory s2, IInventory d2, EntityItem entity) {
        return SpongeCommonEventFactory.callInventoryPickupEvent(destination, entity, stack);
    }

}
