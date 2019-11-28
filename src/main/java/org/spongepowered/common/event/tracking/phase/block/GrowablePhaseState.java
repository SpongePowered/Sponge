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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PooledPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.function.BiConsumer;

@SuppressWarnings({"unchecked", "rawTypes"})
public class GrowablePhaseState extends PooledPhaseState<GrowablePhaseContext> implements IPhaseState<GrowablePhaseContext> {

    private final BiConsumer<CauseStackManager.StackFrame, GrowablePhaseContext> FRAME_MODIFIER = IPhaseState.super.getFrameModifier()
        .andThen((stackFrame, growablePhaseContext) -> {
            if (!growablePhaseContext.usedItem.isEmpty()) {
                stackFrame.addContext(EventContextKeys.USED_ITEM, growablePhaseContext.usedItem);
            }
            stackFrame.addContext(EventContextKeys.GROWTH_ORIGIN, growablePhaseContext.snapshot);

        });

    @Override
    public GrowablePhaseContext createNewContext() {
        final GrowablePhaseContext context = new GrowablePhaseContext(this);
        return context.addBlockCaptures();
    }

    @Override
    public void unwind(GrowablePhaseContext phaseContext) {
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    @Override
    public boolean spawnEntityOrCapture(GrowablePhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.STRUCTURE);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    @Override
    public BlockChange associateBlockChangeWithSnapshot(GrowablePhaseContext phaseContext,
        BlockState newState, Block newBlock, BlockState currentState, SpongeBlockSnapshot snapshot, Block originalBlock) {
        return BlockChange.GROW;
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, GrowablePhaseContext> getFrameModifier() {
        return this.FRAME_MODIFIER;
    }

    @Override
    public boolean doesBulkBlockCapture(GrowablePhaseContext context) {
        return true;
    }

    @Override
    public boolean doesDenyChunkRequests() {
        return true;
    }

    @Override
    public boolean includesDecays() {
        return true;
    }

    private final String desc = TrackingUtil.phaseStateToString("Growable", this);

    @Override
    public String toString() {
        return this.desc;
    }
}
