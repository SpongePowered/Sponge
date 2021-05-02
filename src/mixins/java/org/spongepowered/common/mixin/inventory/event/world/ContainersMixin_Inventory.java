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
package org.spongepowered.common.mixin.inventory.event.world;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.inventory.util.ContainerUtil;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(Containers.class)
public abstract class ContainersMixin_Inventory {

    @Redirect(method = "dropContents(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/Container;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/Containers;dropContents(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/Container;)V"))
    private static void impl$dropItemsAndThrowEvents(final Level world, final double x, final double y, final double z, final Container inventory) {
        if (world instanceof ServerLevel) {
            // Don't drop items if we are restoring blocks
            if (PlatformHooks.INSTANCE.getWorldHooks().isRestoringBlocks(world)) {
                return;
            }
            ContainerUtil.performBlockInventoryDrops((ServerLevel) world, x, y, z, inventory);
        } else {
            for (int i = 0; i < inventory.getContainerSize(); ++i) {
                final ItemStack itemstack = inventory.getItem(i);

                if (!itemstack.isEmpty()) {
                    Containers.dropItemStack(world, x, y, z, itemstack);
                }
            }
        }
    }

}
