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
package org.spongepowered.common.state;

import org.checkerframework.checker.nullness.qual.NonNull;

import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.state.StateMatcher;

import java.util.ArrayList;
import java.util.HashMap;

public final class SpongeFluidStateMatcherBuilder extends AbstractStateMatcherBuilder<FluidState, FluidType> {

    @Override
    public @NonNull StateMatcher<@NonNull FluidState> build() throws IllegalStateException {
        if (this.type == null) {
            throw new IllegalStateException("BlockType cannot be null");
        }
        return new SpongeFluidStateMatcher(this.type,
                new ArrayList<>(this.requiredProperties),
                new HashMap<>(this.properties),
                new ArrayList<>(this.keyValueMatchers));
    }

    @Override
    public StateMatcher.Builder<@NonNull FluidState, @NonNull FluidType> from(final @NonNull StateMatcher<@NonNull FluidState> value) {
        if (!(value instanceof SpongeFluidStateMatcher)) {
            throw new IllegalArgumentException("BlockStateMatcher must be a SpongeBlockStateMatcher");
        }
        return super.from(value);
    }

}
