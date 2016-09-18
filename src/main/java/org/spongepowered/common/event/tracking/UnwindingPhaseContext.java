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
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.common.event.InternalNamedCauses;

import java.util.Optional;

final class UnwindingPhaseContext extends PhaseContext {

    static PhaseContext unwind(IPhaseState state, PhaseContext context) {
        return new UnwindingPhaseContext(state, context);
    }

    private PhaseContext unwindingContext;
    private IPhaseState unwindingState;

    UnwindingPhaseContext(IPhaseState unwindingState, PhaseContext unwindingContext) {
        add(NamedCause.of(InternalNamedCauses.Tracker.UNWINDING_CONTEXT, unwindingContext));
        add(NamedCause.of(InternalNamedCauses.Tracker.UNWINDING_STATE, unwindingState));
        this.unwindingContext = unwindingContext;
        this.unwindingState = unwindingState;
    }

    @Override
    public Optional<User> getOwner() {
        return this.unwindingContext.getOwner();
    }

    @Override
    public Optional<User> getNotifier() {
        return this.unwindingContext.getNotifier();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> first(Class<T> tClass) {
        if (PhaseContext.class == tClass) {
            return Optional.of((T) this.unwindingContext);
        }
        if (IPhaseState.class == tClass) {
            return Optional.of((T) this.unwindingState);
        }
        return Optional.ofNullable(super.first(tClass).orElseGet(() -> this.unwindingContext.first(tClass).orElse(null)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> firstNamed(String name, Class<T> tClass) {
        if (PhaseContext.class == tClass) {
            return Optional.of((T) this.unwindingContext);
        }
        if (IPhaseState.class == tClass) {
            return Optional.of((T) this.unwindingState);
        }
        return Optional.ofNullable(super.firstNamed(name, tClass).orElseGet(() -> this.unwindingContext.firstNamed(name, tClass).orElse(null)));

    }

}
