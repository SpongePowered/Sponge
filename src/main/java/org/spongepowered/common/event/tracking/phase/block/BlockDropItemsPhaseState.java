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

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;

import java.util.function.BiConsumer;

final class BlockDropItemsPhaseState extends BlockPhaseState {

    private final BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> BLOCK_DROP_MODIFIER = super.getFrameModifier()
        .andThen((frame, ctx) -> {
            final BlockSnapshot blockSnapshot = ctx.getSource(BlockSnapshot.class)
                .orElseThrow(TrackingUtil.throwWithContext("Could not find a block dropping items!", ctx));
            frame.pushCause(blockSnapshot);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
        });

    BlockDropItemsPhaseState() {
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> getFrameModifier() {
        return this.BLOCK_DROP_MODIFIER;
    }

    @Override
    public GeneralizedContext createNewContext(final PhaseTracker tracker) {
        return super.createNewContext(tracker)
                .addBlockCaptures()
                .addEntityCaptures();
    }

    @SuppressWarnings({"unchecked", "Duplicates", "RedundantCast"})
    @Override
    public void unwind(final GeneralizedContext context) {
        TrackingUtil.processBlockCaptures(context);
    }

    @Override
    public boolean tracksBlockSpecificDrops(final GeneralizedContext context) {
        return true;
    }
}
