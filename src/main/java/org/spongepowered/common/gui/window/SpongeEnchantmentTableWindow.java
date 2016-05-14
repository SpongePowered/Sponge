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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.world.IInteractionObject;
import org.spongepowered.api.block.tileentity.EnchantmentTable;
import org.spongepowered.api.gui.window.EnchantmentTableWindow;

public class SpongeEnchantmentTableWindow extends AbstractSpongeTileContainerWindow<TileEntityEnchantmentTable, EnchantmentTable>
        implements EnchantmentTableWindow {

    public SpongeEnchantmentTableWindow() {
        super(TileEntityEnchantmentTable.class);
    }

    @Override
    protected TileEntityEnchantmentTable createVirtualTile() {
        return new TileEntityEnchantmentTable(); // No changes here
    }

    @Override
    protected boolean shouldCreateVirtualContainer() {
        return isVirtual();
    }

    @Override
    protected Container createVirtualContainer(IInteractionObject obj, InventoryPlayer inventory, EntityPlayerMP player) {
        return new ContainerEnchantment(inventory, player.worldObj, VIRTUAL_POS) {

            @Override
            public boolean canInteractWith(EntityPlayer playerIn) {
                return SpongeEnchantmentTableWindow.this.players.contains(playerIn);
            }
        };
    }

    public static class Builder extends SpongeWindowBuilder<EnchantmentTableWindow, EnchantmentTableWindow.Builder> implements EnchantmentTableWindow.Builder {

        @Override
        public EnchantmentTableWindow build() {
            return new SpongeEnchantmentTableWindow();
        }
    }

}
