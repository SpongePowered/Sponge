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
package org.spongepowered.common.data.manipulator.mutable.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Booleans;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableBreakableData;
import org.spongepowered.api.data.manipulator.mutable.item.BreakableData;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeBreakableData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;

import java.util.List;
import java.util.Set;

@NonnullByDefault
public class SpongeBreakableData extends AbstractSingleData<Set<BlockType>, BreakableData, ImmutableBreakableData> implements BreakableData {
    
    public SpongeBreakableData() {
        this(Sets.<BlockType>newHashSet());
    }

    public SpongeBreakableData(Set<BlockType> breakable) {
        super(BreakableData.class, Sets.newHashSet(breakable), Keys.BREAKABLE_BLOCK_TYPES);
    }

    @Override
    public int compareTo(BreakableData o) {
        return Booleans.compare(o.breakable().containsAll(getValue()),
                getValue().containsAll(o.breakable().get()));
    }

    @Override
    public DataContainer toContainer() {
        List<String> breakableIds = Lists.newArrayListWithExpectedSize(getValue().size());
        for (BlockType breakable : getValue()) {
            breakableIds.add(breakable.getId());
        }
        return new MemoryDataContainer().set(Keys.BREAKABLE_BLOCK_TYPES.getQuery(), breakableIds);
    }

    @Override
    public SetValue<BlockType> breakable() {
        return new SpongeSetValue<BlockType>(Keys.BREAKABLE_BLOCK_TYPES, Sets.newHashSet(getValue()));
    }

    @Override
    public BreakableData copy() {
        return new SpongeBreakableData(getValue());
    }

    @Override
    public ImmutableBreakableData asImmutable() {
        return new ImmutableSpongeBreakableData(getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return breakable();
    }
}
