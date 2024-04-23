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
package org.spongepowered.common.network.syncher;

import net.minecraft.network.syncher.SynchedEntityData;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public final class SpongeSynchedEntityDataList extends AbstractList<SynchedEntityData.DataValue<?>> {

    private final List<SynchedEntityData.DataValue<?>> list;

    private long flags;

    public SpongeSynchedEntityDataList() {
        this.list = new ArrayList<>();
    }

    public long flags() {
        return this.flags;
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public SynchedEntityData.DataValue<?> get(final int index) {
        return this.list.get(index);
    }

    @Override
    public SynchedEntityData.DataValue<?> set(final int index, final SynchedEntityData.DataValue<?> element) {
        this.flags |= 1L << element.id();
        return this.list.set(index, element);
    }

    @Override
    public void add(final int index, final SynchedEntityData.DataValue<?> element) {
        this.flags |= 1L << element.id();
        this.list.add(index, element);
    }

    @Override
    public SynchedEntityData.DataValue<?> remove(final int index) {
        final SynchedEntityData.DataValue<?> element = this.list.remove(index);
        this.flags &= ~(1L << element.id());
        return element;
    }
}
