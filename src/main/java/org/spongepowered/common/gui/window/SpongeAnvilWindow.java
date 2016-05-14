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
package org.spongepowered.common.gui.window;

import net.minecraft.block.BlockAnvil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import org.spongepowered.api.gui.window.AnvilWindow;

public class SpongeAnvilWindow extends AbstractSpongeBlockContainerWindow implements AnvilWindow {

    @Override
    protected IInteractionObject provideInteractionObject(World world, BlockPos pos) {
        return new BlockAnvil.Anvil(world, pos);
    }

    @Override
    protected Container createVirtualContainer(IInteractionObject obj, InventoryPlayer inventory, EntityPlayerMP player) {
        return new ContainerRepair(inventory, player.worldObj, VIRTUAL_POS, player) {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return SpongeAnvilWindow.this.players.contains(playerIn);
            }
        };
    }

    public static class Builder extends SpongeWindowBuilder<AnvilWindow, AnvilWindow.Builder> implements AnvilWindow.Builder {

        @Override
        public AnvilWindow build() {
            return new SpongeAnvilWindow();
        }
    }

}
