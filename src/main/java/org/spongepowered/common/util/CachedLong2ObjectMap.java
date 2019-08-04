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

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.spongepowered.common.bridge.util.CacheKeyBridge;

import javax.annotation.Nullable;

public class CachedLong2ObjectMap<V extends CacheKeyBridge> extends Long2ObjectOpenHashMap<V> {

    private static final long serialVersionUID = 190617916448550012L;
    @Nullable private V lastRetrievedValue = null;

    @Override
    public V get(final long key) {
        if (this.lastRetrievedValue != null && key == this.lastRetrievedValue.bridge$getCacheKey()) {
            return this.lastRetrievedValue;
        }
        return this.lastRetrievedValue = super.get(key);
    }

    @Override
    public V remove(final long key) {
        if (this.lastRetrievedValue != null && key == this.lastRetrievedValue.bridge$getCacheKey()) {
            this.lastRetrievedValue = null;
        }
        return super.remove(key);
    }

    @Override
    public boolean containsKey(final long key) {
        return this.get(key) != null;
    }
}
