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
package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;
import org.spongepowered.common.world.BlockChange;

import javax.annotation.Nullable;

final class NoCaptureUseItemPacketState extends UseItemPacketState {

    @Override
    public boolean shouldCaptureBlockChangeOrSkip(BasicPacketContext phaseContext, BlockPos pos) {
        return false;
    }

    @Override
    public boolean requiresBlockBulkCaptures() {
        return false;
    }

    @Override
    public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, Transaction<BlockSnapshot> snapshotTransaction,
        BasicPacketContext context) {
        super.handleBlockChangeWithUser(blockChange, snapshotTransaction, context);
    }

    @Override
    public boolean doesCaptureEntityDrops() {
        return false;
    }

    @Override
    public boolean performOrCaptureItemDrop(BasicPacketContext phaseContext, Entity entity, EntityItem entityitem) {
        return false;
    }

    @Override
    public void unwind(BasicPacketContext context) {
        // We didn't capture anything, so there's nothing to do
    }
}
