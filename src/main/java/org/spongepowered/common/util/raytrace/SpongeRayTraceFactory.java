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
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.blockray.RayTrace;
import org.spongepowered.api.world.LocatableBlock;

import java.util.function.Predicate;

public final class SpongeRayTraceFactory implements RayTrace.Factory {

    private final Predicate<LocatableBlock> onlyAir, notAir;

    public SpongeRayTraceFactory() {
        this.onlyAir = locatableBlock -> {
            final BlockType type = locatableBlock.blockState().type();
            return type == BlockTypes.AIR.get() ||
                    type == BlockTypes.CAVE_AIR.get() ||
                    type == BlockTypes.VOID_AIR.get();
        };

        this.notAir = this.onlyAir.negate();
    }

    @Override
    public @NonNull RayTrace<@NonNull Entity> entityRayTrace() {
        return new SpongeEntityRayTrace();
    }

    @Override
    public @NonNull RayTrace<@NonNull LocatableBlock> blockRayTrace() {
        return new SpongeBlockRayTrace();
    }

    @Override
    public @NonNull Predicate<LocatableBlock> onlyAir() {
        return this.onlyAir;
    }

    @Override
    public @NonNull Predicate<LocatableBlock> notAir() {
        return this.notAir;
    }

}
