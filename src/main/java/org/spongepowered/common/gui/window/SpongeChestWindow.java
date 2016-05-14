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
import net.minecraft.tileentity.TileEntityChest;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.gui.window.ChestWindow;

// TODO large chest
public class SpongeChestWindow extends AbstractSpongeTileContainerWindow<TileEntityChest, Chest> implements ChestWindow {

    public SpongeChestWindow() {
        super(TileEntityChest.class);
    }

    @Override
    protected TileEntityChest createVirtualTile() {
        return new TileEntityChest() {

            @Override
            public boolean isUseableByPlayer(EntityPlayer player) {
                return SpongeChestWindow.this.players.contains(player);
            }

            @Override
            public void openInventory(EntityPlayer player) {
                // Blank out this method
            }

            @Override
            public void closeInventory(EntityPlayer player) {
                // Blank out this method
            }
        };
    }

    public static class Builder extends SpongeWindowBuilder<ChestWindow, ChestWindow.Builder> implements ChestWindow.Builder {

        @Override
        public ChestWindow build() {
            return new SpongeChestWindow();
        }
    }

}
