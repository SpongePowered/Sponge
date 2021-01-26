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
package org.spongepowered.common.util.raytrace;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.util.blockray.RayTraceResult;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.phys.Vec3;

public final class SpongeBlockRayTrace extends AbstractSpongeRayTrace<@NonNull LocatableBlock> {

    private static final Predicate<LocatableBlock> DEFAULT_FILTER = block -> {
        final BlockType type = block.getBlockState().getType();
        return type != BlockTypes.CAVE_AIR.get() && type != BlockTypes.VOID_AIR.get() && type != BlockTypes.AIR.get();
    };

    SpongeBlockRayTrace() {
        super(SpongeBlockRayTrace.DEFAULT_FILTER);
    }

    @Override
    final Optional<RayTraceResult<@NonNull LocatableBlock>> testSelectLocation(final ServerWorld serverWorld,
            final Vec3 location,
            final Vec3 exitLocation) {

        final LocatableBlock initialBlock = this.getBlock(serverWorld, location, exitLocation);
        if (this.select.test(initialBlock)) {
            return Optional.of(new SpongeRayTraceResult<>(initialBlock, VecHelper.toVector3d(location)));
        }
        return Optional.empty();
    }
}
