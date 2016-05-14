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
import net.minecraft.tileentity.TileEntityFurnace;
import org.spongepowered.api.block.tileentity.carrier.Furnace;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.tileentity.FurnaceData;
import org.spongepowered.api.gui.window.FurnaceWindow;

import javax.annotation.Nullable;

public class SpongeFurnaceWindow extends AbstractSpongeTileContainerWindow<TileEntityFurnace, Furnace> implements FurnaceWindow {

    private FurnaceData furnaceData;

    public SpongeFurnaceWindow() {
        super(TileEntityFurnace.class);
    }

    @Override
    protected TileEntityFurnace createVirtualTile() {
        return new TileEntityFurnace() {

            @Override
            public boolean isUseableByPlayer(EntityPlayer player) {
                return SpongeFurnaceWindow.this.players.contains(player);
            }
        };
    }

    @Override
    protected void updateVirtual() {
        // Can't use data processor as it references real world tile entity
        int passedBurnTime;
        int maxBurnTime;
        int passedCookTime;
        int maxCookTime;
        if (this.furnaceData == null) {
            passedBurnTime = maxBurnTime = passedCookTime = maxCookTime = 0;
        } else {
            passedBurnTime = this.furnaceData.getOrNull(Keys.PASSED_BURN_TIME);
            maxBurnTime = this.furnaceData.getOrNull(Keys.MAX_BURN_TIME);
            passedCookTime = this.furnaceData.getOrNull(Keys.PASSED_COOK_TIME);
            maxCookTime = this.furnaceData.getOrNull(Keys.MAX_COOK_TIME);
        }
        this.virtualTile.setField(0, maxBurnTime - passedBurnTime);
        this.virtualTile.setField(1, maxBurnTime);
        this.virtualTile.setField(2, passedCookTime);
        this.virtualTile.setField(3, maxCookTime);
    }

    @Override
    public void setVirtualFurnaceData(@Nullable FurnaceData furnaceData) {
        this.furnaceData = furnaceData;
        updateIfVirtual();
    }

    public static class Builder extends SpongeWindowBuilder<FurnaceWindow, FurnaceWindow.Builder> implements FurnaceWindow.Builder {

        @Override
        public FurnaceWindow build() {
            return new SpongeFurnaceWindow();
        }
    }

}
