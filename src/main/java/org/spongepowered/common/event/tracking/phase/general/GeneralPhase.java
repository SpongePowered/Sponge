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
package org.spongepowered.common.event.tracking.phase.general;

import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.UnwindingPhaseContext;
import org.spongepowered.common.event.tracking.UnwindingState;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;

public final class GeneralPhase {

    public static final class State {
        public static final IPhaseState<CommandPhaseContext> COMMAND = new CommandState();
        public static final IPhaseState<ExplosionContext> EXPLOSION = new ExplosionState();
        public static final IPhaseState<GeneralizedContext> COMPLETE = new CompletePhase();
        public static final IPhaseState<SaveHandlerCreationContext> SAVE_HANDLER_CREATION = new SaveHandlerCreationPhase();
        public static final IPhaseState<?> WORLD_UNLOAD = new WorldUnload();
        public static final IPhaseState<MapConversionContext> MAP_CONVERSION = new MapConversionPhase();

        private State() { }
    }

    public static final class Post {
        public static final IPhaseState<UnwindingPhaseContext> UNWINDING = UnwindingState.getInstance();

        private Post() { }
    }


    private GeneralPhase() {
    }


}
