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

import com.google.common.collect.Sets;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.inventory.util.InventoryUtil;

import java.util.Set;

public class PluginPhaseContext<P extends PluginPhaseContext<P>> extends PhaseContext<P> {

    private @MonotonicNonNull Set<AbstractContainerMenu> modifiedContainers;

    private boolean closing;

    protected PluginPhaseContext(final IPhaseState<P> phaseState, final PhaseTracker tracker) {
        super(phaseState, tracker);
    }

    @Override
    protected void reset() {
        super.reset();
        this.modifiedContainers = null;
        this.closing = false;
    }

    @Override
    public boolean captureModifiedContainer(final AbstractContainerMenu container) {
        if (this.closing) {
            return false;
        }

        if (this.modifiedContainers == null) {
            this.modifiedContainers = Sets.newHashSet();
        }

        this.modifiedContainers.add(container);
        return true;
    }

    @Override
    public void close() {
        this.closing = true;

        if (this.modifiedContainers != null) {
            InventoryUtil.postContainerEvents(this.modifiedContainers, this.getTransactor());
        }

        super.close();
    }
}
