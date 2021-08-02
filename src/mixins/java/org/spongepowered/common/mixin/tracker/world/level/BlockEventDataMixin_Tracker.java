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
package org.spongepowered.common.mixin.tracker.world.level;

import net.minecraft.world.level.BlockEventData;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.level.TrackableBlockEventDataBridge;

import java.util.UUID;

@Mixin(BlockEventData.class)
public abstract class BlockEventDataMixin_Tracker implements TrackableBlockEventDataBridge {

    @Nullable private LocatableBlock bridge$TickingBlock = null;
    @Nullable private BlockEntity bridge$TileEntity = null;
    @Nullable private UUID bridge$sourceUser = null;

    @Nullable
    @Override
    public LocatableBlock bridge$getTickingLocatable() {
        return this.bridge$TickingBlock;
    }

    @Override
    public void bridge$setTickingLocatable(@Nullable final LocatableBlock tickBlock) {
        this.bridge$TickingBlock = tickBlock;
    }

    @Nullable
    @Override
    public BlockEntity bridge$getTileEntity() {
        return this.bridge$TileEntity;
    }

    @Override
    public void bridge$setTileEntity(@Nullable final BlockEntity bridge$TileEntity) {
        this.bridge$TileEntity = bridge$TileEntity;
    }

    @Override
    public @Nullable UUID bridge$getSourceUserUUID() {
        return this.bridge$sourceUser;
    }

    @Override
    public void bridge$setSourceUserUUID(final @Nullable UUID user) {
        this.bridge$sourceUser = user;
    }

}