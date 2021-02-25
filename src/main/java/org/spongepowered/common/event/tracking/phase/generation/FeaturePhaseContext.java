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
package org.spongepowered.common.event.tracking.phase.generation;

import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import java.util.Objects;

public final class FeaturePhaseContext extends GenerationContext<FeaturePhaseContext> {

    @Nullable private Feature<?> feature;
    @Nullable private BlockPos origin;

    FeaturePhaseContext(final IPhaseState<FeaturePhaseContext> state, final PhaseTracker tracker) {
        super(state, tracker);
    }

    public FeaturePhaseContext feature(final Feature<?> feature) {
        this.feature = Objects.requireNonNull(feature);
        return this;
    }

    public Feature<?> getFeature() {
        return Objects.requireNonNull(this.feature);
    }

    public FeaturePhaseContext origin(final BlockPos origin) {
        this.origin = Objects.requireNonNull(origin);
        return this;
    }

    public BlockPos getOrigin() {
        return Objects.requireNonNull(this.origin);
    }
}
