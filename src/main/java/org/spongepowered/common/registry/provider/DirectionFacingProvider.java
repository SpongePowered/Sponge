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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableBiMap;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.registry.TypeProvider;

import java.util.Optional;

public final class DirectionFacingProvider implements TypeProvider<Direction, net.minecraft.util.Direction> {

    public static DirectionFacingProvider getInstance() {
        return Holder.INSTANCE;
    }

    public static final ImmutableBiMap<Direction, net.minecraft.util.Direction> directionMap = ImmutableBiMap.<Direction, net.minecraft.util.Direction>builder()
        .put(Direction.NORTH, net.minecraft.util.Direction.NORTH)
        .put(Direction.EAST, net.minecraft.util.Direction.EAST)
        .put(Direction.SOUTH, net.minecraft.util.Direction.SOUTH)
        .put(Direction.WEST, net.minecraft.util.Direction.WEST)
        .put(Direction.UP, net.minecraft.util.Direction.UP)
        .put(Direction.DOWN, net.minecraft.util.Direction.DOWN)
        .build();

    @Override
    public Optional<net.minecraft.util.Direction> get(Direction key) {
        return Optional.ofNullable(directionMap.get(checkNotNull(key)));
    }

    @Override
    public Optional<Direction> getKey(net.minecraft.util.Direction value) {
        return Optional.ofNullable(directionMap.inverse().get(checkNotNull(value)));
    }

    DirectionFacingProvider() { }

    private static final class Holder {
        static final DirectionFacingProvider INSTANCE = new DirectionFacingProvider();

    }
}
