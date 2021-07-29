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
package org.spongepowered.common.event.tracking.phase.block;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.util.VecHelper;

public class GrowablePhaseContext extends PhaseContext<GrowablePhaseContext> {

    protected PhaseContext<?> priorContext;
    ItemStackSnapshot usedItem;
    Level world;
    BlockState blockState;
    BlockPos pos;
    SpongeBlockSnapshot snapshot;

    protected GrowablePhaseContext(final IPhaseState<GrowablePhaseContext> state, final PhaseTracker tracker) {
        super(state, tracker);
    }

    public GrowablePhaseContext provideItem(final ItemStack stack) {
        this.usedItem = ItemStackUtil.snapshotOf(stack);
        this.priorContext = PhaseTracker.getInstance().getPhaseContext();
        return this;
    }

    public GrowablePhaseContext world(final Level worldIn) {
        this.world = worldIn;
        return this;
    }

    public GrowablePhaseContext block(final BlockState blockState) {
        this.blockState = blockState;
        return this;
    }

    public GrowablePhaseContext pos(final BlockPos pos) {
        this.pos = pos;
        return this;
    }

    @Override
    public GrowablePhaseContext buildAndSwitch() {
        checkState(this.pos != null, "BlockPos is null");
        checkState(this.blockState != null, "BlockState is null");
        checkState(this.usedItem != null, "ItemUsed is null");
        checkState(this.priorContext != null, "Prior context is null");
        checkState(this.world != null, "World is null");
        final SpongeBlockSnapshot.BuilderImpl builder = SpongeBlockSnapshot.BuilderImpl.pooled()
            .world(((ServerLevel) this.world))
            .position(VecHelper.toVector3i(this.pos))
            .blockState(this.blockState)
            .flag(BlockChangeFlags.NONE.withPhysics(true).withUpdateNeighbors(true).withNotifyObservers(true));
        this.priorContext.applyOwnerIfAvailable(builder::creator);
        this.priorContext.applyNotifierIfAvailable(builder::notifier);
        this.snapshot = builder.build();
        return super.buildAndSwitch();
    }

    @Override
    protected void reset() {
        super.reset();
        this.priorContext = null;
        this.usedItem = null;
        this.world = null;
        this.blockState = null;
        this.pos = null;
        this.snapshot = null;
    }

    @Override
    protected GrowablePhaseContext defensiveCopy(PhaseTracker tracker) {
        final GrowablePhaseContext newCopy = super.defensiveCopy(tracker);
        return newCopy;
    }
}
