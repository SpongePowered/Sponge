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

import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.PrettyPrinter;

import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("unchecked")
public abstract class GenerationContext<G extends GenerationContext<G>> extends PhaseContext<G> {

    @Nullable private ServerWorld world;
    @Nullable private ChunkGenerator<? extends GenerationSettings> generator;

    GenerationContext(final IPhaseState<G> state, final PhaseTracker tracker) {
        super(state, tracker);
    }

    @Override
    protected void reset() {
        super.reset();
        this.world = null;
        this.generator = null;
    }

    public final G world(final ServerWorld world) {
        this.world = Objects.requireNonNull(world);
        return (G) this;
    }

    public final ServerWorld getWorld() {
        return Objects.requireNonNull(this.world);
    }

    public final G generator(final ChunkGenerator<? extends GenerationSettings> generator) {
        this.generator = Objects.requireNonNull(generator);
        return (G) this;
    }

    public final ChunkGenerator<? extends GenerationSettings> getGenerator() {
        return Objects.requireNonNull(this.generator);
    }

    @Override
    public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
        final String s = String.format("%1$" + indent + "s", "");
        return super.printCustom(printer, indent)
            .add(s + "- %s: %s", "World", this.world)
            .add(s + "- %s: %s", "Generator", this.generator);
    }
}
