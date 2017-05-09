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
package org.spongepowered.common.mixin.core.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;

import javax.annotation.Nullable;

@Mixin(BlockEventData.class)
public abstract class MixinBlockEventData implements IMixinBlockEventData {

    private boolean captureBlocks = true;
    @Nullable private LocatableBlock tickBlock = null;
    @Nullable private TileEntity tickTileEntity = null;
    @Nullable private User sourceUser = null;

    @Shadow public abstract BlockPos getPosition();
    @Shadow public abstract int getEventID();
    @Shadow public abstract int getEventParameter();
    @Shadow public abstract Block getBlock();

    @Override
    public BlockPos getEventBlockPosition() {
        return getPosition();
    }

    @Override
    public boolean getCaptureBlocks() {
        return this.captureBlocks;
    }

    @Override
    public int getEventBlockID() {
        return getEventID();
    }

    @Override
    public int getEventBlockParameter() {
        return getEventParameter();
    }

    @Override
    public Block getEventBlock() {
        return getBlock();
    }

    @Nullable
    @Override
    public LocatableBlock getTickBlock() {
        return this.tickBlock;
    }

    @Override
    public void setTickBlock(@Nullable LocatableBlock tickBlock) {
        this.tickBlock = tickBlock;
    }

    @Nullable
    @Override
    public TileEntity getTickTileEntity() {
        return this.tickTileEntity;
    }

    @Override
    public void setTickTileEntity(@Nullable TileEntity tickTileEntity) {
        this.tickTileEntity = tickTileEntity;
    }

    @Nullable
    @Override
    public User getSourceUser() {
        return this.sourceUser;
    }

    @Override
    public void setSourceUser(User user) {
        this.sourceUser = user;
    }

    @Override
    public void setCaptureBlocks(boolean capture) {
        this.captureBlocks = capture;
    }
}
