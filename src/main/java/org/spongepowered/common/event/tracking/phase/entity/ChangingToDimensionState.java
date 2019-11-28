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

import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.bridge.world.WorldServerBridge;

final class ChangingToDimensionState extends EntityPhaseState<DimensionChangeContext> {

    @Override
    public DimensionChangeContext createNewContext() {
        return new DimensionChangeContext(this)
            .addBlockCaptures()
            .addEntityCaptures();
    }

    @Override
    public boolean tracksBlockSpecificDrops(final DimensionChangeContext context) {
        return true;
    }

    @Override
    public boolean spawnEntityOrCapture(final DimensionChangeContext context, final Entity entity, final int chunkX, final int chunkZ) {
        final ServerWorld worldServer = context.getTargetWorld();
        // Allowed to use the force spawn because it's the same "entity"
        ((WorldServerBridge) worldServer).bridge$forceSpawnEntity((net.minecraft.entity.Entity) entity);
        return true;
    }

    @Override
    public boolean doesDenyChunkRequests() {
        return false;
    }



}
