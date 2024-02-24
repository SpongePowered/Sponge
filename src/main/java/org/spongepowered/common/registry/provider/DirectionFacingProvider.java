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
package org.spongepowered.common.registry.provider;


import com.google.common.collect.ImmutableBiMap;
import org.spongepowered.api.util.Direction;

import java.util.Objects;
import java.util.Optional;

public final class DirectionFacingProvider {

    public static final DirectionFacingProvider INSTANCE = new DirectionFacingProvider();

    private final ImmutableBiMap<Direction, net.minecraft.core.Direction> mappings;

    DirectionFacingProvider() {
        this.mappings = ImmutableBiMap.<Direction, net.minecraft.core.Direction>builder()
                .put(Direction.NORTH, net.minecraft.core.Direction.NORTH)
                .put(Direction.EAST, net.minecraft.core.Direction.EAST)
                .put(Direction.SOUTH, net.minecraft.core.Direction.SOUTH)
                .put(Direction.WEST, net.minecraft.core.Direction.WEST)
                .put(Direction.UP, net.minecraft.core.Direction.UP)
                .put(Direction.DOWN, net.minecraft.core.Direction.DOWN)
                .build();
    }

    public Optional<net.minecraft.core.Direction> get(Direction key) {
        return Optional.ofNullable(this.mappings.get(Objects.requireNonNull(key)));
    }

    public Optional<Direction> getKey(net.minecraft.core.Direction value) {
        return Optional.ofNullable(this.mappings.inverse().get(Objects.requireNonNull(value)));
    }
}
