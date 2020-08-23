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

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;

import java.util.ArrayList;
import java.util.function.BiConsumer;

final class RestoringBlockPhaseState extends BlockPhaseState {

    @SuppressWarnings("unchecked")
    @Override
    public BiConsumer<CauseStackManager.StackFrame, GeneralizedContext> getFrameModifier() {
        return (BiConsumer<CauseStackManager.StackFrame, GeneralizedContext>) IPhaseState.DEFAULT_OWNER_NOTIFIER;
    }

    @Override
    public boolean isRestoring() {
        return true;
    }

    @Override
    public boolean shouldCaptureBlockChangeOrSkip(
        final GeneralizedContext phaseContext, final BlockPos pos, final BlockState currentState, final BlockState newState,
        final BlockChangeFlag flags) {
        return false;
    }

    @Override
    public boolean doesAllowEntitySpawns() {
        return false;
    }

    @Override
    public boolean spawnEntityOrCapture(final GeneralizedContext context, final Entity entity) {
        final ArrayList<Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()){
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    @Override
    public boolean tracksCreatorsAndNotifiers() {
        return false;
    }

    @Override
    public boolean doesBulkBlockCapture(final GeneralizedContext context) {
        return false;
    }

    @Override
    public boolean doesBlockEventTracking(final GeneralizedContext context) {
        return false;
    }

}
