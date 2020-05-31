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

import com.google.common.collect.EnumBiMap;
import com.google.common.collect.ImmutableBiMap;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.registry.TypeProvider;

import java.util.Optional;

public final class DirectionFacingProvider implements TypeProvider<Direction, EnumFacing> {

    public static DirectionFacingProvider getInstance() {
        return Holder.INSTANCE;
    }

    public static final EnumBiMap<Direction, EnumFacing> directionMap = EnumBiMap.create(Direction.class, EnumFacing.class);
    static {
        directionMap.put(Direction.NORTH, EnumFacing.NORTH);
        directionMap.put(Direction.EAST, EnumFacing.EAST);
        directionMap.put(Direction.SOUTH, EnumFacing.SOUTH);
        directionMap.put(Direction.WEST, EnumFacing.WEST);
        directionMap.put(Direction.UP, EnumFacing.UP);
        directionMap.put(Direction.DOWN, EnumFacing.DOWN);
    }

    @Override
    public Optional<EnumFacing> get(Direction key) {
        return Optional.ofNullable(directionMap.get(checkNotNull(key)));
    }

    @Override
    public Optional<Direction> getKey(EnumFacing value) {
        return Optional.ofNullable(directionMap.inverse().get(checkNotNull(value)));
    }

    DirectionFacingProvider() { }

    private static final class Holder {
        static final DirectionFacingProvider INSTANCE = new DirectionFacingProvider();

    }
}
