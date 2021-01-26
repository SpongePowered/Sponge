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
package org.spongepowered.common.mixin.inventory.impl.world.inventory;

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
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_Menu_Inventory implements MenuBridge {

    // @formatter:off
    @Shadow @Final private List<ContainerListener> containerListeners;
    @Shadow @Final private NonNullList<ItemStack> lastSlots;
    @Shadow @Final public List<Slot> slots;
    // @formatter:on

    @Nullable private SpongeInventoryMenu impl$menu;

    @Override
    public void bridge$setMenu(SpongeInventoryMenu menu) {
        this.impl$menu = menu;
    }

    @Nullable @Override
    public SpongeInventoryMenu bridge$getMenu() {
        return this.impl$menu;
    }

    // Called when clicking in an inventory
    // InventoryMenu Callback
    @Inject(method = "doClick", at = @At(value = "HEAD"), cancellable = true)
    private void impl$onClick(int slotId, int dragType, ClickType clickTypeIn, Player player, CallbackInfoReturnable<ItemStack> cir) {
        final SpongeInventoryMenu menu = this.bridge$getMenu();
        if (menu != null) {
            menu.setOldCursor(player.inventory.getCarried().copy());
            if (!menu.onClick(slotId, dragType, clickTypeIn, player, (org.spongepowered.api.item.inventory.Container) this)) {
                cir.setReturnValue(ItemStack.EMPTY);
                // Accept all changes made by plugin
                for (int i = 0; i < this.slots.size(); i++) {
                    Slot slot = this.slots.get(i);
                    this.lastSlots.set(i, slot.getItem().copy());
                }
                // and update client
                for (ContainerListener listener : this.containerListeners) {
                    listener.refreshContainer((AbstractContainerMenu) (Object) this, this.lastSlots);
                }
            }
        }
    }

    // Called when a Container is closed
    // InventoryMenu Callback and resetting viewed and menu state
    @Inject(method = "removed", at = @At(value = "HEAD"))
    private void onOnContainerClosed(Player player, CallbackInfo ci) {
        SpongeInventoryMenu menu = this.bridge$getMenu();
        if (menu != null) {
            menu.onClose(player, (org.spongepowered.api.item.inventory.Container) this);
        }
        this.bridge$setMenu(null);
    }

}
