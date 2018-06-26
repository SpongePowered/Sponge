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
package org.spongepowered.common.event.tracking.phase.plugin;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.IEntitySpecificItemDropsState;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.util.VecHelper;

final class CustomExplosionState extends PluginPhaseState<ExplosionContext> implements IEntitySpecificItemDropsState<ExplosionContext> {
    @Override
    public ExplosionContext createPhaseContext() {
        return new ExplosionContext()
            .addEntityCaptures()
            .addEntityDropCaptures()
            .addBlockCaptures();
    }

    @Override
    public void unwind(ExplosionContext context) {

        final Explosion explosion = context.getExplosion();
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.TNT_IGNITE);
            frame.pushCause(explosion);
            if (context.getNotifier().isPresent()) {
                frame.addContext(EventContextKeys.NOTIFIER, context.getNotifier().get());
            }
            if (context.getOwner().isPresent()) {
                frame.addContext(EventContextKeys.OWNER, context.getOwner().get());
            }
            context.getCapturedBlockSupplier()
                .acceptAndClearIfNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, this, context));
            context.getCapturedEntitySupplier()
                .acceptAndClearIfNotEmpty(entities -> SpongeCommonEventFactory.callSpawnEntity(entities, context));
        }
    }

    @Override
    public boolean canSwitchTo(IPhaseState<?> state) {
        return true;
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return true;
    }

    @Override
    public boolean shouldCaptureBlockChangeOrSkip(ExplosionContext phaseContext, BlockPos pos) {
        final Vector3i blockPos = VecHelper.toVector3i(pos);
        for (final BlockSnapshot capturedSnapshot : phaseContext.getCapturedBlocks()) {
            if (capturedSnapshot.getPosition().equals(blockPos)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean requiresBlockBulkCaptures() {
        return false;
    }
}
