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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.util.List;
import java.util.stream.Collectors;

final class WorldTickPhaseState extends ListenerPhaseState<WorldTickContext> {

    private final String desc;

    WorldTickPhaseState(String name) {
        this.desc = TrackingUtil.phaseStateToString("Plugin", name, this);
    }

    @Override
    public WorldTickContext createNewContext() {
        return new WorldTickContext(this).addCaptures().player();
    }

    @Override
    public void unwind(WorldTickContext phaseContext) {
        final Object container = phaseContext.getSource(Object.class)
            .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a ServerTickEvent listener!", phaseContext));

        TrackingUtil.processBlockCaptures(phaseContext);
        phaseContext.getBlockItemDropSupplier()
            .acceptAndClearIfNotEmpty(map -> map.asMap().forEach((key, value) -> {
                try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                    final LocatableBlock
                        block =
                        new SpongeLocatableBlockBuilder().world((World) phaseContext.getWorld()).position(key.func_177958_n(), key.func_177956_o(),
                            key.func_177952_p()).build();
                    frame.pushCause(container);
                    frame.pushCause(block);
                    final List<Entity> items = value.stream().map(entity -> (Entity) entity).collect(Collectors.toList());
                    SpongeCommonEventFactory.callDropItemDestruct(items, phaseContext);
                }

            }));
    }

    @Override
    public String toString() {
        return this.desc;
    }
}
