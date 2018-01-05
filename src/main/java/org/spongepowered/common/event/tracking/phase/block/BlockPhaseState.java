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

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.common.event.tracking.GeneralizedContext;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;

public class BlockPhaseState implements IPhaseState<GeneralizedContext> {

    BlockPhaseState() {
    }

    @Override
    public boolean canSwitchTo(IPhaseState<?> state) {
        return false;
    }

    @Override
    public final BlockPhase getPhase() {
        return TrackingPhases.BLOCK;
    }

    @Override
    public GeneralizedContext createPhaseContext() {
        return new GeneralizedContext(this);
    }

    @Override
    public void unwind(GeneralizedContext context) {

    }

    @Override
    public boolean spawnEntityOrCapture(GeneralizedContext context, Entity entity, int chunkX, int chunkZ) {
        if (entity instanceof EntityItem) {
            return context.getCapturedItems().add((EntityItem) entity);
        } else {
            return context.getCapturedEntities().add(entity);
        }
    }

    @Override
    public boolean allowEntitySpawns() {
        return true;
    }

    private final String className = this.getClass().getSimpleName();

    @Override
    public String toString() {
        return this.getPhase() + "{" + this.className + "}";
    }
}
