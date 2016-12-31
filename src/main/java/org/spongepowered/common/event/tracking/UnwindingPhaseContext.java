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
package org.spongepowered.common.event.tracking;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.event.InternalNamedCauses;

import java.util.Optional;

final class UnwindingPhaseContext extends PhaseContext {

    static PhaseContext unwind(IPhaseState state, PhaseContext context) {
        return new UnwindingPhaseContext(state, context);
    }

    private PhaseContext unwindingContext;

    UnwindingPhaseContext(IPhaseState unwindingState, PhaseContext unwindingContext) {
        addExtra(InternalNamedCauses.Tracker.UNWINDING_CONTEXT, unwindingContext);
        addExtra(InternalNamedCauses.Tracker.UNWINDING_STATE, unwindingState);
        this.unwindingContext = unwindingContext;
    }

    @Override
    public Optional<User> getOwner() {
        return this.unwindingContext.getOwner();
    }

    @Override
    public Optional<User> getNotifier() {
        return this.unwindingContext.getNotifier();
    }

}
