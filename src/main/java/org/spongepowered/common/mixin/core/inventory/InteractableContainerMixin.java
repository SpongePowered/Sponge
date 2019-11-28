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
package org.spongepowered.common.mixin.core.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.BeaconContainer;
import net.minecraft.inventory.container.BrewingStandContainer;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.DispenserContainer;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.FurnaceContainer;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.inventory.container.HorseInventoryContainer;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.WorkbenchContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = {
        ChestContainer.class,
        HopperContainer.class,
        DispenserContainer.class,
        FurnaceContainer.class,
        EnchantmentContainer.class,
        RepairContainer.class,
        BrewingStandContainer.class,
        BeaconContainer.class,
        HorseInventoryContainer.class,
        MerchantContainer.class,
        PlayerContainer.class,
        WorkbenchContainer.class
})
public abstract class InteractableContainerMixin extends ContainerMixin {

    @Inject(method = "canInteractWith", at = @At("HEAD"), cancellable = true)
    private void impl$canInteractWith(final PlayerEntity playerIn, final CallbackInfoReturnable<Boolean> cir) {
        if (this.impl$canInteractWithPredicate != null) {
            cir.setReturnValue(this.impl$canInteractWithPredicate.test(playerIn));
        }
    }

}
