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
package org.spongepowered.common.service.server.context;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.context.ContextService;
import org.spongepowered.common.event.tracking.PhaseTracker;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.inject.Inject;

public class SpongeContextService implements ContextService {
    private final List<ContextCalculator> calculator = new CopyOnWriteArrayList<>();

    @Inject
    SpongeContextService() {
    }

    @Override
    public @NonNull Set<Context> contexts() {
        return this.contextsFor(PhaseTracker.getInstance().currentCause());
    }

    @Override
    public @NonNull Set<Context> contextsFor(final @NonNull Cause cause) {
        final ImmutableSet.Builder<Context> result = ImmutableSet.builder();
        final Consumer<Context> accumulator = result::add;
        for (final ContextCalculator calc : this.calculator) {
            calc.accumulateContexts(cause, accumulator);
        }
        return result.build();
    }

    @Override
    public void registerContextCalculator(final @NonNull ContextCalculator calculator) {
        this.calculator.add(calculator);
    }
}
