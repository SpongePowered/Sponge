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
package org.spongepowered.common.bridge.block;

import net.minecraft.world.level.block.Block;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.value.Value;


/**
 * A quasi interface to mix into every possible {@link Block} such that their
 * acceptable {@link BlockState}s can be created, manipulated, and applied
 * with the safety of using these instance checks of the {@link BlockBridge}.
 * The advantage of this is that a simple cast from {@link Block} to a
 * particular {@link BlockBridge} to take advantage of particular {@link Value}
 * types, are really simple to perform.
 *
 * <p>It is important to note that when using this level of implementation,
 * it is already guaranteed that a particular {@link BlockBridge} is capable
 * of a particular type thanks to {@link Mixin}s. All that is needed to handle
 * a particular type of {@link Value} or {@link Immutable} is a
 * simple cast. This is particularly useful for {@link BlockState}s as
 * they already know the type they need to focus on.</p>
 */
public interface BlockBridge {
    // Normal API methods

    /**
     * Used only for Forge's dummy air block that is acting as a surrogate block for missing
     * mod blocks. Usually when a block is simply marked for replacement when a mod is re-introduced.
     *
     * @return True if this block is a surrogate dummy block. Should only be used for forge blocks.
     */
    default boolean bridge$isDummy() {
        return false;
    }

}
