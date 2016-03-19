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
package org.spongepowered.common.event.tracking.phase;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;

import javax.annotation.Nullable;

public class GeneralPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        COMMAND,
        COMPLETE;

        public boolean isBusy() {
            return this != COMPLETE;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return !isBusy() || state instanceof BlockPhase.State;
        }

        @Override
        public GeneralPhase getPhase() {
            return TrackingPhases.GENERAL;
        }
    }

    public GeneralPhase(@Nullable TrackingPhase parent) {
        super(parent);
    }

    @Override
    public GeneralPhase addChild(TrackingPhase child) {
        super.addChild(child);
        return this;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.COMMAND) {
            Cause cause = phaseContext.toCause();
            ICommand command = cause.first(ICommand.class).get();
            ICommandSender sender = cause.first(ICommandSender.class).get();
            checkState(command != null, "Cannot complete a command when there was no command executed!");
            checkState(sender != null, "Cannot complete a command when there was no command sender!");
            // todo properly unwind the captured block changes and entity spawns.
        }
    }
}
