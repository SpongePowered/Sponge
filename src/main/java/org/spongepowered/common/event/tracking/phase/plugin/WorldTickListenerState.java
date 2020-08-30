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

import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;

final class WorldTickListenerState extends ListenerPhaseState<WorldTickListenerContext> {

    private final String desc;

    WorldTickListenerState(final String name) {
        this.desc = TrackingUtil.phaseStateToString("Plugin", name, this);
    }

    @Override
    public WorldTickListenerContext createNewContext(final PhaseTracker tracker) {
        return new WorldTickListenerContext(this, tracker).addCaptures().player();
    }

    @Override
    public void unwind(final WorldTickListenerContext phaseContext) {
        final Object container = phaseContext.getSource(Object.class)
            .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a ServerTickEvent listener!", phaseContext));

        TrackingUtil.processBlockCaptures(phaseContext);
//        phaseContext.getBlockItemDropSupplier()
//            .acceptAndClearIfNotEmpty(map -> map.asMap().forEach((key, value) -> {
//                try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
//                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
//                    final LocatableBlock
//                        block =
//                        new SpongeLocatableBlockBuilder().world((ServerWorld) phaseContext.getWorld()).position(key.getX(), key.getY(),
//                            key.getZ()).build();
//                    frame.pushCause(container);
//                    frame.pushCause(block);
//                    final List<Entity> items = value.stream().map(entity -> (Entity) entity).collect(Collectors.toList());
//                    SpongeCommonEventFactory.callDropItemDestruct(items, phaseContext);
//                }
//
//            }));
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
