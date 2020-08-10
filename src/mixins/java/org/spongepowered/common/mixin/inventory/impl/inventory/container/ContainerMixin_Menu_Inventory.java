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
package org.spongepowered.common.mixin.inventory.impl.inventory.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.inventory.ViewableInventoryBridge;
import org.spongepowered.common.bridge.inventory.container.MenuBridge;
import org.spongepowered.common.inventory.custom.SpongeInventoryMenu;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(Container.class)
public abstract class ContainerMixin_Menu_Inventory implements MenuBridge {

    @Shadow @Final private List<IContainerListener> listeners;
    @Shadow @Final private NonNullList<ItemStack> inventoryItemStacks;

    @Shadow @Final public List<Slot> inventorySlots;
    @Nullable private SpongeInventoryMenu impl$menu;

    @Override
    public void bridge$setMenu(SpongeInventoryMenu menu) {
        this.impl$menu = menu;
    }

    @Nullable @Override
    public SpongeInventoryMenu bridge$getMenu() {
        return this.impl$menu;
    }


    @Nullable private Object impl$viewed;

    private void impl$setViewed(@Nullable Object viewed) {
        if (viewed == null) {
            this.impl$unTrackViewable(this.impl$viewed);
        }
        this.impl$viewed = viewed;
    }

    private void impl$unTrackViewable(@Nullable Object inventory) {
        if (inventory instanceof Carrier) {
            inventory = ((Carrier) inventory).getInventory();
        }
        if (inventory instanceof Inventory) {
            ((Inventory) inventory).asViewable().ifPresent(i -> ((ViewableInventoryBridge) i).viewableBridge$removeContainer(((Container) (Object) this)));
        }
        // TODO else unknown inventory - try to provide wrapper Interactable
    }



    // Called when clicking in an inventory
    // InventoryMenu Callback
    @Inject(method = "slotClick", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player, CallbackInfoReturnable<ItemStack> cir) {
        final SpongeInventoryMenu menu = this.bridge$getMenu();
        if (menu != null) {
            menu.setOldCursor(player.inventory.getItemStack().copy());
            if (!menu.onClick(slotId, dragType, clickTypeIn, player, (org.spongepowered.api.item.inventory.Container) this)) {
                cir.setReturnValue(ItemStack.EMPTY);
                // Accept all changes made by plugin
                for (int i = 0; i < this.inventorySlots.size(); i++) {
                    Slot slot = this.inventorySlots.get(i);
                    this.inventoryItemStacks.set(i, slot.getStack().copy());
                }
                // and update client
                for (IContainerListener listener : this.listeners) {
                    listener.sendAllContents((Container) (Object) this, this.inventoryItemStacks);
                }
            }
        }
    }

    // Called when a Container is closed
    // InventoryMenu Callback and resetting viewed and menu state
    @Inject(method = "onContainerClosed", at = @At(value = "HEAD"))
    private void onOnContainerClosed(PlayerEntity player, CallbackInfo ci) {
        SpongeInventoryMenu menu = this.bridge$getMenu();
        if (menu != null) {
            menu.onClose(player, (org.spongepowered.api.item.inventory.Container) this);
        }
        this.impl$setViewed(null);
        this.bridge$setMenu(null);
    }


}
