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
package org.spongepowered.common.util;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.util.Direction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public final class DirectionalBlockUtils {

    private DirectionalBlockUtils() {
    }

    public static <V extends Comparable<V>, P extends IProperty<V>>
                  IBlockState applyConnectedDirections(IBlockState blockState,
                                                       Map<Direction, P> mapping,
                                                       BiFunction<IBlockState, P, V> setValueResolver,
                                                       BiFunction<IBlockState, P, V> resetValueResolver,
                                                       Set<Direction> directions) {
        Map<P, V> facingStates = new HashMap<>();
        for (P property: mapping.values()) {
            facingStates.put(property, resetValueResolver.apply(blockState, property));
        }
        for (Direction connectedDirection: directions) {
            if (connectedDirection.isCardinal()) {
                P facingProperty = mapping.get(connectedDirection);
                facingStates.put(facingProperty, setValueResolver.apply(blockState, facingProperty));
            }
        }
        IBlockState resultBlockState = blockState;
        for (P property: facingStates.keySet()) {
            resultBlockState = resultBlockState.withProperty(property, facingStates.get(property));
        }
        return resultBlockState;
    }
}
