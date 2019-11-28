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

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.util.VecHelper;

public class GrowablePhaseContext extends PhaseContext<GrowablePhaseContext> {

    protected PhaseContext<?> priorContext;
    ItemStackSnapshot usedItem;
    World world;
    BlockState blockState;
    BlockPos pos;
    SpongeBlockSnapshot snapshot;

    protected GrowablePhaseContext(final IPhaseState<? extends GrowablePhaseContext> state) {
        super(state);
    }

    public GrowablePhaseContext provideItem(final ItemStack stack) {
        this.usedItem = ItemStackUtil.snapshotOf(stack);
        this.priorContext = PhaseTracker.getInstance().getCurrentContext();
        return this;
    }

    public GrowablePhaseContext world(final World worldIn) {
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
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled()
            .worldId(((org.spongepowered.api.world.World) this.world).getUniqueId())
            .position(VecHelper.toVector3i(this.pos))
            .blockState(this.blockState)
            .flag(BlockChangeFlags.PHYSICS_OBSERVER);
        this.priorContext.applyOwnerIfAvailable((owner) -> builder.creator(owner.getUniqueId()));
        this.priorContext.applyNotifierIfAvailable((notifier) -> builder.notifier(notifier.getUniqueId()));
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
}
