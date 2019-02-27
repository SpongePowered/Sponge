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
package org.spongepowered.common.event.tracking.phase.tick;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.interfaces.block.IMixinBlock;

import javax.annotation.Nullable;

public final class NeighborNotificationContext extends LocationBasedTickContext<NeighborNotificationContext> {

    @Nullable private BlockPos sourceNotification;
    @Nullable private Block sourceNotifier;
    @Nullable private BlockPos notifiedBlockPos;
    @Nullable private IBlockState notifiedBlockState;
    @Nullable BlockSnapshot notificationSnapshot;

    NeighborNotificationContext(IPhaseState<NeighborNotificationContext> phaseState) {
        super(phaseState);
    }

    @Override
    public NeighborNotificationContext source(Object owner) {
        super.source(owner);
        if (owner instanceof LocatableBlock) {
            final IMixinBlock mixinBlock = BlockUtil.toMixin(((LocatableBlock) owner).getBlockState());
            this.setBlockEvents(mixinBlock.allowsBlockEventCreation())
                .setBulkBlockCaptures(mixinBlock.allowsBlockBulkCapture())
                .setEntitySpawnEvents(mixinBlock.allowsEntityEventCreation())
                .setBulkEntityCaptures(mixinBlock.allowsEntityBulkCapture());
        }
        return this;
    }

    @Override
    public PrettyPrinter printCustom(PrettyPrinter printer, int indent) {
        super.printCustom(printer, indent);
        String s = String.format("%1$"+indent+"s", "");
        if (this.sourceNotifier != null) {
            printer.add(s + "- %s: %s", "SourceBlock", this.sourceNotifier);
        }
        if (this.sourceNotification != null) {
            printer.add(s + "- %s: %s", "SourcePos", this.sourceNotification);
        }
        if (this.notifiedBlockState != null) {
            printer.add(s + "- %s: %s", "NotifiedBlockState", this.notifiedBlockState);
        }
        if (this.notifiedBlockPos != null) {
            printer.add(s + "- %s: %s", "NotifiedPos", this.notifiedBlockPos);
        }
        return printer;
    }

    public NeighborNotificationContext allowsCaptures(IPhaseState<?> state) {
        if (state.isWorldGeneration()) {
            this.setBlockEvents(false);
            this.setBulkBlockCaptures(false);
            this.setEntitySpawnEvents(false);
            this.setBulkEntityCaptures(false);
        }
        return this;
    }

    public NeighborNotificationContext sourceBlock(Block sourceBlock) {
        this.sourceNotifier = sourceBlock;
        return this;
    }

    public NeighborNotificationContext setSourceNotification(@Nullable BlockPos sourceNotification) {
        this.sourceNotification = sourceNotification;
        return this;
    }

    public NeighborNotificationContext setNotifiedBlockPos(@Nullable BlockPos notifiedBlockPos) {
        this.notifiedBlockPos = notifiedBlockPos;
        return this;
    }

    public NeighborNotificationContext setNotifiedBlockState(@Nullable IBlockState notifiedBlockState) {
        this.notifiedBlockState = notifiedBlockState;
        return this;
    }

    public NeighborNotificationContext setSourceNotification(@Nullable BlockSnapshot neighborNotificationSource) {
        this.notificationSnapshot = neighborNotificationSource;
        return this;
    }
}
