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
package org.spongepowered.common.world.schematic;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.common.util.Bitset;

import java.util.Iterator;
import java.util.Optional;

public class BimapPalette implements Palette {

    private static final int DEFAULT_ALLOCATION_SIZE = 64;

    private final BiMap<Integer, BlockState> ids;
    private final BiMap<BlockState, Integer> idsr;
    private final Bitset allocation = new Bitset(DEFAULT_ALLOCATION_SIZE);
    private int maxId = 0;

    public BimapPalette() {
        this.ids = HashBiMap.create();
        this.idsr = this.ids.inverse();
    }

    public BimapPalette(int expectedSize) {
        this.ids = HashBiMap.create(expectedSize);
        this.idsr = this.ids.inverse();
    }

    @Override
    public int getLength() {
        return this.maxId;
    }

    @Override
    public Optional<Integer> get(BlockState state) {
        return Optional.ofNullable(this.idsr.get(state));
    }

    @Override
    public int getOrAssign(BlockState state) {
        Integer id = this.idsr.get(state);
        if (id == null) {
            Iterator<Integer> it = this.allocation.iterator();
            int next;
            if (it.hasNext()) {
                next = this.allocation.iterator().next();
            } else {
                next = this.allocation.getLength();
            }
            this.allocation.set(next);
            this.ids.put(next, state);
            return next;
        }
        return id;
    }

    @Override
    public Optional<BlockState> get(int id) {
        return Optional.ofNullable(this.ids.get(id));
    }

    @Override
    public boolean remove(BlockState state) {
        Integer id = this.idsr.get(state);
        if (id == null) {
            return false;
        }
        this.allocation.unset(id);
        this.ids.remove(id);
        return true;
    }

}
