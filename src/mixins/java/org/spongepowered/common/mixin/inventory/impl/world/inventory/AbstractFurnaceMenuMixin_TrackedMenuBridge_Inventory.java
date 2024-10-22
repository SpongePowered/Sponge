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

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.inventory.container.TrackedMenuBridge;

@Mixin(AbstractFurnaceMenu.class)
public abstract class AbstractFurnaceMenuMixin_TrackedMenuBridge_Inventory {

    @Inject(
        method = "<init>(Lnet/minecraft/world/inventory/MenuType;Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/inventory/RecipeBookType;ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;Lnet/minecraft/world/inventory/ContainerData;)V",
        at = @At("RETURN"))
    private void inventory$attachContainerMenu(
        final MenuType $$0, final RecipeType $$1, final ResourceKey $$2,
        final RecipeBookType $$3, final int $$4, final Inventory inventory, final Container container, final ContainerData $$7,
        final CallbackInfo ci) {
        if (inventory instanceof final TrackedMenuBridge trackedMenu) {
            trackedMenu.bridge$trackContainerMenu((AbstractContainerMenu) (Object) this);
        }

        if (container instanceof final TrackedMenuBridge trackedMenu) {
            trackedMenu.bridge$trackContainerMenu((AbstractContainerMenu) (Object) this);
        }
    }
}
