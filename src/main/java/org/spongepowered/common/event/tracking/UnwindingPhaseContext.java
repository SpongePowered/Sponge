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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.util.PrettyPrinter;

import java.util.Optional;

public final class UnwindingPhaseContext extends PhaseContext<UnwindingPhaseContext> {

    @Override
    protected void reset() {
        super.reset();
    }

    static @Nullable UnwindingPhaseContext unwind(final PhaseContext<@NonNull ?> context, final boolean hasCaptures) {
        if (!context.requiresPost() || !hasCaptures) {
            return null;
        }
        return new UnwindingPhaseContext(context)
                .source(context.getSource())
                .buildAndSwitch();
    }

    private final IPhaseState<@NonNull ?> unwindingState;
    private final PhaseContext<@NonNull ?> unwindingContext;

    private UnwindingPhaseContext(final PhaseContext<@NonNull ?> unwindingContext) {
        super(GeneralPhase.Post.UNWINDING, unwindingContext.createdTracker);
        this.unwindingState = unwindingContext.state;
        this.unwindingContext = unwindingContext;
        this.setBlockEvents(unwindingContext.doesBlockEventTracking());
        // Basically put, the post state needs to understand that if we're expecting potentially chained block changes
        // to worlds, AND we're potentially getting any neighbor notification requests OR tile entity requests,
        // we'll need to switch on to capture such objects. If for example, we do not track tile changes, but we track
        // neighbor notifications, that would be fine, but we cannot require that both are tracked unless specified.
    }

    @Override
    public Optional<User> getCreator() {
        return this.unwindingContext.getCreator();
    }

    @Override
    public Optional<User> getNotifier() {
        return this.unwindingContext.getNotifier();
    }

    @SuppressWarnings("unchecked")
    public <T extends PhaseContext<T>> T getUnwindingContext() {
        return (T) this.unwindingContext;
    }

    IPhaseState<?> getUnwindingState() {
        return this.unwindingState;
    }

    @Override
    public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
        final String s = String.format("%1$" + indent + "s", "");
        super.printCustom(printer, indent)
            .add(s + "- %s: %s", "UnwindingState", this.unwindingState)
            .add(s + "- %s: %s", "UnwindingContext", this.unwindingContext)
        ;
        this.unwindingContext.printCustom(printer, indent * 2);
        return printer;
    }
}
