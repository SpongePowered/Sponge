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
package org.spongepowered.common.event.tracking.phase.entity;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class EntityCollisionState extends EntityPhaseState<BasicEntityContext> {

    @Override
    protected BasicEntityContext createNewContext() {
        return new BasicEntityContext(this).addEntityCaptures();
    }

    @Override
    public boolean isCollision() {
        return true;
    }

    @Override
    public void unwind(BasicEntityContext context) {
        context.getCapturedItemsSupplier()
            .acceptAndClearIfNotEmpty(items -> {
                final List<Entity> entities = items.stream()
                    .map(entity -> (Entity) entity)
                    .collect(Collectors.toList());
                Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PASSIVE);
                SpongeCommonEventFactory.callDropItemCustom(entities, context);
            });
        TrackingUtil.processBlockCaptures(context);
        context.getCapturedEntitySupplier()
                .acceptAndClearIfNotEmpty(mc -> {
                    final List<Entity> entities = new ArrayList<>(mc);
                    Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PASSIVE);
                    SpongeCommonEventFactory.callSpawnEntityCustom(entities, context);
                });
    }
}
