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
package org.spongepowered.common.mixin.core.item.inventory;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinContainer;
import org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter;

import java.util.ArrayList;
import java.util.List;

@NonnullByDefault
@Mixin(Container.class)
public abstract class MixinContainer implements org.spongepowered.api.item.inventory.Container, IMixinContainer {

    private boolean captureInventory = false;
    private List<SlotTransaction> capturedSlotTransactions = new ArrayList<>();

    @Shadow public List<Slot> inventorySlots;
    @Shadow public List<ItemStack> inventoryItemStacks;
    @Shadow public int windowId;
    @Shadow protected List<ICrafting> crafters;

    @SuppressWarnings("rawtypes")
    @Shadow
    public abstract List getInventory();

    @Shadow
    public abstract Slot getSlot(int slotId);

    /**
     * @author bloodmc
     *
     * Purpose: As we do not create a new player object on respawn, we
     * need to update the client with changes if listener already
     * exists.
     */
    @Overwrite
    public void onCraftGuiOpened(ICrafting listener) {
        Container container = (Container) (Object) this;
        if (this.crafters.contains(listener)) {
            // Sponge start
            // throw new IllegalArgumentException("Listener already listening");
            listener.updateCraftingInventory(container, this.getInventory());
            container.detectAndSendChanges();
            // Sponge end
        } else {
            this.crafters.add(listener);
            listener.updateCraftingInventory(container, this.getInventory());
            container.detectAndSendChanges();
        }
    }

    /**
     * @author bloodmc
     *
     * Purpose: All player inventory changes that need to be synced to
     * client flow through this method. Overwrite is used as no mod
     * should be touching this method.
     * 
     */
    @Overwrite
    public void detectAndSendChanges() {
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack itemstack = ((Slot) this.inventorySlots.get(i)).getStack();
            ItemStack itemstack1 = (ItemStack) this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                if (this.captureInventory) {
                    ItemStackSnapshot originalItem = itemstack1 == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) itemstack1).createSnapshot();
                    ItemStackSnapshot newItem = itemstack == null ? ItemStackSnapshot.NONE
                            : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();
                    SlotTransaction slotTransaction =
                            new SlotTransaction(new SlotAdapter(this.inventorySlots.get(i)), originalItem, newItem);
                    this.capturedSlotTransactions.add(slotTransaction);
                }

                itemstack1 = itemstack == null ? null : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);

                for (int j = 0; j < this.crafters.size(); ++j) {
                    ((ICrafting) this.crafters.get(j)).sendSlotContents((Container) (Object) this, i, itemstack1);
                }
            }
        }
    }

    @Inject(method = "putStackInSlot", at = @At(value = "HEAD") )
    public void onPutStackInSlot(int slotId, ItemStack itemstack, CallbackInfo ci) {
        if (this.captureInventory) {
            Slot slot = getSlot(slotId);
            if (slot != null) {
                ItemStackSnapshot originalItem = slot.getStack() == null ? ItemStackSnapshot.NONE
                        : ((org.spongepowered.api.item.inventory.ItemStack) slot.getStack()).createSnapshot();
                ItemStackSnapshot newItem =
                        itemstack == null ? ItemStackSnapshot.NONE : ((org.spongepowered.api.item.inventory.ItemStack) itemstack).createSnapshot();
                SlotTransaction slotTransaction = new SlotTransaction(new SlotAdapter(slot), originalItem, newItem);
                this.capturedSlotTransactions.add(slotTransaction);
            }
        }
    }

    @Override
    public boolean capturingInventory() {
        return this.captureInventory;
    }

    @Override
    public void setCaptureInventory(boolean flag) {
        this.captureInventory = flag;
    }

    @Override
    public List<SlotTransaction> getCapturedTransactions() {
        return this.capturedSlotTransactions;
    }

}
