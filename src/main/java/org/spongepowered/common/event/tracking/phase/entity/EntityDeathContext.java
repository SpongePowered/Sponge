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

import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;

public class EntityDeathContext extends EntityContext<EntityDeathContext> {

    EntityDeathContext(
        final IPhaseState<? extends EntityDeathContext> state) {
        super(state);
    }

    /**
     * Double checks the last state on the stack to verify that the drop phase
     * is not needing to be cleaned up. Since the state is only entered during
     * drops, it needs to be properly cleaned up from the stack.
     */
    @Override
    public void close() {
        final PhaseContext<?> context = PhaseTracker.getInstance().getCurrentContext();
        // The current phase data may not be this phase data, which would ultimately
        // lead to having to close off another on top of this one.
        if (!context.equals(this)) {
            // at most we should be at a depth of 1.
            // should attempt to complete the existing one to wrap up, before exiting fully.
            context.close();
        }
        super.close();
    }

    @Override
    public boolean hasCaptures() {
        // Required for forge mods, such as Draconic Evolution, as drop events must always be fired even when empty
        return true;
    }

    @Override
    public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
        final String s = String.format("%1$" + indent + "s", "");
        return super.printCustom(printer, indent)
            .add(s + "- %s: %s", "DamageSource", this.damageSource);
    }

}
