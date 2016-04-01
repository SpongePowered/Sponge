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
package org.spongepowered.common.data.util;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataSerializable;

/**
 * A common utility class for various versions of various
 * {@link DataSerializable}s that are implemented in Sponge.
 *
 * Note that the organization is by groups, where a
 * {@link org.spongepowered.api.block.BlockState} may have
 * previous values based on damage values and a newer version
 * eliminates the need for those damage values and uses
 * the block state id. It helps to keep the constant names
 * organized.
 */
public final class DataVersions {

    public static final class BlockState {

        public static final int BLOCK_TYPE_WITH_DAMAGE_VALUE = 1;
        public static final int STATE_AS_CATALOG_ID = 2;

        private BlockState() {
        }
    }

    private DataVersions() {

    }

}
