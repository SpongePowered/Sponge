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

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

final class PopulatorGenerationPhaseState extends GeneralGenerationPhaseState {

    PopulatorGenerationPhaseState(String id) {
        super(id);
    }

    @Override
    Cause provideSpawnCause(PhaseContext context) {
        final PopulatorType runningGenerator = context.firstNamed(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, PopulatorType.class)
                .orElse(null);
        final World world = context.firstNamed(InternalNamedCauses.WorldGeneration.WORLD, World.class).orElse(null);
        final Cause.Builder causeBuilder = Cause.builder();
        Cause.source(InternalSpawnTypes.SpawnCauses.WORLD_SPAWNER_CAUSE).named("World", world);
        if (InternalPopulatorTypes.ANIMAL.equals(runningGenerator)) {
            causeBuilder.named(NamedCause.source(InternalSpawnTypes.SpawnCauses.WORLD_SPAWNER_CAUSE))
                    .named(NamedCause.of(InternalNamedCauses.General.ANIMAL_SPAWNER, runningGenerator));
        } else if (runningGenerator != null) {
            causeBuilder.named(NamedCause.source(InternalSpawnTypes.SpawnCauses.STRUCTURE_SPAWNING))
                    .named(NamedCause.of(InternalNamedCauses.WorldGeneration.STRUCTURE, runningGenerator));
        } else {
            causeBuilder.named(NamedCause.source(InternalSpawnTypes.SpawnCauses.STRUCTURE_SPAWNING));
        }
        return causeBuilder.build();
    }

}
