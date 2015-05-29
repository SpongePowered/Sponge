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
package org.spongepowered.common.data.component.item;

import com.google.common.collect.Lists;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.item.BreakableComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.common.data.component.AbstractListComponent;

import java.util.List;

public class SpongeBreakableComponent extends AbstractListComponent<BlockType, BreakableComponent> implements BreakableComponent {

    public SpongeBreakableComponent() {
        super(BreakableComponent.class);
    }

    @Override
    public BreakableComponent copy() {
        return new SpongeBreakableComponent().set(this.elementList);
    }

    @Override
    public int compareTo(BreakableComponent o) {
        return o.getAll().size() - this.elementList.size();
    }

    @Override
    public DataContainer toContainer() {
        final List<String> blockIds = Lists.newArrayList();
        for (BlockType blockType : this.elementList) {
            blockIds.add(blockType.getId());
        }
        return new MemoryDataContainer()
                .set(Tokens.BREAKABLE_BLOCKS.getQuery(), blockIds);
    }
}
