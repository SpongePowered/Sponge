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
package org.spongepowered.common.event.tracking.context.transaction;

import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockState;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.accessor.world.server.ServerWorldAccessor;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.block.TrackerBlockEventDataBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.Optional;
import java.util.function.BiConsumer;

public class AddBlockEventTransaction extends BlockEventBasedTransaction {

    private final BlockEventData blockEvent;
    private final SpongeBlockSnapshot original;


    AddBlockEventTransaction(final SpongeBlockSnapshot original, final TrackerBlockEventDataBridge blockEvent) {
        super(original.getBlockPos(), (BlockState) original.getState(), original.getWorld());
        this.blockEvent = (BlockEventData) blockEvent;
        this.original = original;
    }

    @Override
    protected SpongeBlockSnapshot getResultingSnapshot() {
        return this.getOriginalSnapshot();
    }

    @Override
    protected SpongeBlockSnapshot getOriginalSnapshot() {
        return this.original;
    }

    @Override
    public Optional<BiConsumer<PhaseContext<@NonNull ?>, CauseStackManager.StackFrame>> getFrameMutator(
        @Nullable final GameTransaction<@NonNull ?> parent
    ) {
        return Optional.empty();
    }

    @Override
    public void addToPrinter(final PrettyPrinter printer) {
        printer.add("AddBlockEvent")
            .add(" %s : %s", "Original Block", this.original)
            .add(" %s : %s", "Original State", this.originalState)
            .add(" %s : %s", "EventData", this.blockEvent);
    }

    @Override
    public void restore() {
        this.original.getServerWorld().ifPresent(world -> ((ServerWorldAccessor) world).accessor$blockEvents().remove(this.blockEvent));
    }
}
